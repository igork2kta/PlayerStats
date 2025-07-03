package com.playerstats.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.playerstats.Config;
import com.playerstats.PlayerStats;
import com.playerstats.network.ModifyAttributePacket;
import com.playerstats.network.PacketHandler;
import com.playerstats.network.ResetAttributesPacket;
import com.playerstats.util.AttributeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public class StatsScreen extends Screen {

    public StatsScreen() {
        super(Component.literal("Player Stats"));
    }

    private static final ResourceLocation BACKGROUND = new ResourceLocation("playerstats", "textures/gui/stats_background.png");
    //Tamanho do background
    private static final int BG_WIDTH = 255;
    private static final int BG_HEIGHT = 255;
    private static final int SCROLL_STEP = 10;
    private static final int LINE_HEIGHT = 15;

    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int clipTop;
    private int clipBottom;
    private Button resetButton;


    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - BG_WIDTH) / 2;
        this.topPos = (this.height - BG_HEIGHT) / 2;

        this.clipTop = topPos + 48;
        this.clipBottom = topPos + BG_HEIGHT - 55;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int totalLines = (int) BuiltInRegistries.ATTRIBUTE.stream()
                .map(player.getAttributes()::getInstance)
                .filter(attr -> attr != null && !Config.cachedIgnoredAttributes.contains(attr.getAttribute().getDescriptionId()))
                .count();

        int visibleLines = (clipBottom - clipTop) / LINE_HEIGHT;
        maxScroll = Math.max(0, (totalLines - visibleLines) * LINE_HEIGHT);

        resetButton = Button.builder(
                Component.translatable("gui.playerstats.reset"),
                btn -> PacketHandler.sendToServer(new ResetAttributesPacket())
        ).bounds(leftPos + (BG_WIDTH / 2) - 35, topPos + BG_HEIGHT - 30, 70, 20).build();

        rebuildButtons();
    }


    private void rebuildButtons() {
        this.clearWidgets();

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int y = clipTop - scrollOffset;

        for (Attribute attr : BuiltInRegistries.ATTRIBUTE) {
            AttributeInstance attribute = player.getAttributes().getInstance(attr);

            //Aqui, removemos os atributos que não são editáveis (pelo menos a maioria)
            if (attribute == null || !attribute.getAttribute().isClientSyncable() ) continue;

            //Aqui, removemos os que não queremos que apareçam
            if (Config.cachedIgnoredAttributes.contains(attr.getDescriptionId())) continue;


            if (y + 12 < clipTop) {
                y += LINE_HEIGHT;
                continue;
            }
            if (y > clipBottom) break;


            if (Config.DEBUG_MODE.get()) {
                double increment = AttributeUtils.getIncrement(attr.getDescriptionId());
                PlayerStats.LOGGER.info("Configurando atributo: {} Incremento: {}", attr.getDescriptionId(), increment);
            }

            //Posição do botão de incrementar, varia pois no ambiente DEV tem o botão de decrementar
            int plusButtonPos;

            if (!net.minecraftforge.fml.loading.FMLEnvironment.production) {
                addRenderableWidget(Button.builder(Component.literal("-"), btn ->
                                sendAttributeChange(attr))
                        .bounds(leftPos + 10, y, 12, 12).build());
                plusButtonPos = leftPos + 26;
            }
            else

                plusButtonPos =  leftPos + 10;

            addRenderableWidget(Button.builder(Component.literal("+"), btn ->
                            sendAttributeChange(attr))
                    .bounds(plusButtonPos, y, 12, 12).build());

            y += LINE_HEIGHT;
        }

        addRenderableWidget(resetButton);
    }

    private void sendAttributeChange(Attribute attribute) {
        ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
        if (id != null) {
            PacketHandler.sendToServer(new ModifyAttributePacket(id.toString()));
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

        //Cor texto de pontos disponíveis
        int color;
        if(points > 0) color = 0x00FF00; //Verde
        else color = 0xFF5555; //Vermelho

        guiGraphics.drawString(font, Component.translatable("gui.playerstats.points", points), leftPos + 10, topPos + 12, color);


        int y = clipTop - scrollOffset +2; //2 para alinhamento do texto com os botões

        for (Attribute attr : BuiltInRegistries.ATTRIBUTE) {
            AttributeInstance instance = player.getAttributes().getInstance(attr);

            //Aqui, removemos os atributos que não são editáveis (pelo menos a maioria)
            if (instance == null || !instance.getAttribute().isClientSyncable()) continue;

            //Aqui, removemos os que não queremos que apareça
            String name = AttributeUtils.getAttributeName(attr);
            if (Config.cachedIgnoredAttributes.contains(attr.getDescriptionId())) continue;

            if (y + 12 < clipTop) {
                y += LINE_HEIGHT;
                continue;
            }
            if (y > clipBottom) break;

            String value = String.format("%.2f", instance.getValue());

            int pos;
            if (!net.minecraftforge.fml.loading.FMLEnvironment.production) pos = leftPos + 45;
            else pos =  leftPos + 29;

            guiGraphics.drawString(font, name + ": " + value, pos, y, 0x303030, false);

            y += LINE_HEIGHT;
        }

        if (resetButton != null && resetButton.isHovered()) {
            boolean hasXp = player.experienceLevel >= 50;

            MutableComponent text = hasXp
                    ? Component.translatable("gui.playerstats.can_reset")
                    : Component.translatable("gui.playerstats.cant_reset");

            color = hasXp ? 0x00FF00 : 0xFF5555; // verde ou vermelho

            guiGraphics.drawString(
                    font,
                    text,
                    mouseX + 10, // desenha próximo do cursor
                    mouseY,
                    color,
                    false
            );
        }

        int upgradeCount = ClientAttributeCache.getUpgradeCount();
        int xpCost = (upgradeCount + 1) * 5;
        boolean hasXpForUpgrade = player.experienceLevel > xpCost;
        color = hasXpForUpgrade ? 0x00FF00 : 0xFF5555; // verde ou vermelho

        guiGraphics.drawString(font, Component.translatable("gui.playerstats.xp_cost", xpCost), leftPos + 10, topPos + 24, color);



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
