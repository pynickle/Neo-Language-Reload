package com.euphony.neo_language_reload.gui;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;

public class LanguageEntryButtonWidget extends ImageButton {
    public LanguageEntryButtonWidget(int width, int height, WidgetSprites textures, Button.OnPress action) {
        super(0, 0, width, height, textures, action);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractContents(graphics, mouseX, mouseY, a);
        if (this.isHovered()) {
            graphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }
}
