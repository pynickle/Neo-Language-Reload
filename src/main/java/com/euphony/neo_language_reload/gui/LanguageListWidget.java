package com.euphony.neo_language_reload.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class LanguageListWidget extends ObjectSelectionList<LanguageListWidget.Entry> {
    private final HeaderEntry headerEntry;
    private final LanguageSelectScreen screen;

    public LanguageListWidget(Minecraft client, LanguageSelectScreen screen, int width, int height, Component title) {
        super(client, width, height - 83 - 16, 32 + 16, 24);
        this.screen = screen;
        this.headerEntry = new HeaderEntry(
                client.font,
                Component.empty().append(title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD)
        );

        centerListVertically = false;
    }

    public void set(Stream<? extends Entry> entries) {
        this.clearEntries();
        this.addEntry(headerEntry, (int) (9f * 1.5f));
        entries.forEach(this::addEntry);
        this.refreshScrollAmount();
    }

    @Override
    protected int addEntry(Entry entry, int entryHeight) {
        entry.setParent(this);
        return super.addEntry(entry, entryHeight);
    }

    @Override
    @Nullable
    public Entry getEntryAtPosition(double x, double y) {
        var entry = super.getEntryAtPosition(x, y);
        return entry != null && this.scrollbarVisible() && x >= this.scrollBarX()
                ? null
                : entry;
    }


    @Override
    protected void renderSelection(GuiGraphics context, Entry entry, int color) {
        if (this.scrollbarVisible()) {
            var x1 = this.getX();
            var y1 = this.getY();
            var x2 = this.scrollBarX();
            var y2 = y1 + entry.getHeight();
            context.fill(x1, y1, x2, y2, color);
            context.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, CommonColors.BLACK);
        } else {
            super.renderSelection(context, entry, color);
        }
    }

    public int getHoveredSelectionRight() {
        return this.scrollbarVisible()
                ? this.scrollBarX()
                : this.getRowRight();
    }

    public LanguageSelectScreen getScreen() {
        return screen;
    }

    public int getRowHeight() {
        return height;
    }

    @Override
    public int getRowWidth() {
        return width;
    }

    @Override
    public int scrollBarX() {
        return this.getRight() - 6;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        var selectedEntry = this.getSelected();
        return selectedEntry != null
                ? selectedEntry.keyPressed(input)
                : super.keyPressed(input);
    }

    public abstract static class Entry extends ObjectSelectionList.Entry<LanguageListWidget.Entry> {
        protected LanguageListWidget parentList;

        public void setParent(LanguageListWidget list) {
            this.parentList = list;
        }

        public LanguageListWidget getParent() {
            return parentList;
        }

        public abstract String getCode();
    }
}
