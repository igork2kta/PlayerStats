package com.playerstats.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class CustomButton extends Button {


    private final MutableComponent LABEL; // Texto que será mostrado
    private final Font font = Minecraft.getInstance().font;

    private final ResourceLocation TEXTURE;

    public CustomButton(int x, int y, int width, int height, MutableComponent label, ResourceLocation texture, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.LABEL = label;
        this.TEXTURE = texture;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

        //A textura tem o dobro do tamanho exibido, em cima normal e em baixo é a textura hovered. Quando passa o mouse por cima, altera para a textura hovered
        int texY = this.isHovered() ? this.height : 0; // Se hover, desenha a parte inferior

        graphics.blit(
                TEXTURE,
                this.getX(),
                this.getY(),
                0,
                texY,
                this.width,
                this.height,
                this.width,
                this.height * 2 // altura total da textura (dobro da altura do botão)
        );

        int textX = this.getX() + (this.width - font.width(LABEL)) / 2;
        int textY = this.getY() + (this.height - 8) / 2;
        graphics.drawString(font, LABEL, textX, textY, 0xFFFFFF, false);
    }
}
