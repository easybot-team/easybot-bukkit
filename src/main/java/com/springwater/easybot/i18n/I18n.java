package com.springwater.easybot.i18n;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.springwater.easybot.Easybot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class I18n {
    // {work_dir}/lang
    public static Path WORK_DIR = Easybot.instance.getDataFolder().toPath().resolve("lang").normalize();
    private static final Gson GSON = new Gson();
    private static final EasyBotTranslator TRANSLATOR = new EasyBotTranslator();
    public static TranslatableComponentRenderer<Locale> TRANSLATABLE_COMPONENT_RENDERER = TranslatableComponentRenderer.usingTranslationSource(TRANSLATOR);

    // 创建文件
    public static void EnsureDirectory() {
        if (!WORK_DIR.toFile().exists()) {
            boolean created = WORK_DIR.toFile().mkdirs();
            if (!created) {
                Easybot.instance.getLogger().warning("创建语言存放目录失败: " + WORK_DIR);
            }
        }
    }

    public static String convertMinecraftToMessageFormat(String minecraftFormat) {
        // Minecraft 语言文件里的 { } ' 都是普通字面量，但它们同时是 MessageFormat 的语法字符。
        // 直接透传会抛出 "Unmatched braces in the pattern."
        // （例如 26.3 的 vanilla.json 中有 "key.keyboard.keypad.left.brace": "小键盘 {"），
        // 单个条目异常会导致整个语言文件加载失败，所以先按 MessageFormat 规则转义，再替换占位符。
        String source = minecraftFormat
                .replace("'", "''")
                .replace("{", "'{'")
                .replace("}", "'}'");

        // 匹配 %[argument_index$][flags][width][.precision]conversion
        // 这里我们只关心最常见的 %s 和 %d，并支持可选的 n$
        Pattern pattern = Pattern.compile("%(?:(\\d+)\\$)?([ds])");
        Matcher matcher = pattern.matcher(source);
        StringBuffer sb = new StringBuffer();

        // 用于处理不带索引的连续占位符（如多个 %s）
        int autoIndex = 0;
        while (matcher.find()) {
            String indexStr = matcher.group(1);
            String conversion = matcher.group(2);
            int index;
            if (indexStr != null) {
                // 显式索引，Minecraft 从 1 开始，MessageFormat 从 0 开始
                index = Integer.parseInt(indexStr) - 1;
            } else {
                // 没有索引，按出现顺序分配
                index = autoIndex++;
            }

            String replacement;
            if ("d".equals(conversion)) {
                // 整数，可以保留数字类型
                replacement = "{" + index + ",number,integer}";
            } else {
                // %s 统一视为普通参数
                replacement = "{" + index + "}";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        // 处理双百分号转义 (%% → %)
        return sb.toString().replace("%%", "%");
    }

    public static void LoadLanguagesAsync() {
        Easybot.EXECUTOR.submit(I18n::LoadCustomLanguages);
    }

    private static void LoadCustomLanguages() {
        Easybot.instance.getLogger().info("开始加载语言...");
        Type mapType = new TypeToken<Map<String, String>>() {
        }.getType();

        File[] langFiles = WORK_DIR.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        if (langFiles == null || langFiles.length == 0) {
            Easybot.instance.getLogger().warning("没有找到语言文件");
            return;
        }

        TRANSLATOR.clearRegistry();
        
        for (File file : langFiles) {
            String fileName = file.getName();
            String namespace;
            if (fileName.equals("vanilla.json"))
                namespace = "minecraft";
            else
                namespace = fileName.substring(0, fileName.length() - 5);
            try (Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
                Map<String, String> translations = GSON.fromJson(reader, mapType);
                if (translations != null && !translations.isEmpty()) {
                    int loaded = 0;
                    int skipped = 0;
                    for (Map.Entry<String, String> entry : translations.entrySet()) {
                        try {
                            TRANSLATOR.register(
                                    entry.getKey(),
                                    Locale.CHINESE,
                                    new MessageFormat(
                                            convertMinecraftToMessageFormat(entry.getValue()),
                                            Locale.CHINESE
                                    )
                            );
                            loaded++;
                        } catch (Exception ex) {
                            // 单条翻译格式异常不应导致整个语言文件加载失败
                            skipped++;
                        }
                    }
                    Easybot.instance.getLogger()
                            .info("已加载语言文件: " + fileName + " (" + loaded + " 条"
                                    + (skipped > 0 ? "，跳过 " + skipped + " 条异常" : "")
                                    + ") 命名空间: (" + namespace + ")");
                }
            } catch (Exception e) {
                Easybot.instance.getLogger()
                        .warning("加载语言文件失败 " + fileName + ": " + e.getMessage());
            }
        }
    }
    

    public static Component render(@NotNull Component component, @NotNull Locale locale) {
        return TRANSLATABLE_COMPONENT_RENDERER.render(component, locale);
    }
}
