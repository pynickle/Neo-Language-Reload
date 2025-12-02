package com.euphony.neo_language_reload;

import com.euphony.neo_language_reload.access.IAdvancementsScreen;
import com.euphony.neo_language_reload.config.Config;
import com.euphony.neo_language_reload.config.ConfigScreen;
import com.euphony.neo_language_reload.mixin.*;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.locale.Language;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.LinkedList;

@Mod(value = NeoLanguageReload.MOD_ID, dist = Dist.CLIENT)
public class NeoLanguageReload {
    public static final String MOD_ID = "neo_language_reload";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String NO_LANGUAGE = "*";

    public static boolean shouldSetSystemLanguage = false;

    public static void reloadLanguages() {
        var client = Minecraft.getInstance();

        // Reload language manager
        client.getLanguageManager().onResourceManagerReload(client.getResourceManager());

        // Update window title and chat
        client.updateTitle();
        client.gui.getChat().rescaleChat();

        // Update book and advancements screens
        if (client.screen instanceof BookViewScreen bookScreen) {
            ((BookScreenAccessor) bookScreen).languagereload_setCachedPageIndex(-1);
        } else if (client.screen instanceof AdvancementsScreen advancementsScreen) {
            ((IAdvancementsScreen) advancementsScreen).languagereload_recreateWidgets();
        }

        if (client.level != null) {
            // Update signs
            var chunkManager = (ClientChunkManagerAccessor) client.level.getChunkSource();
            var chunks = ((ClientChunkMapAccessor) (Object) chunkManager.languagereload_getChunks()).languagereload_getChunks();
            for (int i = 0; i < chunks.length(); i++) {
                var chunk = chunks.get(i);
                if (chunk == null) continue;
                for (var blockEntity : chunk.getBlockEntities().values()) {
                    if (!(blockEntity instanceof SignBlockEntity sign)) continue;
                    ((SignTextAccessor) sign.getFrontText()).languagereload_setOrderedMessages(null);
                    ((SignTextAccessor) sign.getBackText()).languagereload_setOrderedMessages(null);
                }
            }

            // Update text displays
            for (var entity : client.level.entitiesForRendering()) {
                if (entity instanceof Display.TextDisplay textDisplay) {
                    ((TextDisplayEntityAccessor) textDisplay).languagereload_setTextLines(null);
                }
            }
        }
    }

    public static void setLanguage(@Nullable String language) {
        if (language == null || language.equals(NO_LANGUAGE)) {
            setLanguage(NO_LANGUAGE, null);
        } else if (language.equals(Language.DEFAULT)) {
            setLanguage(Language.DEFAULT, null);
        } else {
            setLanguage(language, new LinkedList<>() {{
                add(Language.DEFAULT);
            }});
        }
    }

    public static void setLanguage(
            @Nullable String language,
            @Nullable LinkedList<@NotNull String> fallbacks
    ) {
        var newLanguage = language == null ? NO_LANGUAGE : language;
        var newFallbacks = fallbacks == null ? new LinkedList<String>() : fallbacks;

        var client = Minecraft.getInstance();
        var languageManager = client.getLanguageManager();
        var config = Config.getInstance();

        var languageIsSame = languageManager.getSelected().equals(newLanguage);
        var fallbacksAreSame = config.fallbacks.equals(newFallbacks);
        if (languageIsSame && fallbacksAreSame) return;

        config.previousLanguage = languageManager.getSelected();
        config.previousFallbacks = config.fallbacks;
        config.language = newLanguage;
        config.fallbacks = newFallbacks;
        Config.save();

        languageManager.setSelected(newLanguage);
        client.options.languageCode = newLanguage;
        client.options.save();

        reloadLanguages();
    }

    public static @NotNull LinkedList<@NotNull String> getLanguages() {
        var list = new LinkedList<String>();
        var language = Minecraft.getInstance().getLanguageManager().getSelected();
        if (!language.equals(NeoLanguageReload.NO_LANGUAGE)) {
            list.add(language);
        }
        list.addAll(Config.getInstance().fallbacks);
        return list;
    }

    public NeoLanguageReload(IEventBus modEventBus, ModContainer modContainer) {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (client, screen) -> new ConfigScreen(screen));
    }
}
