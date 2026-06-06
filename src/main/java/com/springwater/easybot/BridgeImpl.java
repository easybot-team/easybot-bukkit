package com.springwater.easybot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.springwater.easybot.bridge.BridgeBehavior;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.bridge.message.*;
import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.model.PlayerSkin;
import com.springwater.easybot.bridge.model.ServerInfo;
import com.springwater.easybot.bridge.packet.NbtDataTypeEnum;
import com.springwater.easybot.utils.*;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTType;
import de.tr7zw.nbtapi.iface.*;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BridgeImpl implements BridgeBehavior {
    private final Logger logger = Logger.getLogger("EasyBotImpl");

    @Override
    public String runCommand(String playerName, String command, boolean enablePapi) {
        if (!ClientProfile.isCommandSupported()) {
            logger.warning("无法执行命令: 此服务端不支持执行命令,请检查EasyBot之前输出的日志以找到原因!");
            return "无法执行命令: 此服务端不支持执行命令,请检查服务端EasyBot插件之前输出的日志以找到原因!";
        }

        if (!ClientProfile.isPapiSupported() && enablePapi) {
            logger.warning("无法执行EasyBot主程序传来的命令,服务器未安装PlaceholderApi!");
            return "无法执行命令: 服务器未安装PlaceholderApi!";
        }

        if (enablePapi) {
            OfflinePlayer player = null;
            if (playerName != null && !playerName.equals("")) {
                player = Bukkit.getOfflinePlayer(playerName);
            }
            command = PlaceholderAPI.setPlaceholders(player, command);
        }
        return Easybot.getCommandApi().runCommand(command);
    }

    @Override
    public String papiQuery(String playerName, String query) {
        OfflinePlayer player = null;
        if (playerName != null && !playerName.equals("")) {
            player = Bukkit.getOfflinePlayer(playerName);
        }
        return PlaceholderAPI.setPlaceholders(player, query);
    }

    @Override
    public ServerInfo getInfo() {
        ServerInfo info = new ServerInfo();
        info.setServerName(Bukkit.getName());
        info.setPluginVersion(Easybot.instance.getDescription().getVersion());
        info.setServerVersion(Bukkit.getBukkitVersion());
        info.setCommandSupported(ClientProfile.isCommandSupported());
        info.setPapiSupported(ClientProfile.isPapiSupported());
        info.setHasGeyser(ClientProfile.isHasGeyser());
        info.setOnlineMode(ClientProfile.isOnlineMode());
        return info;
    }

    @Override
    public void SyncToChat(String message) {
        logger.info(message);
        Easybot.instance.runTask(() -> Bukkit.getOnlinePlayers().forEach(x -> x.sendMessage(message)));
    }

    @Override
    public void BindSuccessBroadcast(String playerName, String accountId, String accountName) {
        Easybot.instance.runTask(() -> {
            Player onlinePlayer = Bukkit.getPlayer(playerName);
            if (onlinePlayer != null) {
                String message = Easybot.instance.getConfig().getString("message.bind_success", "§f[§a!§f] 绑定§f §a#account §f(§a#name§f) 成功!").replace("&", "§").replace("#account", accountId).replace("#name", accountName);
                onlinePlayer.sendMessage(message);
            }

            if (Easybot.instance.getConfig().getBoolean("event.enable_success_event", false)) {
                if (!ClientProfile.isCommandSupported()) {
                    logger.warning("无法在玩家绑定成功后执行命令: 此服务端不支持执行命令,请检查EasyBot之前输出的日志以找到原因!");
                } else {
                    List<String> commands = Easybot.instance.getConfig().getStringList("event.bind_success");
                    for (String command : commands) {
                        command = command.replace("&", "§").replace("$player", playerName).replace("$account", accountId).replace("$name", accountName);
                        Easybot.getCommandApi().runCommandAsConsole(command);
                    }
                }
            }
        });
    }

    @Override
    public void KickPlayer(String player, String kickMessage) {
        Easybot.instance.runTask(() -> {
            Player kickPlayer = Bukkit.getPlayer(player);
            if (kickPlayer != null) {
                kickPlayer.kickPlayer(kickMessage);
            }
        });
    }

    @Override
    public void SyncToChatExtra(List<Segment> segments, String text) {
        if (!ChatCompatUtil.hasAppendMethod()) {
            Easybot.instance.runTask(() -> Bukkit.getOnlinePlayers().forEach(x -> x.sendMessage(text)));
            return;
        }
        try {
            ComponentBuilder builder = new ComponentBuilder("");

            Queue<Segment> queue = new LinkedList<>(segments);
            StringBuilder currentText = new StringBuilder();  // 使用StringBuilder合并文本
            List<Segment> segmentsToAdd = new ArrayList<>();

            while (!queue.isEmpty()) {
                Segment segment = queue.poll();
                if (segment instanceof TextSegment) {
                    currentText.append(segment.getText());  // 直接用StringBuilder追加文本
                } else {
                    if (currentText.length() > 0) {  // 如果有文本累积，则添加合并的TextSegment
                        TextSegment combinedTextSegment = new TextSegment();
                        combinedTextSegment.setText(currentText.toString());
                        segmentsToAdd.add(combinedTextSegment);
                        currentText.setLength(0);  // 重置StringBuilder
                    }
                    segmentsToAdd.add(segment);  // 直接添加非TextSegment的部分
                }
            }

            if (currentText.length() > 0) {
                TextSegment combinedTextSegment = new TextSegment();
                combinedTextSegment.setText(currentText.toString());
                segmentsToAdd.add(combinedTextSegment);
            }

            for (Segment segment : segmentsToAdd) {
                builder.append(toComponent(segment));
            }


            List<String> atPlayerNames = segments.stream().filter(x -> x instanceof AtSegment).flatMap(seg -> Arrays.stream(((AtSegment) seg).getAtPlayerNames())).collect(Collectors.toList());

            Easybot.instance.runTask(() -> Bukkit.getOnlinePlayers().forEach(p -> {
                // 判断玩家名字是否在atPlayerNames中,忽略大小写
                boolean hasAt = atPlayerNames.stream().anyMatch(x -> x.equalsIgnoreCase(GeyserUtils.getNameByPlayer(p)));
                if (!hasAt && Easybot.instance.getConfig().getBoolean("event.on_at.find", true)) {
                    hasAt = text.contains(GeyserUtils.getNameByPlayer(p));
                }

                if (hasAt && Easybot.instance.getConfig().getBoolean("event.on_at.enable", true)) {
                    AtEventUtils.at(p.getPlayer());
                }

                try {
                    try {
                        p.spigot().sendMessage(builder.create());
                    } catch (Exception ignored) {
                        p.sendMessage(builder.create());
                    }
                } catch (Exception ex) {
                    logger.warning(ex.getMessage());
                    logger.warning("将群内消息转换为Minecraft格式消息时遇到错误,将向玩家发送原始信息!");
                    Easybot.instance.runTask(() -> Bukkit.getOnlinePlayers().forEach(x -> x.sendMessage(text)));
                }
            }));
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
            logger.warning("将群内消息转换为Minecraft格式消息时遇到错误,将向玩家发送原始信息!");
            Easybot.instance.runTask(() -> Bukkit.getOnlinePlayers().forEach(x -> x.sendMessage(text)));
        }
    }

    @Override
    public boolean moduleIsInstalled(String name) {
        return Bukkit.getPluginManager().getPlugin(name) != null;
    }

    @Override
    public boolean moduleIsEnabled(String name) {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    @Override
    public boolean isAuthenticated(String name) {
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<Boolean> inner = new CompletableFuture<>();
            Easybot.instance.runTask(() -> {
                try {
                    Player player = Bukkit.getPlayer(name);
                    if (player == null) {
                        inner.complete(true); // true表示不需要登录
                        return;
                    }

                    if (AuthMeUtils.isAuthMeInstalled()) {
                        inner.complete(AuthMeUtils.isPlayerAuthenticated(player));
                    } else if (LibreLoginUtils.isLibreLoginInstalled()) {
                        inner.complete(LibreLoginUtils.isAuthenticated(player));
                    } else {
                        inner.complete(true);
                    }
                } catch (Exception e) {
                    inner.completeExceptionally(e);
                }
            });
            return inner;
        }).thenCompose(f -> f).join();
    }

    @Override
    public @Nullable JsonObject ReadNbtData(String playerUuid, NbtDataTypeEnum nbtDataTypeEnum) {
        if (nbtDataTypeEnum != NbtDataTypeEnum.PlayerData) {
            return null;
        }

        Player player = Bukkit.getPlayer(UUID.fromString(playerUuid));
        if(player != null) {
            // 异步任务，返回nbt
            Future<ReadableNBT> future = CompletableFuture.supplyAsync(() -> {
                CompletableFuture<ReadableNBT> nbtFuture = new CompletableFuture<>();

                Easybot.instance.runTask(() -> NBT.get(player, nbt -> {
                    ReadWriteNBT newNbt = NBT.createNBTObject();
                    newNbt.mergeCompound(nbt);
                    nbtFuture.complete(newNbt);
                }));

                try {
                    // 等待NBT读取完成，设置超时防止卡顿
                    return nbtFuture.get(5, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                    return null;
                }
            });

            try {
                ReadableNBT nbt = future.get(); // 等待异步任务完成

                if(nbt != null) {
                    JsonObject root = new JsonObject();
                    convertNbtToJson(nbt, root);
                    return root;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }


        World mainWorld = null;
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                mainWorld = world;
                break;
            }
        }
        if (mainWorld == null) {
            return null; // 没有主世界，无法定位玩家数据文件
        }

        File worldFolder = getWorldFolder(mainWorld);
        if (worldFolder == null || !worldFolder.exists()) {
            return null;
        }

        // 3. 确定玩家数据文件夹（优先匹配新版本路径 players，否则回退 playerdata）
        File dataFile = locatePlayerDataFile(worldFolder, playerUuid);
        if (dataFile == null) {
            return null;
        }

        // 4. 使用 NBT-API 读取文件并转换为 JsonObject
        try {
            ReadWriteNBT nbt = NBT.readFile(dataFile);
            JsonObject root = new JsonObject();
            convertNbtToJson(nbt, root);
            return root;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void convertNbtToJson(ReadableNBT nbtCompound, JsonObject jsonObject) {
        for (String key : nbtCompound.getKeys()) {
            NBTType type = nbtCompound.getType(key);
            switch (type) {
                case NBTTagByte:
                    jsonObject.addProperty(key, nbtCompound.getByte(key));
                    break;
                case NBTTagShort:
                    jsonObject.addProperty(key, nbtCompound.getShort(key));
                    break;
                case NBTTagInt:
                    jsonObject.addProperty(key, nbtCompound.getInteger(key));
                    break;
                case NBTTagLong:
                    jsonObject.addProperty(key, nbtCompound.getLong(key));
                    break;
                case NBTTagFloat:
                    addFloatProperty(jsonObject, key, nbtCompound.getFloat(key));
                    break;
                case NBTTagDouble:
                    addDoubleProperty(jsonObject, key, nbtCompound.getDouble(key));
                    break;
                case NBTTagString:
                    jsonObject.addProperty(key, nbtCompound.getString(key));
                    break;
                case NBTTagCompound:
                    JsonObject nestedJson = new JsonObject();
                    convertNbtToJson(Objects.requireNonNull(nbtCompound.getCompound(key)), nestedJson);
                    jsonObject.add(key, nestedJson);
                    break;
                case NBTTagList:
                    JsonArray jsonArray = new JsonArray();
                    ReadableNBTList<ReadWriteNBT> nbtList = nbtCompound.getCompoundList(key);
                    for (int i = 0; i < nbtList.size(); i++) {
                        Object value = nbtList.get(i);
                        if (value instanceof NBTCompound) {
                            JsonObject arrayItemJson = new JsonObject();
                            convertNbtToJson((NBTCompound) value, arrayItemJson);
                            jsonArray.add(arrayItemJson);
                        } else if (value instanceof String) {
                            jsonArray.add((String) value);
                        } else if (value instanceof Number) {
                            addNumberToArray(jsonArray, (Number) value);
                        }
                    }
                    jsonObject.add(key, jsonArray);
                    break;
                default:
                    break;
            }
        }
    }

    private static void addFloatProperty(JsonObject obj, String key, float val) {
        obj.addProperty(key, sanitizeFloat(val));
    }

    private static void addDoubleProperty(JsonObject obj, String key, double val) {
        obj.addProperty(key, sanitizeDouble(val));
    }

    private static void addNumberToArray(JsonArray array, Number val) {
        if (val instanceof Float) {
            array.add(sanitizeFloat((Float) val));
        } else if (val instanceof Double) {
            array.add(sanitizeDouble((Double) val));
        } else {
            // 整数类型直接添加（Long/Integer/Short/Byte 不会出现 Infinity/NaN）
            double d = val.doubleValue();
            if (Double.isInfinite(d) || Double.isNaN(d)) {
                // 理论不会走到，以防万一转成 0
                array.add(0);
            } else {
                array.add(val);
            }
        }
    }

    /** 将 Float 特殊值替换为合法数值 */
    private static float sanitizeFloat(float f) {
        if (Float.isNaN(f)) {
            return 0f;
        }
        if (Float.isInfinite(f)) {
            return f > 0 ? Float.MAX_VALUE : -Float.MAX_VALUE;
        }
        return f;
    }

    /** 将 Double 特殊值替换为合法数值 */
    private static double sanitizeDouble(double d) {
        if (Double.isNaN(d)) {
            return 0.0;
        }
        if (Double.isInfinite(d)) {
            return d > 0 ? Double.MAX_VALUE : -Double.MAX_VALUE;
        }
        return d;
    }
    private File getWorldFolder(World world) {
        try {
            File container = Bukkit.getWorldContainer();
            if(container.getPath().contains(world.getName())) return container; // 哈哈,旧版本是"./world",新版本却是".",那我问我,为什么不写死,对啊!我为什么不写死啊?
            return new File(container, world.getName());
        } catch (NoSuchMethodError error) {
            return new File(".", world.getName());
        }
    }
    
    private File locatePlayerDataFile(File worldFolder, String playerUuid) {
        // 检查新版路径
        File playersDir = new File(worldFolder, "players");
        if (playersDir.isDirectory()) {
            File dataDir = new File(playersDir, "data");
            if(dataDir.isDirectory()) {
                File file = new File(dataDir, playerUuid + ".dat");
                if (file.isFile()) {
                    return file;
                }
            }
        }

        // 检查旧版路径
        File playerdataDir = new File(worldFolder, "playerdata");
        if (playerdataDir.isDirectory()) {
            File file = new File(playerdataDir, playerUuid + ".dat");
            if (file.isFile()) {
                return file;
            }
        }

        return null; // 均未找到 (那很神秘了
    }


    @Override
    public List<PlayerInfo> getPlayerList() {
        return Bukkit.getOnlinePlayers().stream().filter(FakePlayerUtils::isNotFake).map(x -> {
            PlayerInfo info = new PlayerInfo();
            info.setPlayerName(GeyserUtils.getNameByPlayer(x));
            info.setPlayerUuid(GeyserUtils.getUuid(x.getUniqueId()).toString());
            info.setIp(BridgeUtils.getPlayerIp(x));
            info.setBedrock(GeyserUtils.isBedrock(x));
            info.setSkinUrl(SkinUtils.getSkin(x));
            return info;
        }).collect(Collectors.toList());
    }

    @Override
    public @Nullable PlayerSkin getPlayerSkin(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) return null;
        return SkinUtils.getSkinOrNull(player);
    }


    private BaseComponent toComponent(Segment segment) {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(segment.getText()));
        if (segment instanceof AtSegment) {
            component.setColor(ChatColor.GOLD);
            String[] atPlayerNames = ((AtSegment) segment).getAtPlayerNames();
            if (!Objects.equals(((AtSegment) segment).getAtUserId(), "0")) {
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").append("@").append(((AtSegment) segment).getAtUserName()).append(" (").append(((AtSegment) segment).getAtUserId()).append(")").append(atPlayerNames.length > 1 ? new TextComponent("\n该玩家绑定了" + atPlayerNames.length + "个账号\n" + String.join(",", atPlayerNames)) : new TextComponent("")).create()));
            }
        } else if (segment instanceof ImageSegment) {
            component.setColor(ChatColor.GREEN);

            if (Easybot.instance.getConfig().getBoolean("sync.chat_image_support", true)) {
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("[[CICode,url=" + ((ImageSegment) segment).getUrl() + ",name=" + ((ImageSegment) segment).getSummary() + "]]").create()));
            } else {
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7§n点击预览 ").append(new TextComponent("§7§n" + ((ImageSegment) segment).getUrl())).create()));
            }

            component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ((ImageSegment) segment).getUrl()));
        } else if (segment instanceof FileSegment) {
            component.setColor(ChatColor.GOLD);
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7§n点击下载 ").append(new TextComponent("§7§n" + ((FileSegment) segment).getFileUrl())).create()));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ((FileSegment) segment).getFileUrl()));

        } else if (segment instanceof FaceSegment) {
            component.setColor(ChatColor.GREEN);
            if (ClientProfile.isHasItemsAdder() && ClientProfile.isHasQFaces()) {
                String qface = ItemsAdderUtils.getFace(Integer.parseInt(((FaceSegment) segment).getId()));
                if (qface != null) {
                    component = new TextComponent(qface);
                    component.setColor(ChatColor.WHITE);
                }
            }
        } else {
            component.setColor(ChatColor.WHITE);
        }
        return component;
    }
}
