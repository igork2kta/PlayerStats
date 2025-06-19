package com.playerstats.client;

import com.mojang.blaze3d.systems.RenderSystem;
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

    private static final ResourceLocation BACKGROUND = new ResourceLocation("playerstats", "textures/gui/stats_background.png");
    private static final int BG_WIDTH = 255;
    private static final int BG_HEIGHT = 255;
    private int leftPos;
    private int topPos;

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int SCROLL_STEP = 10;

    private int clipTop;
    private int clipBottom;

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - BG_WIDTH) / 2;
        this.topPos = (this.height - BG_HEIGHT) / 2;

        this.clipTop = topPos + 38;
        this.clipBottom = topPos + BG_HEIGHT - 10;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int totalLines = player.getAttributes().getSyncableAttributes().size();
        int visibleLines = (clipBottom - clipTop) / 15;
        maxScroll = Math.max(0, (totalLines - visibleLines) * 15);

        rebuildButtons();
    }

    private void rebuildButtons() {
        this.clearWidgets();

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int y = clipTop - scrollOffset;
        for (AttributeInstance attr : player.getAttributes().getSyncableAttributes()) {

            Attribute attribute = attr.getAttribute();
            String name = Component.translatable(attribute.getDescriptionId()).getString();
            double increment;

            if(name.equals("Armor")|| name.equals("Gravity")|| name.equals("Step Weight") || name.equals("Fall Flying")) continue;

            if (y + 12 < clipTop) {
                y += 15;
                continue; // antes da área visível
            }
            if (y > clipBottom) {
                break; // depois da área visível
            }

            switch(name) {
                case "Max Health":
                case "Luck":
                case "Block Reach":
                    increment = 1;
                    break;
                case "Max Mana":
                case "Weight":
                    increment = 10;
                    break;
                case "Speed":
                case "Mana Regeneration":
                    increment = 0.01;
                    break;

                default:
                    increment = 0.1;
                    break;
            }

            double finalIncrement = increment;

            addRenderableWidget(Button.builder(Component.literal("-"), btn -> {
                sendAttributeChange(attribute, -finalIncrement);
            }).bounds(leftPos + 10, y, 12, 12).build());

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
        guiGraphics.drawString(font, Component.translatable("gui.playerstats.points", points) , leftPos + 10, y, 0xFFFFFF);

        // Renderizar atributos dentro dos limites
        y = clipTop - scrollOffset;
        for (AttributeInstance attr : player.getAttributes().getSyncableAttributes()) {
            
            Attribute attribute = attr.getAttribute();
            String name = Component.translatable(attribute.getDescriptionId()).getString();
            if(name.equals("Armor")|| name.equals("Gravity")|| name.equals("Step Weight") || name.equals("Fall Flying")) continue;

            if (y + 12 < clipTop) {
                y += 15;
                continue;
            }
            if (y > clipBottom) break;

            Attribute attribute = attr.getAttribute();
            String name = Component.translatable(attribute.getDescriptionId()).getString();
            String value = String.format("%.2f", attr.getValue());

            guiGraphics.drawString(font, name + ": " + value, leftPos + 45, y, 0xFFFFFF);
            y += 15;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollOffset -= delta * SCROLL_STEP;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        rebuildButtons(); // atualiza os botões visíveis
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
