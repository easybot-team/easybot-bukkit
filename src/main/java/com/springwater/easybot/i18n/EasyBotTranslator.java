package com.springwater.easybot.i18n;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EasyBot 自己的翻译源。
 * <p>
 * 注意: adventure 5.x（Paper 26.2+ 自带）已经移除了 {@code TranslationRegistry}，
 * 所以这里自己维护一份 {@code Locale -> (key -> MessageFormat)} 的翻译表，
 * 只依赖 4.x / 5.x 都存在的 {@link Translator} 与 {@link GlobalTranslator} 接口，
 * 同一个 jar 可以同时跑旧服务端与 Paper 26.3。
 */
public class EasyBotTranslator implements Translator {

    private static final Key NAME = Key.key("easybot:translator");

    private final Map<Locale, Map<String, MessageFormat>> translations = new ConcurrentHashMap<>();

    public void clearRegistry() {
        translations.clear();
    }

    public void register(@NotNull String key, @NotNull Locale locale, @NotNull MessageFormat format) {
        Map<String, MessageFormat> byKey = translations.get(locale);
        if (byKey == null) {
            byKey = new HashMap<>();
            translations.put(locale, byKey);
        }
        byKey.put(key, format);
    }

    public boolean isEmpty() {
        for (Map<String, MessageFormat> byKey : translations.values()) {
            if (!byKey.isEmpty()) return false;
        }
        return true;
    }

    private @Nullable MessageFormat find(@NotNull String key, @NotNull Locale locale) {
        Map<String, MessageFormat> byKey = translations.get(locale);
        if (byKey == null) return null;
        return byKey.get(key);
    }
    @Override
    public @NotNull Key name() {
        return NAME;
    }

    @Override
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        MessageFormat messageFormat = find(key, locale);
        if(messageFormat != null)
            return messageFormat;
        return GlobalTranslator.translator().translate(key, locale);
    }

    @Override
    public @Nullable Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {
        MessageFormat messageFormat = find(component.key(), locale);
        if (messageFormat != null) {
            // 自己的翻译交给渲染器的 MessageFormat 分支处理，这里不抢参数拼接的活
            return null;
        }
        return GlobalTranslator.translator().translate(component, locale);
    }

    @Override
    public @NotNull TriState hasAnyTranslations() {
        return isEmpty() ? TriState.NOT_SET : TriState.TRUE;
    }
}
