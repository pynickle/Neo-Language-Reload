package com.euphony.neo_language_reload.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public class HeaderEntry extends LanguageListWidget.Entry {
    private final Font font;
    private final Component text;

    public HeaderEntry(Font font, Component text) {
        this.font = font;
        this.text = text;
    }

    @Override
    public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float a) {
        var x = this.getX() + this.getWidth() / 2;
        var y = this.getContentYMiddle() - 9 / 2;
        context.centeredText(font, text, x, y, CommonColors.WHITE);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        return false;
    }

    @Override
    public Component getNarration() {
        return text;
    }

    @Override
    public String getCode() {
        return "";
    }
}
