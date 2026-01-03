package com.euphony.neo_language_reload.mixin;

import com.euphony.neo_language_reload.NeoLanguageReload;
import com.euphony.neo_language_reload.access.ILanguageOptionsScreen;
import com.euphony.neo_language_reload.gui.LanguageEntry;
import com.euphony.neo_language_reload.gui.LanguageListWidget;
import net.minecraft.client.Options;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(LanguageSelectScreen.class)
public abstract class LanguageOptionsScreenMixin extends OptionsSubScreen implements ILanguageOptionsScreen {
    @Unique
    private LanguageListWidget availableLanguageList;
    @Unique
    private LanguageListWidget selectedLanguageList;
    @Unique
    private EditBox searchBox;
    @Unique
    private final LinkedList<String> selectedLanguages = new LinkedList<>();
    @Unique
    private final Map<String, LanguageEntry> languageEntries = new LinkedHashMap<>();

    @Shadow
    private LanguageSelectScreen.LanguageSelectionList languageSelectionList;

    @Inject(method = "<init>", at = @At("TAIL"))
    void onConstructed(Screen lastScreen, Options options, LanguageManager languageManager, CallbackInfo ci) {
        selectedLanguages.addAll(NeoLanguageReload.getLanguages());

        var languages = languageManager.getLanguages();
        if (languages.isEmpty()) {
            var defaultLanguage = LanguageManagerAccessor.languagereload_getEnglishUs();
            languageEntries.put(Language.DEFAULT, new LanguageEntry(this::refresh, Language.DEFAULT, defaultLanguage, selectedLanguages));
        } else {
            languages.forEach((code, language) -> languageEntries.put(code, new LanguageEntry(this::refresh, code, language, selectedLanguages)));
        }

        layout.setHeaderHeight(48);
        layout.setFooterHeight(53);
    }

    @Inject(method = "addContents", at = @At("HEAD"), cancellable = true)
    void onInitBody(CallbackInfo ci) {
        languageSelectionList = LanguageSelectionListWidgetAccessor.languagereload_init(it(), minecraft);

        var listWidth = Math.min(width / 2 - 4, 200);
        availableLanguageList = new LanguageListWidget(minecraft, it(), listWidth, height, Component.translatable("pack.available.title"));
        selectedLanguageList = new LanguageListWidget(minecraft, it(), listWidth, height, Component.translatable("pack.selected.title"));
        availableLanguageList.setX(width / 2 - 4 - listWidth);
        selectedLanguageList.setX(width / 2 + 4);
        layout.addToContents(availableLanguageList);
        layout.addToContents(selectedLanguageList);
        refresh();

        ci.cancel();
    }
    
    @Overwrite
    protected void addTitle() {
        searchBox = new EditBox(minecraft.font, width / 2 - 100, 22, 200, 20, searchBox, Component.empty()) {
            @Override
            public void setFocused(boolean focused) {
                if (!isFocused() && focused) {
                    super.setFocused(true);
                    focusSearch();
                } else super.setFocused(focused);
            }
        };
        searchBox.setResponder(__ -> refresh());

        var header = layout.addToHeader(LinearLayout.vertical().spacing(5));
        header.defaultCellSetting().alignHorizontallyCenter();
        header.addChild(new StringWidget(title, minecraft.font));
        header.addChild(searchBox);
    }

    @Inject(method = "repositionElements", at = @At("HEAD"), cancellable = true)
    protected void onInitTabNavigation(CallbackInfo ci) {
        super.repositionElements();

        var listWidth = Math.min(width / 2 - 4, 200);
        availableLanguageList.updateSize(listWidth, layout);
        selectedLanguageList.updateSize(listWidth, layout);
        availableLanguageList.setX(width / 2 - 4 - listWidth);
        selectedLanguageList.setX(width / 2 + 4);
        availableLanguageList.refreshScrollAmount();
        selectedLanguageList.refreshScrollAmount();

        ci.cancel();
    }

    @Inject(method = "onDone", at = @At("HEAD"), cancellable = true)
    private void onDone(CallbackInfo ci) {
        if (minecraft == null) return;
        minecraft.setScreen(lastScreen);

        var language = selectedLanguages.peekFirst();
        if (language == null) {
            NeoLanguageReload.setLanguage(null);
        } else {
            var fallbacks = new LinkedList<>(selectedLanguages);
            fallbacks.removeFirst();
            NeoLanguageReload.setLanguage(language, fallbacks);
        }

        ci.cancel();
    }

    @Unique
    private void refresh() {
        selectedLanguageList.set(selectedLanguages.stream().map(languageEntries::get).filter(Objects::nonNull));
        availableLanguageList.set(languageEntries.values().stream()
                .filter(entry -> {
                    if (selectedLanguageList.children().contains(entry)) return false;
                    var query = searchBox.getValue().toLowerCase(Locale.ROOT);
                    var langCode = entry.getCode().toLowerCase(Locale.ROOT);
                    var langName = entry.getLanguage().toComponent().getString().toLowerCase(Locale.ROOT);
                    return langCode.contains(query) || langName.contains(query);
                }));
    }

    @Overwrite
    protected void setInitialFocus() {
        focusSearch();
    }

    @Unique
    private void focusSearch() {
        changeFocus(ComponentPath.path(searchBox, this));
    }

    @Override
    public void languagereload_focusList(LanguageListWidget list) {
        changeFocus(ComponentPath.path(list, this));
    }

    @Override
    public void languagereload_focusEntry(LanguageEntry entry) {
        changeFocus(ComponentPath.path(entry, entry.getParent(), this));
    }

    @Unique
    LanguageSelectScreen it() {
        return (LanguageSelectScreen) (Object) this;
    }

    LanguageOptionsScreenMixin(Screen parent, Options options, Component title) {
        super(parent, options, title);
    }
}
