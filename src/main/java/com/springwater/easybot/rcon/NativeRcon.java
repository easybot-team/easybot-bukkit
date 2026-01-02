package com.springwater.easybot.rcon;

import com.springwater.easybot.Easybot;
import org.glavo.rcon.AuthenticationException;
import org.glavo.rcon.Rcon;

import java.io.IOException;

public class NativeRcon {
    private Rcon rcon;
    private String address;
    private int port;
    private String password;

    public synchronized void start() {
        this.address = Easybot.instance.getConfig().getString("adapter.native_rcon.address", "localhost");
        this.port = Easybot.instance.getConfig().getInt("adapter.native_rcon.port", 25575);
        this.password = Easybot.instance.getConfig().getString("adapter.native_rcon.password", "");

        if (this.password.isEmpty()) {
            Easybot.instance.getLogger().warning("Rcon密码为空,可能无法连接到服务器!");
        }
        attemptConnection();
    }

    private void attemptConnection() {
        close();

        try {
            rcon = new Rcon(address, port, password);
            Easybot.instance.getLogger().info("Rcon连接成功: " + address + ":" + port);
        } catch (IOException | AuthenticationException e) {
            Easybot.instance.getLogger().warning("Rcon连接建立失败: " + e.getMessage());
            rcon = null;
        }
    }

    public synchronized String executeCommand(String command) {
        if (rcon == null) {
            attemptConnection();
            if (rcon == null) {
                return "Rcon离线，尝试重连失败";
            }
        }
        try {
            return rcon.command(command);
        } catch (IOException e) {
            Easybot.instance.getLogger().warning("Rcon连接丢失，正在尝试自动重连...");
            attemptConnection();
            if (rcon != null) {
                try {
                    return rcon.command(command);
                } catch (IOException ex) {
                    Easybot.instance.getLogger().warning("Rcon重连后执行仍失败: " + ex);
                    return "Rcon重连成功，但命令执行失败: " + ex.getMessage();
                }
            } else {
                return "Rcon连接断开，自动重连失败";
            }
        }
    }

    public synchronized void close() {
        if (rcon == null) return;
        try {
            rcon.close();
        } catch (IOException e) {
            // 忽略关闭时的错误
        } finally {
            rcon = null;
        }
    }
}