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

import java.util.Set;
import java.util.HashSet;

public class StatsScreen extends Screen {

    public StatsScreen() {
        super(Component.literal("Player Stats"));
    }

    private static final ResourceLocation BACKGROUND = new ResourceLocation("playerstats", "textures/gui/stats_background.png");
    private static final int BG_WIDTH = 255;
    private static final int BG_HEIGHT = 255;
    private static final int SCROLL_STEP = 10;
    private static final int LINE_HEIGHT = 15;

    private static final Set<String> IGNORED_ATTRIBUTES = Set.of("Armor", "Gravity", "Step Height", "Fall Flying", "Nametag Render Distance", "Armor Toughness", "Weight");

    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;
    private int maxScroll = 0;
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

        int totalLines = (int) BuiltInRegistries.ATTRIBUTE.stream()
                .map(player.getAttributes()::getInstance)
                .filter(attr -> attr != null && !IGNORED_ATTRIBUTES.contains(getAttributeName(attr.getAttribute())))
                .count();

        int visibleLines = (clipBottom - clipTop) / LINE_HEIGHT;
        maxScroll = Math.max(0, (totalLines - visibleLines) * LINE_HEIGHT);

        rebuildButtons();
    }

    private void rebuildButtons() {
        this.clearWidgets();

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int y = clipTop - scrollOffset;

        for (Attribute attr : BuiltInRegistries.ATTRIBUTE) {
            AttributeInstance attribute = player.getAttributes().getInstance(attr);
            if (attribute == null || !attribute.getAttribute().isClientSyncable() ) continue;

            String name = getAttributeName(attr);
            if (IGNORED_ATTRIBUTES.contains(name)) continue;

            if (y + 12 < clipTop) {
                y += LINE_HEIGHT;
                continue;
            }
            if (y > clipBottom) break;

            double increment = getIncrement(name);

            double finalIncrement = increment;

            addRenderableWidget(Button.builder(Component.literal("-"), btn ->
                            sendAttributeChange(attr, -finalIncrement))
                    .bounds(leftPos + 10, y, 12, 12).build());

            addRenderableWidget(Button.builder(Component.literal("+"), btn ->
                            sendAttributeChange(attr, finalIncrement))
                    .bounds(leftPos + 26, y, 12, 12).build());

            y += LINE_HEIGHT;
        }
    }

    private double getIncrement(String name) {
        return switch (name) {
            case "Max Health", "Luck" -> 1;
            case "Max Mana", "Weight" -> 10;
            case "Speed", "Mana Regeneration" -> 0.01;
            case "Entity Reach", "Block Reach" -> 0.3;
            default -> 0.1;
        };
    }

    private String getAttributeName(Attribute attr) {
        return Component.translatable(attr.getDescriptionId()).getString();
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
        guiGraphics.drawString(font, Component.translatable("gui.playerstats.points", points), leftPos + 10, topPos + 13, 0xFFFFFF);

        int y = clipTop - scrollOffset;

        for (Attribute attr : BuiltInRegistries.ATTRIBUTE) {
            AttributeInstance instance = player.getAttributes().getInstance(attr);
            if (instance == null || !instance.getAttribute().isClientSyncable()) continue;

            String name = getAttributeName(attr);
            if (IGNORED_ATTRIBUTES.contains(name)) continue;

            if (y + 12 < clipTop) {
                y += LINE_HEIGHT;
                continue;
            }
            if (y > clipBottom) break;

            String value = String.format("%.2f", instance.getValue());
            guiGraphics.drawString(font, name + ": " + value, leftPos + 45, y, 0xFFFFFF);

            y += LINE_HEIGHT;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollOffset -= delta * SCROLL_STEP;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        rebuildButtons();
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
