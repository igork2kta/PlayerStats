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
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import java.util.List;

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
    private EditBox searchBox;
    private String searchText = "";

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - BG_WIDTH) / 2;
        this.topPos = (this.height - BG_HEIGHT) / 2;

        this.clipTop = topPos + 62;
        this.clipBottom = topPos + BG_HEIGHT - 55;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        this.searchBox = new EditBox(this.font, leftPos + 10, topPos + 36, 120, 14, Component.literal("Search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setResponder(text -> {
            this.searchText = text.toLowerCase();
            this.scrollOffset = 0;
            rebuildButtons();

        });
        this.addRenderableWidget(this.searchBox);

        resetButton = Button.builder(
                Component.translatable("gui.playerstats.reset"),
                btn -> PacketHandler.sendToServer(new ResetAttributesPacket())
        ).bounds(leftPos + (BG_WIDTH / 2) - 35, topPos + BG_HEIGHT - 30, 70, 20).build();

        rebuildButtons();
    }

    private void rebuildButtons() {
        this.clearWidgets();
        this.addRenderableWidget(this.searchBox);

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        List<Attribute> filteredAttributes = AttributeUtils.getAttributes(player, searchText);

        int visibleLines = (clipBottom - clipTop) / LINE_HEIGHT;
        maxScroll = Math.max(0, (filteredAttributes.size() - visibleLines) * LINE_HEIGHT);

        int y = clipTop - scrollOffset;

        for (Attribute attr : filteredAttributes) {
            AttributeInstance attribute = player.getAttributes().getInstance(attr);
            if (y + 12 < clipTop) {
                y += LINE_HEIGHT;
                continue;
            }
            if (y + 2 > clipBottom) break;

            if (Config.DEBUG_MODE.get()) {
                double increment = AttributeUtils.getIncrement(attr.getDescriptionId());
                PlayerStats.LOGGER.info("Configurando atributo: {} Incremento: {}", attr.getDescriptionId(), increment);
            }

            int plusButtonPos;

            if (!net.minecraftforge.fml.loading.FMLEnvironment.production) {
                addRenderableWidget(Button.builder(Component.literal("-"), btn ->
                        sendAttributeChange(attr)).bounds(leftPos + 10, y, 12, 12).build());
                plusButtonPos = leftPos + 26;
            } else {
                plusButtonPos = leftPos + 10;
            }

            addRenderableWidget(Button.builder(Component.literal("+"), btn ->
                    sendAttributeChange(attr)).bounds(plusButtonPos, y, 12, 12).build());

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

        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        // Placeholder: mostrar só se estiver vazio e não focado
        if (searchBox.getValue().isEmpty() && !searchBox.isFocused()) {

            guiGraphics.drawString(
                    this.font,
                    Component.translatable("gui.playerstats.search_placeholder"),
                    searchBox.getX() + 4,
                    searchBox.getY() + 3,
                    0xA0A0A0 ,
                    false
            );
        }



        Player player = Minecraft.getInstance().player;
        Font font = Minecraft.getInstance().font;
        if (player == null) return;

        int points = ClientAttributeCache.getPoints();
        int color = points > 0 ? 0x00FF00 : 0xFF5555;
        guiGraphics.drawString(font, Component.translatable("gui.playerstats.points", points), leftPos + 10, topPos + 12, color);

        List<Attribute> filteredAttributes = AttributeUtils.getAttributes(player, searchText);

        int y = clipTop - scrollOffset + 2; //2 para alinhamento do texto com os botões

        for (Attribute attr : filteredAttributes) {
            AttributeInstance instance = player.getAttributes().getInstance(attr);
            String name = AttributeUtils.getAttributeName(attr);

            if (y + 12 < clipTop) {
                y += LINE_HEIGHT;
                continue;
            }

            String value = String.format("%.2f", instance.getValue());
            int pos = net.minecraftforge.fml.loading.FMLEnvironment.production ? leftPos + 29 : leftPos + 45;

            // Parte base (nome + valor normal)
            String baseText = name + ": " + value;
            guiGraphics.drawString(font, baseText, pos, y, 0x303030, false);

            // Parte do boost (somente se existir)
            ClientBoostCache.BoostInfo boost = ClientBoostCache.activeBoosts.get(attr);
            if (boost != null) {
                String boostText = String.format(" (+%.2f %ds)", boost.amount, boost.secondsRemaining);
                int boostX = pos + font.width(baseText); // começa logo após o valor
                guiGraphics.drawString(font, boostText, boostX, y, 0x00CC66  , false); // verde
            }

            y += LINE_HEIGHT;
            if (y > clipBottom) break;
        }

        if (resetButton != null && resetButton.isHovered()) {
            boolean hasXp = player.experienceLevel >= 50;
            MutableComponent text = hasXp
                    ? Component.translatable("gui.playerstats.can_reset")
                    : Component.translatable("gui.playerstats.cant_reset");
            color = hasXp ? 0x00FF00 : 0xFF5555;
            guiGraphics.drawString(font, text, mouseX + 10, mouseY, color, false);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Verifica se foi o botão direito do mouse (2)
        if (button == 1) {
            // Verifica se o mouse está dentro da caixa de texto
            if (searchBox.isMouseOver(mouseX, mouseY)) {
                searchBox.setValue("");       // limpa o texto
                scrollOffset = 0;             // reseta scroll (opcional)
                rebuildButtons();             // atualiza a lista
                return true;
            }
        }

        // Deixa o restante do sistema processar cliques normalmente
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
