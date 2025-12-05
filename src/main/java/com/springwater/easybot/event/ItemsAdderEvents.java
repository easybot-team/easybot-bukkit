package com.springwater.easybot.event;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.utils.ItemsAdderUtils;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderEvents implements Listener {
    @EventHandler
    public void onLoaded(ItemsAdderLoadDataEvent event){
        if(ItemsAdderUtils.isQFacesInstalled()){
            Easybot.instance.getLogger().info("\u001B[32m※ 检测到ItemsAdder QFaces表情资源包！\u001B[0m");
            ClientProfile.setHasQFaces(true);
        }else{
            Easybot.instance.getLogger().info("\u001B[31m※ 检查到可用扩展: IA资源包QFaces现已上线,支持在消息同步时渲染QQ表情! \u001B[0m");
            ClientProfile.setHasQFaces(false);
        }
    }
}
