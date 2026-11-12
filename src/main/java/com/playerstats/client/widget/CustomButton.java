package com.playerstats.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class CustomButton extends Button {


    private final MutableComponent LABEL; // Texto que será mostrado
    private final Font font = Minecraft.getInstance().font;

    private final Identifier TEXTURE;
    private BooleanSupplier activeSupplier;
    private Supplier<Identifier> textureSupplier;

    public CustomButton(int x, int y, int width, int height, MutableComponent label, Identifier texture, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.LABEL = label;
        this.TEXTURE = texture;
    }

    public void setActiveSupplier(BooleanSupplier activeSupplier) {
        this.activeSupplier = activeSupplier;
    }

    public void setTextureSupplier(Supplier<Identifier> textureSupplier) {
        this.textureSupplier = textureSupplier;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (this.activeSupplier != null) {
            this.active = this.activeSupplier.getAsBoolean();
        }

        int tint = !this.active ? 0x99808080 : 0xFFFFFFFF;

        //A textura tem o dobro do tamanho exibido, em cima normal e em baixo é a textura hovered. Quando passa o mouse por cima, altera para a textura hovered
        int texY = (this.isHovered() && this.active) ? this.height : 0; // Se hover, desenha a parte inferior
        Identifier activeTexture = this.textureSupplier != null ? this.textureSupplier.get() : TEXTURE;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                activeTexture,
                this.getX(),
                this.getY(),
                0,
                texY,
                this.width,
                this.height,
                this.width,
                this.height * 2, // altura total da textura (dobro da altura do botão)
                tint
        );

        int textX = this.getX() + (this.width - font.width(LABEL)) / 2;
        int textY = this.getY() + (this.height - 8) / 2;
        graphics.text(font, LABEL, textX, textY, 0xFFFFFFFF, false);
    }
}
