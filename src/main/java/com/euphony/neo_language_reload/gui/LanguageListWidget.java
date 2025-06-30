package com.euphony.neo_language_reload.gui;

import com.euphony.neo_language_reload.access.ILanguageOptionsScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import static org.lwjgl.glfw.GLFW.*;

public class LanguageListWidget extends ObjectSelectionList<LanguageEntry> {
    private final Component title;
    private final LanguageSelectScreen screen;

    public LanguageListWidget(Minecraft client, LanguageSelectScreen screen, int width, int height, Component title) {
        super(client, width, height - 83 - 16, 32 + 16, 24);
        this.title = title;
        this.screen = screen;

        centerListVertically = false;
    }

    @Override
    protected void renderHeader(GuiGraphics context, int x, int y) {
        var headerText = title.copy().withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
        int headerPosX = x + width / 2 - minecraft.font.width(headerText) / 2;
        int headerPosY = Math.min(this.getY() + 3, y);
        context.drawString(minecraft.font, headerText, headerPosX, headerPosY, -1, true);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        var selectedEntry = this.getSelected();
        if (selectedEntry == null) return super.keyPressed(keyCode, scanCode, modifiers);

        if (keyCode == GLFW_KEY_SPACE || keyCode == GLFW_KEY_ENTER) {
            selectedEntry.toggle();
            this.setFocused(null);
            ((ILanguageOptionsScreen) screen).languagereload_focusEntry(selectedEntry);
            return true;
        }

        if (Screen.hasShiftDown()) {
            if (keyCode == GLFW_KEY_DOWN) {
                selectedEntry.moveDown();
                return true;
            }
            if (keyCode == GLFW_KEY_UP) {
                selectedEntry.moveUp();
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /*
    // Remove focusing on entry click
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.updateScrollingState(mouseX, mouseY, button);
        if (!this.isMouseOver(mouseX, mouseY)) return false;

        var entry = this.getEntryAtPosition(mouseX, mouseY);
        if (entry == null && button == 0) return true;

        if (entry != null && entry.mouseClicked(mouseX, mouseY, button)) {
            var focusedEntry = this.getFocused();
            if (focusedEntry != entry && focusedEntry instanceof ContainerEventHandler parentElement)
                parentElement.setFocused(null);
            this.setDragging(true);
            return true;
        }
        return this.scrolling;
    }

     */

    @Override
    @Nullable
    public LanguageEntry getEntryAtPosition(double x, double y) {
        var entry = super.getEntryAtPosition(x, y);
        return entry != null && this.scrollbarVisible() && x >= this.scrollBarX()
                ? null
                : entry;
    }


    @Override
    protected void renderSelection(GuiGraphics context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
        if (this.scrollbarVisible()) {
            var x1 = this.getRowLeft() - 2;
            var x2 = this.scrollBarX();
            var y1 = y - 2;
            var y2 = y + entryHeight + 2;
            context.fill(x1, y1, x2, y2, borderColor);
            context.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, fillColor);
        } else {
            super.renderSelection(context, y, entryWidth, entryHeight, borderColor, fillColor);
        }
    }

    public int getHoveredSelectionRight() {
        return this.scrollbarVisible()
                ? this.scrollBarX()
                : this.getRowRight() - 2;
    }

    public LanguageSelectScreen getScreen() {
        return screen;
    }

    public int getRowHeight() {
        return itemHeight;
    }

    @Override
    public int getRowWidth() {
        return width;
    }

    @Override
    public int scrollBarX() {
        return this.getRight() - 6;
    }
}
