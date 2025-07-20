package com.euphony.neo_language_reload.access;

import com.euphony.neo_language_reload.gui.LanguageEntry;
import com.euphony.neo_language_reload.gui.LanguageListWidget;

public interface ILanguageOptionsScreen {
    void languagereload_focusList(LanguageListWidget list);

    void languagereload_focusEntry(LanguageEntry entry);
}
