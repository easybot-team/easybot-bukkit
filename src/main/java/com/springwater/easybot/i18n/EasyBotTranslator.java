package com.springwater.easybot.i18n;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Locale;

public class EasyBotTranslator implements Translator {
    @Getter
    private TranslationRegistry registry = TranslationRegistry.create(Key.key("easybot:translator"));
    public void clearRegistry() {
        registry = TranslationRegistry.create(Key.key("easybot:translator"));
    }
    @Override
    public @NotNull Key name() {
        return Key.key("easybot:translator");
    }

    @Override
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        MessageFormat messageFormat = registry.translate(key, locale);
        if(messageFormat != null)
            return messageFormat;
        return GlobalTranslator.translator().translate(key, locale);
    }

    @Override
    public @Nullable Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {
        Component translated = registry.translate(component, locale);
        if(translated != null)
            return translated;
        return GlobalTranslator.translator().translate(component, locale);
    }

    @Override
    public @NotNull TriState hasAnyTranslations() {
        return registry.hasAnyTranslations();
    }
}
