package com.euphony.neo_language_reload.access;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public interface ITranslationStorage {
    String languagereload_get(String key);

    @Nullable String languagereload_getTargetLanguage();

    void languagereload_setTargetLanguage(@Nullable String value);
}
