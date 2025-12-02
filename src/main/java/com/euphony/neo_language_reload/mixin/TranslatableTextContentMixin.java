package com.euphony.neo_language_reload.mixin;

import com.euphony.neo_language_reload.access.ILanguage;
import com.euphony.neo_language_reload.access.ITranslationStorage;
import com.euphony.neo_language_reload.config.Config;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.TranslatableFormatException;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Consumer;

@Mixin(TranslatableContents.class)
abstract class TranslatableTextContentMixin implements ComponentContents {
    @Shadow
    @Final
    private String key;

    @WrapOperation(method = "visit(Lnet/minecraft/network/chat/FormattedText$ContentConsumer;)Ljava/util/Optional;",
            at = @At(value = "FIELD", target = "Lnet/minecraft/network/chat/contents/TranslatableContents;decomposedParts:Ljava/util/List;"))
    List<FormattedText> onVisit(TranslatableContents instance, Operation<List<FormattedText>> translationsGetter) {
        var overriddenTranslations = getOverriddenTranslations();
        if (overriddenTranslations != null) return overriddenTranslations;
        return translationsGetter.call(instance);
    }

    @WrapOperation(method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At(value = "FIELD", target = "Lnet/minecraft/network/chat/contents/TranslatableContents;decomposedParts:Ljava/util/List;"))
    List<FormattedText> onVisitStyled(TranslatableContents instance, Operation<List<FormattedText>> translationsGetter) {
        var overriddenTranslations = getOverriddenTranslations();
        if (overriddenTranslations != null) return overriddenTranslations;
        return translationsGetter.call(instance);
    }

    @Unique
    List<FormattedText> getOverriddenTranslations() {
        if (!Config.getInstance().multilingualItemSearch) return null;

        var language = Language.getInstance();
        if (language == null) return null;

        var translationStorage = ((ILanguage) language).languagereload_getTranslationStorage();
        if (translationStorage == null) return null;

        var targetLanguage = ((ITranslationStorage) translationStorage).languagereload_getTargetLanguage();
        if (targetLanguage == null) return null;

        var string = ((ITranslationStorage) translationStorage).languagereload_get(key);
        try {
            var builder = new ImmutableList.Builder<FormattedText>();
            this.decomposeTemplate(string, builder::add);
            return builder.build();
        } catch (TranslatableFormatException e) {
            return ImmutableList.of(FormattedText.of(string));
        }
    }

    @Shadow
    protected abstract void decomposeTemplate(String translation, Consumer<FormattedText> partsConsumer);
}
