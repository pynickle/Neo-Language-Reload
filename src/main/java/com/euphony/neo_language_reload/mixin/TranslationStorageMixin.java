package com.euphony.neo_language_reload.mixin;

import com.euphony.neo_language_reload.access.ITranslationStorage;
import com.euphony.neo_language_reload.config.Config;
import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(ClientLanguage.class)
abstract class TranslationStorageMixin extends Language implements ITranslationStorage {
    @Shadow public abstract String getOrDefault(String key, String defaultValue);

    @Unique private final Map<Long, String> targetLanguageByThread = Maps.newConcurrentMap();
    @Unique private static Map<String, Map<String, String>> separateTranslationsOnLoad;
    @Unique private Map<String, Map<String, String>> separateTranslations;

    @Inject(method = "<init>", at = @At("RETURN"))
    void onConstructed(Map<String, String> translations, boolean rightToLeft, CallbackInfo ci) {
        separateTranslations = separateTranslationsOnLoad;
        separateTranslationsOnLoad = null;
    }

    @Inject(method = "loadFrom",
            at = @At("HEAD"))
    private static void onLoad(ResourceManager resourceManager, List<String> filenames, boolean defaultRightToLeft, CallbackInfoReturnable<ClientLanguage> cir) {
        separateTranslationsOnLoad = Maps.newHashMap();
    }

    @Redirect(method = "appendFrom(Ljava/lang/String;Ljava/util/List;Ljava/util/Map;Ljava/util/Map;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/locale/Language;loadFromJson(Ljava/io/InputStream;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;)V"))
    private static void onInternalLoad$saveSeparately(InputStream inputStream, BiConsumer<String, String> entryConsumer, BiConsumer<String, Component> langCode, @Local(argsOnly = true) String languageName) {
        if (Config.getInstance().multilingualItemSearch) {
            Language.loadFromJson(inputStream, entryConsumer.andThen((key, value) ->
                    separateTranslationsOnLoad.computeIfAbsent(languageName, k -> Maps.newHashMap()).put(key, value)));
        } else Language.loadFromJson(inputStream, entryConsumer);
    }

    @Override
    public String languagereload_get(String key) {
        var targetLanguage = languagereload_getTargetLanguage();
        if (targetLanguage != null) {
            var targetTranslations = separateTranslations.get(targetLanguage);
            return targetTranslations == null ? "" : targetTranslations.getOrDefault(key, "");
        }
        return this.getOrDefault(key);
    }

    @Override
    public @Nullable String languagereload_getTargetLanguage() {
        return targetLanguageByThread.get(Thread.currentThread().threadId());
    }

    @Override
    public void languagereload_setTargetLanguage(@Nullable String value) {
        var threadId = Thread.currentThread().threadId();
        if (value == null) targetLanguageByThread.remove(threadId);
        else targetLanguageByThread.put(threadId, value);
    }
}
