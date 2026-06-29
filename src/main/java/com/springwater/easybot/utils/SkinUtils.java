package com.springwater.easybot.utils;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.bridge.model.PlayerSkin;
import me.clip.placeholderapi.PlaceholderAPI;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinProperty;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Optional;

public class SkinUtils {
    public static String getSkin(Player player) {
        try {
            if (ClientProfile.isHasSkinsRestorer() && ClientProfile.isPapiSupported()) {
                String skin = PlaceholderAPI.setPlaceholders(player, "%skinsrestorer_texture_url_or_steve%");
                if (!skin.equalsIgnoreCase("error") && !skin.equalsIgnoreCase("")) {
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

    public static @Nullable PlayerSkin getSkinOrNull(Player player) {
        try {
            if (ClientProfile.isHasSkinsRestorer() && ClientProfile.isPapiSupported()) {
                PlayerSkin skinWithSkinsRestorer = new PlayerSkin();
                String skin = PlaceholderAPI.setPlaceholders(player, "%skinsrestorer_texture_url_or_empty%");
                if (!skin.equalsIgnoreCase("error") && !skin.equalsIgnoreCase("")) {
                    skinWithSkinsRestorer.setSkinUrl(skin);
                }
                if (ClientProfile.isHasPaperSkinApi()) {
                    URL cape = player.getPlayerProfile().getTextures().getCape();
                    if (cape != null) {
                        skinWithSkinsRestorer.setCapeUrl("https://textures.minecraft.net/" + cape.getPath());
                    }
                }
                return skinWithSkinsRestorer;
            } else if (ClientProfile.isHasPaperSkinApi()) {
                PlayerSkin skinWithPaperSkinApi = new PlayerSkin();
                URL skin = player.getPlayerProfile().getTextures().getSkin();
                if (skin == null) return null;
                skinWithPaperSkinApi.setSkinUrl("https://textures.minecraft.net/" + skin.getPath());
                URL cape = player.getPlayerProfile().getTextures().getCape();
                if (cape != null) {
                    skinWithPaperSkinApi.setCapeUrl("https://textures.minecraft.net/" + cape.getPath());
                }
                return skinWithPaperSkinApi;
            }
        } catch (Exception ex) {
            Easybot.instance.getLogger().severe("处理玩家皮肤信息遇到异常! " + ex);
        }
        return null;
    }
}
