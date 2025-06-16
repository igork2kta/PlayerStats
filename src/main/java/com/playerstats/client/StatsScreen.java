package com.playerstats.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.playerstats.event.PlayerAttributePersistence;
import com.playerstats.network.ModifyAttributePacket;
import com.playerstats.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.vertex.PoseStack;

public class StatsScreen extends Screen {

    public StatsScreen() {
        super(Component.literal("Player Stats"));
    }

    private static final ResourceLocation BACKGROUND = new ResourceLocation("playerstats", "textures/gui/stats_background.png");
    private static final int BG_WIDTH = 160;
    private static final int BG_HEIGHT = 120;
    private int leftPos;
    private int topPos;

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - BG_WIDTH) / 2;
        this.topPos = (this.height - BG_HEIGHT) / 2;

        Player player = Minecraft.getInstance().player;
        Font font = Minecraft.getInstance().font;

        if (player == null) return;

        int y = topPos + 25;
        for (AttributeInstance attr : player.getAttributes().getSyncableAttributes()) {
            Attribute attribute = attr.getAttribute();
            String name = Component.translatable(attribute.getDescriptionId()).getString();
            String value = String.format("%.2f", attr.getValue());

            double increment = name.equals("Max Health") ? 1 : 0.01;
            double finalIncrement = increment;

            // Botão -
            addRenderableWidget(Button.builder(Component.literal("-"), btn -> {
                sendAttributeChange(attribute, -finalIncrement);
            }).bounds(leftPos + 10, y, 12, 12).build());

            // Botão +
            addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
                sendAttributeChange(attribute, finalIncrement);
            }).bounds(leftPos + 26, y, 12, 12).build());

            y += 15;
        }


    }


    private void sendAttributeChange(Attribute attribute, double delta) {
        ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
        if (id != null) {
            PacketHandler.sendToServer(new ModifyAttributePacket(id.toString(), delta));

        } else {
            System.out.println("Atributo sem ResourceLocation válido!");
        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, BG_WIDTH, BG_HEIGHT);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        Player player = Minecraft.getInstance().player;
        Font font = Minecraft.getInstance().font;
        if (player == null) return;

        int points = ClientAttributeCache.getPoints();

        int y = topPos + 13;
        guiGraphics.drawString(font,"Pontos disponíveis: " + points, leftPos + 10, y, 0xFFFFFF);
        y += 13;
        for (AttributeInstance attr : player.getAttributes().getSyncableAttributes()) {
            Attribute attribute = attr.getAttribute();
            String name = Component.translatable(attribute.getDescriptionId()).getString();
            String value = String.format("%.2f", attr.getValue());

            guiGraphics.drawString(font, name + ": " + value, leftPos + 45, y, 0xFFFFFF);
            y += 15;
        }
    }



    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
