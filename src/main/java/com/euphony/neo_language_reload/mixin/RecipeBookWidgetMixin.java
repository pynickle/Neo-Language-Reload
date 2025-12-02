package com.euphony.neo_language_reload.mixin;

import com.euphony.neo_language_reload.NeoLanguageReload;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookComponent.class)
public class RecipeBookWidgetMixin {
    @Inject(method = "pirateSpeechForThePeople", cancellable = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/resources/language/LanguageManager;setSelected(Ljava/lang/String;)V"))
    void onLanguageSwitching$cancel(String search, CallbackInfo ci) {
        NeoLanguageReload.setLanguage("en_pt");
        ci.cancel();
    }
}
