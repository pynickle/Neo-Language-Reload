package com.euphony.neo_language_reload.mixin;

import com.euphony.neo_language_reload.NeoLanguageReload;
import com.euphony.neo_language_reload.config.Config;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Objects;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private boolean usedDebugKeyAsModifier;

    @Shadow
    protected abstract void debugWarningComponent(Component message);

    @Shadow
    protected abstract void debugFeedbackComponent(Component message);

    @Unique
    private void processLanguageReloadKeys(KeyEvent input) {
        if (input.hasShiftDown()) {
            var config = Config.getInstance();
            var languageManager = minecraft.getLanguageManager();

            var language = languageManager.getLanguage(config.previousLanguage);
            if (language == null && config.previousLanguage.equals(Language.DEFAULT)) {
                language = LanguageManagerAccessor.languagereload_getEnglishUs();
            }
            var noLanguage = config.previousLanguage.equals(NeoLanguageReload.NO_LANGUAGE);
            if (language == null && !noLanguage) {
                debugWarningComponent(Component.translatable("debug.reload_languages.switch.failure"));
            } else {
                NeoLanguageReload.setLanguage(config.previousLanguage, config.previousFallbacks);
                var languages = new ArrayList<Component>();
                if (noLanguage) {
                    languages.add(Component.literal("∅"));
                }
                if (language != null) {
                    languages.add(language.toComponent());
                }
                languages.addAll(config.fallbacks.stream()
                        .map(languageManager::getLanguage)
                        .filter(Objects::nonNull)
                        .map(LanguageInfo::toComponent)
                        .toList());
                debugFeedbackComponent(Component.translatable("debug.reload_languages.switch.success", ComponentUtils.formatList(languages, Component.literal(", "))));
            }
        } else {
            NeoLanguageReload.reloadLanguages();
            debugFeedbackComponent(Component.translatable("debug.reload_languages.message"));
        }
    }

    @Inject(method = "handleDebugKeys", at = @At("RETURN"), cancellable = true)
    private void onProcessF3(KeyEvent keyInput, CallbackInfoReturnable<Boolean> cir) {
        if (NeoLanguageReload.reloadLanguagesKey.matches(keyInput)) {
            processLanguageReloadKeys(keyInput);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPress", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/KeyboardHandler;debugCrashKeyTime:J"),
            cancellable = true)
    private void onOnKey(long window, int action, KeyEvent input, CallbackInfo ci) {
        if (minecraft.screen != null && minecraft.options.keyDebugModifier.isDown() && NeoLanguageReload.reloadLanguagesKey.matches(input)) {
            this.usedDebugKeyAsModifier = true;
            if (action != InputConstants.PRESS) {
                processLanguageReloadKeys(input);
            }
            ci.cancel();
        }
    }

    @Inject(method = "charTyped", at = @At(value = "HEAD"), cancellable = true)
    private void onOnChar(long window, CharacterEvent event, CallbackInfo ci) {
        var clientWindow = minecraft.getWindow();

        if (InputConstants.isKeyDown(clientWindow, GLFW.GLFW_KEY_F3) && InputConstants.isKeyDown(clientWindow, GLFW.GLFW_KEY_J)) {
            ci.cancel();
        }
    }
}
