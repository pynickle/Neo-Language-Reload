package com.euphony.neo_language_reload.access;

import org.jetbrains.annotations.Nullable;


public interface ITranslationStorage {
    String languagereload_get(String key);

    @Nullable String languagereload_getTargetLanguage();

    void languagereload_setTargetLanguage(@Nullable String value);
}
