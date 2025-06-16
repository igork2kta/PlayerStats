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

public class StatsScreen extends Screen {

    public StatsScreen() {
        super(Component.literal("Player Stats"));
    }



    @Override
    protected void init() {
        super.init();
        double increment;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int y = 20;
        for (AttributeInstance attr : player.getAttributes().getSyncableAttributes()) {
            Attribute attribute = attr.getAttribute();
            String name = Component.translatable(attribute.getDescriptionId()).getString();

            String attributeName = Component.translatable(attribute.getDescriptionId()).getString();

            if(attributeName == "Max Health") increment = 1;
            else increment = 0.01;

            double finalIncrement = increment;

            // Botão +

            addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
                sendAttributeChange(attribute, finalIncrement);
            }).bounds(20, y +3, 10, 10).build());

            // Botão -
            addRenderableWidget(Button.builder(Component.literal("-"), btn -> {
                sendAttributeChange(attribute, finalIncrement*-1);
            }).bounds(35, y +3, 10, 10).build());

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
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        Font font = Minecraft.getInstance().font;
        int y = 25;
        for (AttributeInstance attr : player.getAttributes().getSyncableAttributes()) {
            Attribute attribute = attr.getAttribute();
            String name = Component.translatable(attribute.getDescriptionId()).getString();
            String value = String.format("%.2f", attr.getValue());

            // Texto ao lado dos botões
            guiGraphics.drawString(font, name + ": " + value, 50, y, 0xFFFFFF);
            y += 15;
        }
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
