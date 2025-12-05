package com.springwater.easybot.papi;

import com.springwater.easybot.utils.StatisticsParser;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OfflineStatisticExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "ez-statistic";
    }

    @Override
    public @NotNull String getAuthor() {
        return "EasyBot - MiuxuE";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";
        String statisticString = StatisticsParser.getStatistic(params);
        if (statisticString == null) return "";
        Statistic statistic = Statistic.valueOf(statisticString);

        String itemString = StatisticsParser.getItem(params);
        String entityString = StatisticsParser.getEntity(params);

        if (itemString != null) {
            return String.valueOf(player.getStatistic(statistic, Material.valueOf(itemString)));
        } else if (entityString != null) {
            return String.valueOf(player.getStatistic(statistic, EntityType.valueOf(entityString)));
        }
        return String.valueOf(player.getStatistic(statistic));
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        return super.onPlaceholderRequest(player, params);
    }
}
