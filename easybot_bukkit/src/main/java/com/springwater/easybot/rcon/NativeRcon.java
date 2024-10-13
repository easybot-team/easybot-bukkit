package com.springwater.easybot.rcon;

import com.springwater.easybot.Easybot;
import org.glavo.rcon.AuthenticationException;
import org.glavo.rcon.Rcon;

import java.io.IOException;

public class NativeRcon {
    private Rcon rcon;

    public void start() throws AuthenticationException, IOException {
        String address = Easybot.instance.getConfig().getString("adapter.native_rcon.address", "localhost");
        int port = Easybot.instance.getConfig().getInt("adapter.native_rcon.port", 25575);
        String password = Easybot.instance.getConfig().getString("adapter.native_rcon.password", "");
        if(password.equals("")){
            Easybot.instance.getLogger().warning("Rcon密码为空,可能无法连接到服务器!");
        }
        rcon = new Rcon(address, port, password);
    }

    public String executeCommand(String command){
        if(rcon == null) return "Rcon不在线";
        try {
            return rcon.command(command);
        } catch (IOException e) {
            Easybot.instance.getLogger().warning("NativeRCON执行失败: " + e);
            return "NativeRCON执行失败,请查看服务器日志";
        }
    }

    public void close(){
        if(rcon == null) return;
        try {
            rcon.close();
            rcon = null;
        } catch (IOException e) {
        }
    }

}
