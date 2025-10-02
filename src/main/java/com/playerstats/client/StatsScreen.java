package com.playerstats.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.playerstats.Config;
import com.playerstats.PlayerStats;
import com.playerstats.client.widget.CustomSearchBox;
import com.playerstats.client.widget.CustomButton;
import com.playerstats.network.*;
import com.playerstats.util.AttributeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class StatsScreen extends Screen {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("playerstats", "textures/gui/stats_background.png");
    private static final ResourceLocation PLUS_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath("playerstats", "textures/gui/plus_button.png");
    //Tamanho do background
    private static final int BG_WIDTH = 300;
    private static final int BG_HEIGHT = 300;
    private static final int SCROLL_STEP = 17;
    private static final int LINE_HEIGHT = 17;

    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int clipTop;
    private int clipBottom;

    private Button resetButton, attributesButton, uniqueAbilitiesButton;
    private EditBox searchBox;
    private String searchText = "";

    //Entidade que ser alterada
    private LivingEntity entity;
    //Player de onde serão subtraídos XP e pontos
    private Player player;

    private final boolean consumeXp = Config.CONSUME_XP.get();
    private final int requiredXpforReset = Config.REQUIRED_XP_FOR_RESET.get();
    private final int xpIncrement = Config.XP_COST_INCREMENT.get();

    private boolean showAttributes = true;

    public StatsScreen() {
        super(Component.literal("Player Stats"));
    }

    public StatsScreen(LivingEntity entity) {
        super(Component.literal("Player Stats"));
        this.entity = entity;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - BG_WIDTH) / 2;
        this.topPos = (this.height - BG_HEIGHT) / 2;

        //Posição de corrte do SCROLL
        this.clipTop = topPos + 115;
        this.clipBottom = topPos + BG_HEIGHT - 60;

        //Se não tiver entidade, é o proprio player
        player = Minecraft.getInstance().player;
        if (entity == null) entity = player;

        this.searchBox = new CustomSearchBox(this.font, leftPos + 25, topPos + 95, 260, 19);

        //Toda vez que o usuario digita algo, muda para minusculo, reseta a barra de rolagem e redesenha os botões
        this.searchBox.setResponder(text -> {
            this.searchText = text.toLowerCase();
            this.scrollOffset = 0;
            rebuildButtons();

        });

        attributesButton = new CustomButton(
                leftPos + (BG_WIDTH / 4) - 25,   // X
                topPos + 65,         // Y
                70,                              // largura
                20,                              // altura
                Component.literal("Attributes"),
                ResourceLocation.fromNamespaceAndPath("playerstats", "textures/gui/reset_button.png"),
                btn -> {showAttributes = true; scrollOffset = 0; rebuildButtons();}
        );

        uniqueAbilitiesButton = new CustomButton(
                leftPos + (BG_WIDTH / 2) + 25,   // X
                topPos + 65,         // Y
                85,                              // largura
                20,                              // altura
                Component.literal("Unique Abilities"),
                ResourceLocation.fromNamespaceAndPath("playerstats", "textures/gui/reset_button.png"),
                btn -> {showAttributes = false; scrollOffset = 0; rebuildButtons();}
        );

        resetButton = new CustomButton(
                leftPos + (BG_WIDTH / 2) - 35,   // X
                topPos + BG_HEIGHT - 50,         // Y
                70,                              // largura
                20,                              // altura
                Component.translatable("gui.playerstats.reset"),
                ResourceLocation.fromNamespaceAndPath("playerstats", "textures/gui/reset_button.png"),
                btn -> PacketHandler.sendToServer(new ResetAttributesPacket(entity.getId()))
        );

        rebuildButtons();


    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        this.renderBackground(guiGraphics);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT);

        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTicks);

        //Nome da entidade
        String displayName = entity.getDisplayName().getString();
        int textWidth = font.width(displayName);

        // centraliza dentro do background
        int textX = leftPos + (BG_WIDTH - textWidth) / 2;
        guiGraphics.drawString(font, displayName, textX, topPos + 24, 0xA0A0A0, false);

        if(showAttributes){

            int points = ClientAttributeCache.getPoints();
            int color = points > 0 ? 0x00CC66 : 0xFF5555;
            Component attributepoints = Component.translatable("gui.playerstats.points", points);
            textWidth = font.width(attributepoints);
            textX = leftPos + (BG_WIDTH - textWidth) / 2;
            guiGraphics.drawString(font, attributepoints, textX, topPos + 35, color);

            if(consumeXp){
                int upgradeCount = ClientAttributeCache.getUpgradeCount();
                int xpCost = (upgradeCount + 1) * xpIncrement;
                boolean hasXpForUpgrade = player.experienceLevel > xpCost;
                color = hasXpForUpgrade ? 0x00CC66 : 0xFF5555; // verde ou vermelho
                Component xp = Component.translatable("gui.playerstats.xp_cost", xpCost);
                textWidth = font.width(xp);
                textX = leftPos + (BG_WIDTH - textWidth) / 2;
                guiGraphics.drawString(font, xp, textX, topPos + 46, color);
            }
            List<Attribute> filteredAttributes = AttributeUtils.getAttributes(entity, searchText);


            int y = clipTop - scrollOffset + 2; //2 para alinhamento do texto com os botões

            for (Attribute attr : filteredAttributes) {
                AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);
                String name = AttributeUtils.getAttributeName(attr);

                if (y + 15 <= clipTop) {
                    y += LINE_HEIGHT;
                    continue;
                }

                String value = String.format("%.2f", instance.getValue());
                int pos = leftPos + 40;

                // Parte base (nome + valor normal)
                String baseText = name + ": " + value;
                guiGraphics.drawString(font,baseText, pos, y, 0X291d13, false);

                if(entity.equals(player)){
                    // Parte do boost (somente se existir)
                    ClientBoostCache.BoostInfo boost = ClientBoostCache.activeBoosts.get(attr);
                    if (boost != null) {
                        String boostText = String.format(" (+%.2f %ds)", boost.amount, boost.secondsRemaining);
                        int boostX = pos + font.width(baseText); // começa logo após o valor
                        guiGraphics.drawString(font, boostText, boostX, y, 0x00CC66  , false); // verde
                    }
                }


                // Tooltip em modo debug
                if (Config.DEBUG_MODE.get()) {
                    int mouseX1 = pos;
                    int mouseX2 = pos +  font.width(name);;
                    int mouseY1 = y;
                    int mouseY2 = y + LINE_HEIGHT;

                    if (mouseX >= mouseX1 && mouseX <= mouseX2 && mouseY >= mouseY1 && mouseY <= mouseY2) {
                        double increment = AttributeUtils.getIncrement(attr.getDescriptionId());

                        List<Component> tooltip = List.of(
                                Component.literal("ID: " + attr.getDescriptionId()),
                                Component.literal(String.format(java.util.Locale.ROOT, "Increment: %.2f", increment))
                        );

                        guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                    }
                }

                y += LINE_HEIGHT;
                if (y >= clipBottom) break;
            }


            if (resetButton != null && resetButton.isHovered()) {
                boolean hasXp = player.experienceLevel >= requiredXpforReset && consumeXp;
                MutableComponent text = hasXp
                        ? Component.translatable("gui.playerstats.can_reset", requiredXpforReset).withStyle(s -> s.withColor(0x00FF00))
                        : Component.translatable("gui.playerstats.cant_reset", requiredXpforReset).withStyle(s -> s.withColor(0xFF5555));


                guiGraphics.renderTooltip(font, text, mouseX, mouseY);
            }
        }

        else{

            int points = ClientAttributeCache.getAbilityPoints();
            int color = points > 0 ? 0x00CC66 : 0xFF5555;
            Component abilityPoints = Component.translatable("gui.playerstats.points", points);
            textWidth = font.width(abilityPoints);
            textX = leftPos + (BG_WIDTH - textWidth) / 2;
            guiGraphics.drawString(font, abilityPoints, textX, topPos + 35, color);

            if(consumeXp){
                int xpCost = Config.REQUIRED_XP_FOR_ABILITY.get();
                boolean hasXpForUpgrade = player.experienceLevel > xpCost;
                color = hasXpForUpgrade ? 0x00CC66 : 0xFF5555; // verde ou vermelho
                Component xp = Component.translatable("gui.playerstats.xp_cost", xpCost);
                textWidth = font.width(xp);
                textX = leftPos + (BG_WIDTH - textWidth) / 2;
                guiGraphics.drawString(font, xp, textX, topPos + 46, color);
            }

            List<AttributeInstance> filteredAbilities = AttributeUtils.getCustomAttributes(entity, searchText);

            int y = clipTop - scrollOffset + 3; //2 para alinhamento do texto com os botões

            for (AttributeInstance instance: filteredAbilities) {

                String name = AttributeUtils.getAttributeName(instance.getAttribute());

                if (y + 15 <= clipTop) {
                    y += LINE_HEIGHT;
                    continue;
                }

                int pos = leftPos + 40;
                if(instance.getValue() == -1)
                    guiGraphics.drawString(font,name, pos, y, 0X291d13, false);
                else if(instance.getValue() == 1)
                    guiGraphics.drawString(font, Component.literal(name).append(Component.translatable("gui.playerstats.obtained_active")), pos, y, 0X291d13, false);
                else
                    guiGraphics.drawString(font,Component.literal(name).append(Component.translatable("gui.playerstats.obtained_inactive")), pos, y, 0X291d13, false);

                // Tooltip em modo debug
                if (Config.DEBUG_MODE.get()) {
                    int mouseX1 = pos;
                    int mouseX2 = pos +  font.width(name);;
                    int mouseY1 = y;
                    int mouseY2 = y + LINE_HEIGHT;

                    if (mouseX >= mouseX1 && mouseX <= mouseX2 && mouseY >= mouseY1 && mouseY <= mouseY2) {
                        double increment = AttributeUtils.getIncrement(instance.getAttribute().getDescriptionId());

                        List<Component> tooltip = List.of(
                                Component.literal("ID: " + instance.getAttribute().getDescriptionId()),
                                Component.literal(String.format(java.util.Locale.ROOT, "increment: %.2f", increment))
                        );

                        guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                    }
                }

                y += LINE_HEIGHT;
                if (y >= clipBottom) break;
            }
        }
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

    }


    private void rebuildButtons() {
        this.clearWidgets();
        addRenderableWidget(attributesButton);
        addRenderableWidget(uniqueAbilitiesButton);
        addRenderableWidget(searchBox);

        if(showAttributes){

        List<Attribute> filteredAttributes = AttributeUtils.getAttributes(entity, searchText);

        int visibleLines = (clipBottom - clipTop) / LINE_HEIGHT;
        maxScroll = Math.max(0, (filteredAttributes.size() - visibleLines) * LINE_HEIGHT);

        int y = clipTop - scrollOffset;

        for (Attribute attr : filteredAttributes) {

                if (y + 17 <= clipTop) {
                    y += LINE_HEIGHT;
                    continue;
                }
                if (y + 4 >= clipBottom) break;

                if (Config.DEBUG_MODE.get()) {
                    double increment = AttributeUtils.getIncrement(attr.getDescriptionId());
                    PlayerStats.LOGGER.info("Configurando atributo: {} Incremento: {}", attr.getDescriptionId(), increment);
                }

                this.addRenderableWidget(new CustomButton(
                        leftPos + 20, // X
                        y,  // Y
                        14, // Largura
                        14, // Altura
                        Component.empty(),
                        PLUS_BUTTON_TEXTURE,
                        button -> {
                            sendAttributeChange(attr);
                        }
                ));

                y += LINE_HEIGHT;
            }
            addRenderableWidget(resetButton);
        }
        else{

            List<AttributeInstance> filteredAbilities = AttributeUtils.getCustomAttributes(entity, searchText);

            int visibleLines = (clipBottom - clipTop) / LINE_HEIGHT;
            maxScroll = Math.max(0, (filteredAbilities.size() - visibleLines) * LINE_HEIGHT);

            int y = clipTop - scrollOffset;

            for (AttributeInstance instance: filteredAbilities) {

                if (y + 17 <= clipTop) {
                    y += LINE_HEIGHT;
                    continue;
                }
                if (y + 4 >= clipBottom) break;


                this.addRenderableWidget(new CustomButton(
                        leftPos + 20, // X
                        y,  // Y
                        14, // Largura
                        14, // Altura
                        Component.empty(),
                        PLUS_BUTTON_TEXTURE,
                        button -> {
                            sendAttributeChange(instance.getAttribute());
                        }
                ));

                y += LINE_HEIGHT;
            }
        }


    }

    private void sendAttributeChange(Attribute attribute) {
        ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
        if (id != null) {
                PacketHandler.sendToServer(new ModifyAttributePacket(entity.getId(), id.toString()));

        } else {
            System.out.println("Atributo sem ResourceLocation válido!");
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
