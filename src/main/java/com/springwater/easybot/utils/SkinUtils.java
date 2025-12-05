package com.springwater.easybot.utils;

import com.springwater.easybot.bridge.ClientProfile;
import me.clip.placeholderapi.PlaceholderAPI;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinProperty;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import java.net.URL;
import java.util.Optional;

public class SkinUtils {
    public static String getSkin(Player player) {
        try {
            if (ClientProfile.isHasSkinsRestorer() && ClientProfile.isPapiSupported()) {
                String skin = PlaceholderAPI.setPlaceholders(player, "%skinsrestorer_texture_url_or_steve%");
                if(!skin.equalsIgnoreCase("error") && !skin.equalsIgnoreCase("")){
                    return skin;
                }
            }
            if (ClientProfile.isHasPaperSkinApi()) {
                URL skin = player.getPlayerProfile().getTextures().getSkin();
                if (skin == null) return "";
                return "https://textures.minecraft.net/" + skin.getPath();
            }
            return "https://mc-heads.net/skin/" + player.getUniqueId();
        } catch (Exception ignored) {
            return "";
        }
    }
}
