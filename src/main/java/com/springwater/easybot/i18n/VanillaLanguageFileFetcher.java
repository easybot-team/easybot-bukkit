package com.springwater.easybot.i18n;

import com.google.gson.Gson;
import com.springwater.easybot.Easybot;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VanillaLanguageFileFetcher {
    private static final Gson gson = new Gson();

    private static Runnable LOAD_COMPLETE = () -> {};
    public static void loadVanillaLanguageAsync(Runnable complete) {
        LOAD_COMPLETE = complete;
        Easybot.EXECUTOR.execute(VanillaLanguageFileFetcher::loadVanillaLanguage);
    }

    private static void loadVanillaLanguage() {
        Path savePath = I18n.WORK_DIR.resolve("vanilla.json");
        String minecraftVersion = Bukkit.getServer().getVersion();
        Easybot.instance.getLogger().info("开始为服务器版本 " + minecraftVersion + " 准备语言文件...");
        if (Files.exists(savePath)) {
            Easybot.instance.getLogger().info("vanilla.json 已存在，跳过下载。");
            LOAD_COMPLETE.run();
            return;
        }

        // 从版本字符串中提取纯净的MC版本号（如 1.20.4）
        String pureVersion = extractMinecraftVersion(minecraftVersion);
        if (pureVersion == null) {
            Easybot.instance.getLogger().warning("无法从服务器版本字符串中解析MC版本号: " + minecraftVersion);
            LOAD_COMPLETE.run();
            return;
        }
        Easybot.instance.getLogger().info("解析到的纯净版本号: " + pureVersion);

        try {
            // 获取版本清单并查找目标版本
            String versionManifestUrl = "https://bmclapi2.bangbang93.com/mc/game/version_manifest_v2.json";
            String versionInfoUrl = fetchVersionInfoUrl(pureVersion, versionManifestUrl);
            if (versionInfoUrl == null) {
                Easybot.instance.getLogger().warning("未在版本清单中找到版本: " + pureVersion);
                LOAD_COMPLETE.run();
                return;
            }

            // 获取版本的详细信息并提取 assetIndex.url
            String assetIndexUrl = fetchAssetIndexUrl(versionInfoUrl);
            if (assetIndexUrl == null) {
                Easybot.instance.getLogger().warning("无法获取版本 " + pureVersion + " 的资源索引地址");
                LOAD_COMPLETE.run();
                return;
            }

            // 从资源索引中获取语言文件的哈希值，优先查找 .json，回退到 .lang
            String langHash = fetchLanguageFileHash(assetIndexUrl);
            boolean isLangFormat = false;

            if (langHash == null) {
                // 未找到 zh_cn.json，尝试查找 zh_cn.lang
                Easybot.instance.getLogger().info("未在资源索引中找到 minecraft/lang/zh_cn.json，尝试查找 minecraft/lang/zh_cn.lang...");
                langHash = fetchLanguageFileHashLegacy(assetIndexUrl);
                if (langHash == null) {
                    Easybot.instance.getLogger().warning("未在资源索引中找到 minecraft/lang/zh_cn.lang");
                    LOAD_COMPLETE.run();
                    return;
                }
                isLangFormat = true;
            }

            // 构造下载地址并下载文件
            String downloadUrl = "https://bmclapi2.bangbang93.com/assets/" + langHash.substring(0, 2) + "/" + langHash;

            if (isLangFormat) {
                // 下载 .lang 文件到临时路径，然后转换为 JSON
                Path tempLangPath = I18n.WORK_DIR.resolve("vanilla.lang.tmp");
                try {
                    downloadFile(downloadUrl, tempLangPath);
                    Easybot.instance.getLogger().info("正在转换 lang -> json...");
                    convertLangToJson(tempLangPath, savePath);
                    Easybot.instance.getLogger().info("语言文件转换完成，保存至: " + savePath.toAbsolutePath());
                } finally {
                    // 清理临时文件
                    Files.deleteIfExists(tempLangPath);
                }
            } else {
                downloadFile(downloadUrl, savePath);
                Easybot.instance.getLogger().info("语言文件下载完成，保存至: " + savePath.toAbsolutePath());
            }

            LOAD_COMPLETE.run();
        } catch (Exception e) {
            Easybot.instance.getLogger().severe("下载语言文件时发生异常: " + e.getMessage());
            Easybot.instance.getLogger().severe(e.toString());
            LOAD_COMPLETE.run();
        }
    }

    /**
     * 从 Bukkit 的版本字符串中提取真正的 Minecraft 版本号。
     * 例如: "git-Paper-123 (MC: 1.20.4)" -> "1.20.4"
     *       "1.20.1" -> "1.20.1"
     */
    private static String extractMinecraftVersion(String versionString) {
        // 匹配 MC: x.x.x 或直接数字版本号
        Pattern pattern = Pattern.compile("(?:MC:\\s*)?(\\d+\\.\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(versionString);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 从版本清单中获取指定版本的 client.json 下载地址。
     */
    private static String fetchVersionInfoUrl(String version, String manifestUrl) throws IOException {
        String json = httpGet(manifestUrl);
        Map<?, ?> manifest = gson.fromJson(json, Map.class);
        Object versionsObj = manifest.get("versions");
        if (!(versionsObj instanceof Iterable)) {
            return null;
        }
        for (Object entryObj : (Iterable<?>) versionsObj) {
            Map<?, ?> entry = (Map<?, ?>) entryObj;
            String id = (String) entry.get("id");
            if (version.equals(id)) {
                return (String) entry.get("url");
            }
        }
        return null;
    }

    /**
     * 从 client.json 中提取 assetIndex.url。
     */
    private static String fetchAssetIndexUrl(String versionInfoUrl) throws IOException {
        String json = httpGet(versionInfoUrl);
        Map<?, ?> versionInfo = gson.fromJson(json, Map.class);
        Object assetIndexObj = versionInfo.get("assetIndex");
        if (assetIndexObj instanceof Map) {
            return (String) ((Map<?, ?>) assetIndexObj).get("url");
        }
        return null;
    }

    /**
     * 从资源索引中获取 zh_cn.json 的哈希值。
     */
    private static String fetchLanguageFileHash(String assetIndexUrl) throws IOException {
        String json = httpGet(assetIndexUrl);
        Map<?, ?> assetIndex = gson.fromJson(json, Map.class);
        Object objectsObj = assetIndex.get("objects");
        if (!(objectsObj instanceof Map)) {
            return null;
        }
        Map<?, ?> objects = (Map<?, ?>) objectsObj;
        Object langEntryObj = objects.get("minecraft/lang/zh_cn.json");
        if (langEntryObj instanceof Map) {
            return (String) ((Map<?, ?>) langEntryObj).get("hash");
        }
        return null;
    }

    /**
     * 从资源索引中获取 zh_cn.lang 的哈希值（旧版 Minecraft 格式）。
     */
    private static String fetchLanguageFileHashLegacy(String assetIndexUrl) throws IOException {
        String json = httpGet(assetIndexUrl);
        Map<?, ?> assetIndex = gson.fromJson(json, Map.class);
        Object objectsObj = assetIndex.get("objects");
        if (!(objectsObj instanceof Map)) {
            return null;
        }
        Map<?, ?> objects = (Map<?, ?>) objectsObj;
        Object langEntryObj = objects.get("minecraft/lang/zh_cn.lang");
        if (langEntryObj instanceof Map) {
            return (String) ((Map<?, ?>) langEntryObj).get("hash");
        }
        return null;
    }

    /**
     * 将 Minecraft .lang 格式文件转换为 .json 格式并保存。
     * .lang 格式: 每行一个 key=value 对，# 开头的行为注释，空行跳过。
     * .json 格式: {"key": "value", ...}
     */
    private static void convertLangToJson(Path langPath, Path jsonPath) throws IOException {
        Map<String, String> langMap = new LinkedHashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(langPath, java.nio.charset.StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 跳过空行和注释行
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIndex = line.indexOf('=');
                if (eqIndex > 0) {
                    String key = line.substring(0, eqIndex);
                    String value = line.substring(eqIndex + 1);
                    langMap.put(key, value);
                }
            }
        }

        Files.createDirectories(jsonPath.getParent());
        try (Writer writer = Files.newBufferedWriter(jsonPath, java.nio.charset.StandardCharsets.UTF_8)) {
            gson.toJson(langMap, writer);
        }
    }

    /**
     * 通过 HTTP GET 请求获取响应内容（UTF-8字符串）。
     */
    private static String httpGet(String urlString) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "EasyBot/1.0 (Minecraft Plugin)");

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + responseCode + " while fetching " + urlString);
            }

            try (InputStream is = conn.getInputStream();
                 InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 从指定 URL 下载文件并保存到本地路径。
     */
    private static void downloadFile(String urlString, Path destination) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent", "EasyBot/1.0 (Minecraft Plugin)");

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("下载失败，HTTP " + responseCode + " from " + urlString);
            }

            Files.createDirectories(destination.getParent());
            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
