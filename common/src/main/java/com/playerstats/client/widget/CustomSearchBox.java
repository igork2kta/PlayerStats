package com.playerstats.client.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CustomSearchBox extends EditBox {

    Font font;

    public CustomSearchBox(Font font, int x, int y, int width, int height) {
        super(font, x, y, width, height, Component.literal(""));
        this.setBordered(false); // Remove a borda padrão para podermos desenhar a nossa
        this.setMaxLength(50);
        this.font = font;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Fundo customizado
        graphics.blit(
                new ResourceLocation("playerstats", "textures/gui/search_box.png"),
                this.getX() - 5,
                this.getY() - (this.height - 8) / 2,
                0,
                0,
                this.width,
                this.height,
                this.width,
                this.height
        );

        // Placeholder: mostrar só se estiver vazio e não focado
        if (this.getValue().isEmpty() && !this.isFocused()) {
            graphics.drawString(
                    this.font,
                    Component.translatable("gui.playerstats.search_placeholder"),
                    this.getX() + 4,
                    this.getY() + 2,
                    0xA0A0A0 ,
                    false
            );
        }

        // Texto digitado e cursor
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);

    }
}
