package com.springwater.easybot.api;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.rcon.NativeRcon;
import com.springwater.easybot.utils.BukkitUtils;
import com.springwater.easybot.utils.ReflectionUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.glavo.rcon.AuthenticationException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class CommandApi {
    private Object dedicatedServer;
    private NativeRcon nativeRcon;

    public CommandApi() throws IllegalAccessException {
        FileConfiguration config = Easybot.instance.getConfig();
        boolean useNativeRcon = config.getBoolean("adapter.native_rcon.use_native_rcon", false);
        if (useNativeRcon) {
            nativeRcon = new NativeRcon();
            Easybot.instance.getLogger().info("命令接口初始化成功 [原生RCON接口]");
            startNativeRcon(); 
            return;
        }

        if(BukkitUtils.canCreateCommandSender()){
            Easybot.instance.getLogger().info("找到高版本命令接口,将优先使用此接口!");
        }

        Server server = Bukkit.getServer();
        this.dedicatedServer = ReflectionUtils.findFieldByType(server, "DedicatedServer");
        if (dedicatedServer == null) {
            this.dedicatedServer = ReflectionUtils.findFieldByType(server, "MinecraftServer");
            if (dedicatedServer == null) throw new RuntimeException("Can not find dedicatedServer field");
            Easybot.instance.getLogger().info("命令接口初始化成功 [旧版服务器方案]");
        } else {
            Easybot.instance.getLogger().info("命令接口初始化成功 [新版服务器方案]");
        }
    }

    private static Class<?> getRconConsoleSourceClassPath() throws ClassNotFoundException {
        // 捏🐎的 为什么这东西还有两种名字
        // 不同版本可能有不同的别名,是不是还有混淆之后的名字啊!?! 啊?
        try {
            return Class.forName("net.minecraft.server.rcon.RconConsoleSource");
        } catch (ClassNotFoundException ignored) {
        }

        try {
            return Class.forName("net.minecraft.server.rcon.RemoteControlCommandListener");
        } catch (ClassNotFoundException ignored) {
        }

        throw new ClassNotFoundException("Can not find RconConsoleSource class path");
    }

    public void startNativeRcon() {
        new Thread(() -> {
            try {
                nativeRcon.start();
            } catch (Exception ignored) {
            }
        }).start();
    }

    public void closeNativeRcon() {
        if (nativeRcon == null) {
            return;
        }
        nativeRcon.close();
    }

    public void runCommandAsConsole(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }


    // 本质上是冒充RCON执行命令
    public String runCommand(String command) {
        if (nativeRcon != null) {
            return nativeRcon.executeCommand(command);
        }

        try {
            if (BukkitUtils.canCreateCommandSender()) {
                List<String> messages = new ArrayList<>();
                CommandSender sender = Bukkit.createCommandSender(feedback -> {
                    messages.add(
                            LegacyComponentSerializer.legacy('§').serialize(feedback)
                    );
                });
                Easybot.instance.runTask(() -> Bukkit.dispatchCommand(sender, command));
                Thread.sleep(500);
                return String.join("\n", messages);
            }
        } catch (Exception ignored) {

        }

        try {
            Method method = dedicatedServer.getClass().getMethod("runCommand", String.class);
            return (String) method.invoke(dedicatedServer, command);
        } catch (UnsupportedOperationException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException ignored) {
            // UnsupportedOperationException的情况:
            // Not supported - remote source required.

            /*
                @Override // net.minecraft.server.ServerInterface
                public String runCommand(String command) {
                    throw new UnsupportedOperationException("Not supported - remote source required.");
                }
             */
        }

        try {
            // 获取 dedicatedServer 的类和方法
            Class<?> serverClass = dedicatedServer.getClass();
            Method runCommandMethod = serverClass.getMethod("runCommand",
                    getRconConsoleSourceClassPath(),
                    String.class);

            // 使用反射获取 RconConsoleSource 构造函数
            Class<?> rconConsoleSourceClass = getRconConsoleSourceClassPath();
            Constructor<?> rconConsoleSourceConstructor = rconConsoleSourceClass.getConstructors()[0];

            // 通过反射创建 RconConsoleSource 实例
            Object rconConsoleSource = rconConsoleSourceConstructor.newInstance(
                    dedicatedServer,
                    InetSocketAddress.createUnresolved("", 0));

            // 通过反射调用 runCommand 方法
            return (String) runCommandMethod.invoke(dedicatedServer, rconConsoleSource, command);
        } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException | InvocationTargetException ignored) {
        }


        try {
            Method method = dedicatedServer.getClass().getMethod("executeRemoteCommand", String.class);
            return (String) method.invoke(dedicatedServer, command);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }

        throw new RuntimeException("Can not find runCommand method");
    }
}
