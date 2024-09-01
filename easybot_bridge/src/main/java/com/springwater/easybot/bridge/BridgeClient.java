package com.springwater.easybot.bridge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.springwater.easybot.bridge.adapter.OpCodeAdapter;
import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.model.ServerInfo;
import com.springwater.easybot.bridge.packet.*;
import com.springwater.easybot.bridge.utils.GsonUtils;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Logger;

@WebSocket
public class BridgeClient {
    private static final Logger logger = Logger.getLogger("EasyBot");
    @Getter
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(OpCode.class, new OpCodeAdapter())
            .create();
    private final WebSocketClient client;
    private final ExecutorService executor;
    private final BridgeBehavior behavior;
    private Session session;
    @Setter
    @Getter
    private IdentifySuccessPacket identifySuccessPacket;
    @Setter
    @Getter
    private String token;
    private String uri;
    private final Object connectionLock = new Object(); // 用于同步控制的锁
    private boolean isConnected = false; // 标志是否已经连接
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();

    @Getter
    private boolean ready;

    private final ConcurrentHashMap<String, CompletableFuture<String>> callbackTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService timeoutScheduler = Executors.newScheduledThreadPool(1);
    private final long timeoutSeconds = 5; // Timeout duration
    public <T> CompletableFuture<T> sendAndWaitForCallbackAsync(PacketWithCallBackId packet, Class<T> responseType) {
        String callbackId = UUID.randomUUID().toString();
        packet.setCallBackId(callbackId);

        CompletableFuture<String> future = new CompletableFuture<>();
        callbackTasks.put(callbackId, future);

        send(gson.toJson(packet));

        ScheduledFuture<?> timeoutFuture = timeoutScheduler.schedule(() -> {
            CompletableFuture<String> removedFuture = callbackTasks.remove(callbackId);
            if (removedFuture != null) {
                removedFuture.completeExceptionally(new TimeoutException("等待EasyBot返回结果超时!"));
            }
        }, timeoutSeconds, TimeUnit.SECONDS);

        return future.thenApply(result -> {
            timeoutFuture.cancel(false); // Cancel timeout if completed successfully
            return gson.fromJson(result, responseType);
        }).exceptionally(ex -> {
            timeoutFuture.cancel(false); // Cancel timeout if an exception occurs
            throw new RuntimeException("Error waiting for callback", ex);
        });
    }

    public BridgeClient(String uri, BridgeBehavior behavior) {
        this.uri = uri;
        this.behavior = behavior;
        this.client = new WebSocketClient();
        this.executor = Executors.newSingleThreadExecutor();
        connect();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.info("已连接到服务器: " + session.getUpgradeRequest().getRequestURI());
        this.session = session;
        synchronized (connectionLock) {
            isConnected = true; // 连接成功后设置状态
        }
    }

    private void send(String message) {
        if (session != null) {
            session.getRemote().sendStringByFuture(message);
        }
    }


    @Getter
    private int heartbeatInterval = 120;

    private void startHeartbeat() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            if (session != null && session.isOpen()) {
                send(gson.toJson(new HeartbeatPacket()));
            }
        }, 0, getHeartbeatInterval(), TimeUnit.SECONDS);
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        logger.info("收到消息: " + message);
        Gson gson = getGson();
        Packet packet = gson.fromJson(message, Packet.class);
        switch (packet.getOpCode()) {
            case Hello:
                HelloPacket helloPacket = gson.fromJson(message, HelloPacket.class);

                logger.info("已连接到主程序!");
                logger.info(">>>主程序连接信息<<<");
                logger.info("系统: " + helloPacket.getSystemName());
                logger.info("运行时版本: " + helloPacket.getDotnetVersion());
                logger.info("主程序版本: " + helloPacket.getVersion());
                logger.info("连接信息: " + helloPacket.getSessionId() + " (心跳:" + helloPacket.getInterval() + "s)");
                logger.info(">>>服务器信息<<<");
                logger.info("Token: " + getToken());
                logger.info("Server: " + ClientProfile.getServerDescription());
                logger.info("Version: " + ClientProfile.getPluginVersion());
                logger.info("CommandSupported: " + ClientProfile.isCommandSupported());
                logger.info("PapiSupported" + ClientProfile.isPapiSupported());
                logger.info(">>>准备上传<<<");
                logger.info("上报身份中...");

                heartbeatInterval = helloPacket.getInterval();
                sendIdentifyPacket();
                break;
            case IdentifySuccess:
                IdentifySuccessPacket identifySuccessPacket = gson.fromJson(message, IdentifySuccessPacket.class);
                setIdentifySuccessPacket(identifySuccessPacket);
                logger.info("身份验证成功! 服务器名: " + identifySuccessPacket.getServerName());
                logger.info("已连接到主程序!");
                startUpdateSyncSettings();
                startHeartbeat();
                ready = true;
                break;
            case Packet:
                handlePacket(message);
                break;
            case CallBack:
                PacketWithCallBackId packetWithCallBackId = gson.fromJson(message, PacketWithCallBackId.class);
                if (packetWithCallBackId.getCallBackId() != null) {
                    CompletableFuture<String> future = callbackTasks.remove(packetWithCallBackId.getCallBackId());
                    if (future != null) {
                        future.complete(message);
                    }
                }
                break;
        }
    }

    private void handlePacket(String message) {
        Gson gson = getGson();
        PacketWithCallBackId packet = gson.fromJson(message, PacketWithCallBackId.class);
        JsonObject callBack = new JsonObject();
        callBack.addProperty("op", OpCode.CallBack.getValue());
        callBack.addProperty("callback_id", packet.getCallBackId());
        callBack.addProperty("exec_op", packet.getOperation());

        switch (packet.getOperation()) {
            case "GET_SERVER_INFO":
                ServerInfo info = behavior.getInfo();
                GsonUtils.merge(gson, callBack, info);
                break;
            case "UN_BIND_NOTIFY":
                PlayerUnBindNotifyPacket unBindNotifyPacket = gson.fromJson(message, PlayerUnBindNotifyPacket.class);
                behavior.KickPlayer(unBindNotifyPacket.getPlayerName(), unBindNotifyPacket.getKickMessage());
                break;
            case "BIND_SUCCESS_NOTIFY":
                BindSuccessNotifyPacket bindSuccessNotifyPacket = gson.fromJson(message, BindSuccessNotifyPacket.class);
                behavior.BindSuccessBroadcast(bindSuccessNotifyPacket.getPlayerName(), bindSuccessNotifyPacket.getAccountId(), bindSuccessNotifyPacket.getAccountName());
                break;
            case "PLACEHOLDER_API_QUERY":
                PlaceholderApiQueryPacket placeholderApiQueryPacket = gson.fromJson(message, PlaceholderApiQueryPacket.class);
                PlaceholderApiQueryResultPacket papiQueryResultPacket = new PlaceholderApiQueryResultPacket();
                try{
                    String papiQueryResult = behavior.papiQuery(placeholderApiQueryPacket.getPlayerName(), placeholderApiQueryPacket.getText());
                    papiQueryResultPacket.setSuccess(true);
                    papiQueryResultPacket.setText(papiQueryResult);
                }catch (Exception ex){
                    papiQueryResultPacket.setSuccess(false);
                    papiQueryResultPacket.setText(ex.getLocalizedMessage());
                    logger.severe("执行Papi查询命令失败: " + ex);
                }
                GsonUtils.merge(gson, callBack, papiQueryResultPacket);
                break;
            case "RUN_COMMAND":
                RunCommandPacket runCommandPacket = gson.fromJson(message, RunCommandPacket.class);
                RunCommandResultPacket runCommandResultPacket = new RunCommandResultPacket();
                try{
                    String runCommandResult = behavior.runCommand(runCommandPacket.getPlayerName(), runCommandPacket.getCommand(), runCommandPacket.isEnablePapi());
                    runCommandResultPacket.setSuccess(true);
                    runCommandResultPacket.setText(runCommandResult);
                }catch (Exception ex){
                    runCommandResultPacket.setSuccess(false);
                    runCommandResultPacket.setText(ex.getLocalizedMessage());
                    logger.severe("执行命令失败: " + ex);
               }
                GsonUtils.merge(gson, callBack, runCommandResultPacket);
                break;
            case "SEND_TO_CHAT":
                SendToChatPacket sendToChatPacket = gson.fromJson(message, SendToChatPacket.class);
                behavior.SyncToChat(sendToChatPacket.getText());
                break;
            case "SYNC_SETTINGS_UPDATED":
                UpdateSyncSettingsPacket updateSyncSettingsPacket = gson.fromJson(message, UpdateSyncSettingsPacket.class);
                ClientProfile.setSyncMessageMoney(updateSyncSettingsPacket.getSyncMoney());
                ClientProfile.setSyncMessageMode(updateSyncSettingsPacket.getSyncMode());
                break;
            default:
                logger.info("收到未知操作: " + packet.getOperation() + " 请确保你的插件是最新版本????");
                break;
        }

        send(gson.toJson(callBack));
    }

    private void sendIdentifyPacket() {
        Gson gson = getGson();
        IdentifyPacket packet = new IdentifyPacket(getToken());
        packet.setPluginVersion(ClientProfile.getPluginVersion());
        packet.setServerDescription(ClientProfile.getServerDescription());
        send(gson.toJson(packet));
    }

    public PlayerLoginResultPacket login(String playerName, String playerUuid) throws ExecutionException, InterruptedException {
        OnPlayerJoinPacket packet = new OnPlayerJoinPacket();
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setPlayerName(playerName);
        playerInfo.setPlayerUuid(playerUuid);
        packet.setPlayerInfo(playerInfo);
        return sendAndWaitForCallbackAsync(packet, PlayerLoginResultPacket.class).get();
    }

    public void reportPlayer(String playerName, String playerUuid){
        ReportPlayerPacket packet = new ReportPlayerPacket();
        packet.setPlayerName(playerName);
        packet.setPlayerUuid(playerUuid);
        packet.setCallBackId("");
        send(getGson().toJson(packet));
    }

    public StartBindResultPacket startBind(String playerName) throws ExecutionException, InterruptedException {
        StartBindPacket packet = new StartBindPacket();
        packet.setPlayerName(playerName);
        return sendAndWaitForCallbackAsync(packet, StartBindResultPacket.class).get();
    }

    public GetSocialAccountResultPacket getSocialAccount(String playerName) throws ExecutionException, InterruptedException{
        GetSocialAccountPacket packet = new GetSocialAccountPacket();
        packet.setPlayerName(playerName);
        return sendAndWaitForCallbackAsync(packet, GetSocialAccountResultPacket.class).get();
    }

    public GetNewVersionResultPacket getNewVersion() throws ExecutionException, InterruptedException{
        return sendAndWaitForCallbackAsync(new GetNewVersionPacket(), GetNewVersionResultPacket.class).get();
    }

    public GetBindInfoResultPacket getBindInfo(String playerName) throws ExecutionException, InterruptedException{
        GetBindInfoPacket packet = new GetBindInfoPacket();
        packet.setPlayerName(playerName);
        return sendAndWaitForCallbackAsync(packet, GetBindInfoResultPacket.class).get();
    }

    public void syncMessage(PlayerInfoWithRaw playerInfo, String message, boolean useCommand){
        SyncMessagePacket packet = new SyncMessagePacket();
        packet.setPlayer(playerInfo);
        packet.setMessage(message);
        packet.setUseCommand(useCommand);
        packet.setCallBackId("");
        send(getGson().toJson(packet));
    }

    public void syncDeathMessage(PlayerInfoWithRaw playerInfo, String killMessage, String killer){
        SyncDeathMessagePacket packet = new SyncDeathMessagePacket();
        packet.setPlayer(playerInfo);
        packet.setRaw(killMessage);
        packet.setKiller(killer);
        packet.setCallBackId("");
        send(getGson().toJson(packet));
    }

    public void syncEnterExit(PlayerInfoWithRaw playerInfo, boolean isEnter){
        SyncEnterExitMessagePacket packet = new SyncEnterExitMessagePacket();
        packet.setPlayer(playerInfo);
        packet.setEnter(isEnter);
        packet.setCallBackId("");
        send(getGson().toJson(packet));
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.info("连接关闭: " + reason);
        synchronized (connectionLock) {
            isConnected = false; // 当连接关闭时重置状态
        }
        ready = false;
        heartbeatScheduler.shutdownNow();
        reconnect(); // 尝试重连
    }

    @OnWebSocketError
    public void onError(Throwable throwable) {
        logger.severe("连接遇到错误: " + throwable);
        synchronized (connectionLock) {
            isConnected = false; // 当连接关闭时重置状态
        }
        ready = false;
        heartbeatScheduler.shutdownNow();
        reconnect(); // 尝试重连
    }

    private void connect() {
        synchronized (connectionLock) {
            if (isConnected) {
                //logger.info("已经连接到服务器，跳过此次连接请求。");
                return;
            }
            isConnected = true;
        }

        executor.submit(() -> {
            try {
                logger.info("正在连接到服务器: " + uri);
                client.start();
                URI echoUri = new URI(uri);
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                client.connect(this, echoUri, request);
            } catch (Exception e) {
                logger.severe("连接失败: " + e.getMessage());
                synchronized (connectionLock) {
                    isConnected = false; // 连接失败后重置状态
                }
                reconnect(); // 尝试重连
            }
        });
    }

    private void reconnect() {
        try {
            TimeUnit.SECONDS.sleep(5); // 重连前的延迟
            logger.warning("正在尝试重连服务器");
            connect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("重连已终止!");
        }
    }

    public void resetUrl(String newUrl) {
        try {
            logger.info("重置URL: " + newUrl);
            if (session != null) {
                session.close(); // 关闭当前连接
            }
            this.uri = newUrl; // 更新URL
            connect(); // 使用新URL重新连接
        } catch (Exception e) {
            logger.severe("重置URL失败: " + e.getMessage());
        }
    }

    public void startUpdateSyncSettings(){
        NeedSyncSettingsPacket packet = new NeedSyncSettingsPacket();
        packet.setCallBackId("");
        send(getGson().toJson(packet));
    }

    public void close() {
        try {
            if (session != null) {
                session.close();
            }
            timeoutScheduler.shutdown();
            client.stop(); // 停止WebSocketClient
            executor.shutdownNow(); // 关闭线程池
        } catch (Exception e) {
            logger.severe("关闭连接失败: " + e.getMessage());
        }
    }
}
