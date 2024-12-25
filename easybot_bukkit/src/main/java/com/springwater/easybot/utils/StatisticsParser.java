package com.springwater.easybot.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatisticsParser {
    // 正则表达式用于匹配统计类型，不区分大小写
    private static final Pattern STATISTIC_PATTERN = Pattern.compile("^(\\w+)(?:\\$[^&=]+=[^&]+)*$", Pattern.CASE_INSENSITIVE);

    // 正则表达式用于匹配物品名称，不区分大小写
    private static final Pattern ITEM_PATTERN = Pattern.compile("(?i)\\$i=([^$&]+)");

    // 正则表达式用于匹配实体名称，不区分大小写
    private static final Pattern ENTITY_PATTERN = Pattern.compile("(?i)\\$e=([^$&]+)");

    /**
     * 获取统计类型名称。
     */
    public static String getStatistic(String content) {
        Matcher matcher = STATISTIC_PATTERN.matcher(content);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return content; // 如果不匹配，直接返回原始内容
    }

    /**
     * 获取物品名称。
     */
    public static String getItem(String content) {
        Matcher matcher = ITEM_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null; // 如果没有找到物品信息，返回 null
    }

    /**
     * 获取实体名称。
     */
    public static String getEntity(String content) {
        Matcher matcher = ENTITY_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null; // 如果没有找到实体信息，返回 null
    }
}
