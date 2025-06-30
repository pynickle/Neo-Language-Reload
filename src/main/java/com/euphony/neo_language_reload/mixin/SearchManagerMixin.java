package com.euphony.neo_language_reload.mixin;

import com.euphony.neo_language_reload.access.ILanguage;
import com.euphony.neo_language_reload.access.ITranslationStorage;
import com.euphony.neo_language_reload.config.Config;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = SessionSearchTrees.class, priority = 990)
abstract class SearchManagerMixin {
    @WrapOperation(method = {"lambda$getTooltipLines$0"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;"
            )
    )
    private static List<Component> addFallbackTranslationsToSearchTooltips(ItemStack instance, Item.TooltipContext context, Player player, TooltipFlag type, Operation<List<Component>> operation) {
        var original = operation.call(instance, context, player, type);

        if (Config.getInstance() == null) return original;
        if (!Config.getInstance().multilingualItemSearch) return original;

        var language = Language.getInstance();
        if (language == null) return original;

        var translationStorage = ((ILanguage) language).languagereload_getTranslationStorage();
        if (translationStorage == null) return original;

        var result = new ArrayList<>(original);
        for (var fallbackCode : Config.getInstance().fallbacks) {
            ((ITranslationStorage) translationStorage).languagereload_setTargetLanguage(fallbackCode);
            operation.call(instance, context, player, type)
                    .stream()
                    .map(Component::getString)
                    .map(Component::literal)
                    .forEach(result::add);
        }

        ((ITranslationStorage) translationStorage).languagereload_setTargetLanguage(null);
        return result;
    }
}
