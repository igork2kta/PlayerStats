package com.playerstats.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.playerstats.Config;
import com.playerstats.PlayerStats;
import com.playerstats.client.widget.CustomSearchBox;
import com.playerstats.client.widget.CustomButton;
import com.playerstats.network.*;
import com.playerstats.util.AttributeUtils;
import com.playerstats.util.UniqueAbilitiesUtils;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private List<Attribute> cachedAttributes = List.of();
    private List<AttributeInstance> cachedAbilities = List.of();

    public StatsScreen() {
        super(Component.literal("Player Stats"));
    }

    public StatsScreen(LivingEntity entity) {
        super(Component.literal("Player Stats"));
        this.entity = entity;
    }

    private void updateFilteredLists() {
        if (showAttributes) {
            this.cachedAttributes = AttributeUtils.getAttributes(entity, searchText);
        } else {
            this.cachedAbilities = AttributeUtils.getCustomAttributes(entity, searchText);
        }
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
            updateFilteredLists();
            rebuildButtons();

        });

        attributesButton = new CustomButton(
                leftPos + (BG_WIDTH / 4) - 25,   // X
                topPos + 65,         // Y
                70,                              // largura
                20,                              // altura
                Component.literal("Attributes"),
                ResourceLocation.fromNamespaceAndPath("playerstats", "textures/gui/reset_button.png"),
                btn -> {showAttributes = true; scrollOffset = 0; updateFilteredLists(); rebuildButtons();}
        );

        uniqueAbilitiesButton = new CustomButton(
                leftPos + (BG_WIDTH / 2) + 25,   // X
                topPos + 65,         // Y
                85,                              // largura
                20,                              // altura
                Component.literal("Unique Abilities"),
                ResourceLocation.fromNamespaceAndPath("playerstats", "textures/gui/reset_button.png"),
                btn -> {showAttributes = false; scrollOffset = 0; updateFilteredLists(); rebuildButtons();}
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

        updateFilteredLists();
        rebuildButtons();


    }

    //Sem isso o background fica desfocado
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //this.renderBlurredBackground(partialTick);
        super.renderMenuBackground(guiGraphics);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.client.event.ScreenEvent.BackgroundRendered(this, guiGraphics));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

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
            int y = clipTop - scrollOffset + 2; //2 para alinhamento do texto com os botões

            for (Attribute attr : cachedAttributes) {
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


                // Tooltip informativa do atributo (Sempre ativa)
                int mouseX1 = pos;
                int mouseX2 = leftPos + BG_WIDTH - 25;
                int mouseY1 = y;
                int mouseY2 = y + LINE_HEIGHT;

                if (mouseX >= mouseX1 && mouseX <= mouseX2 && mouseY >= mouseY1 && mouseY <= mouseY2) {
                    double increment = AttributeUtils.getIncrement(instance);
                    double baseValue = instance.getBaseValue();
                    double currentValue = instance.getValue();
                    double nextValue = currentValue + increment;

                    String sanitizedPath = AttributeUtils.sanitizeToPath(attr.getDescriptionId());
                    AttributeModifier modifier = instance.getModifiers().stream()
                            .filter(mod -> mod.id().getNamespace().equals("playerstats")
                                    && (mod.id().getPath().equals(attr.getDescriptionId()) || mod.id().getPath().equals(sanitizedPath)))
                            .findFirst()
                            .orElse(null);

                    double pointsBonus = modifier != null ? modifier.amount() : 0.0;
                    int pointsInvested = Math.abs(increment) > 0.0001 && Math.abs(pointsBonus) > 0.0001
                            ? (int) Math.round(pointsBonus / increment) : 0;

                    List<Component> tooltip = new ArrayList<>();
                    // Título em destaque
                    tooltip.add(Component.literal(name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

                    boolean isMaxed = AttributeUtils.isMaxed(instance);
                    Double limitValue = AttributeUtils.getAttributeLimit(instance);

                    if (isMaxed) {
                        tooltip.add(Component.translatable("gui.playerstats.tooltip.max_reached")
                                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                    } else {
                        // Ganho por ponto e prévia do próximo nível
                        String incFormatted = (increment > 0 ? "+" : "") + String.format(Locale.ROOT, "%.2f", increment);
                        tooltip.add(Component.translatable("gui.playerstats.tooltip.increment", incFormatted)
                                .withStyle(ChatFormatting.GREEN));

                        tooltip.add(Component.translatable("gui.playerstats.tooltip.next_value",
                                String.format(Locale.ROOT, "%.2f", nextValue), incFormatted)
                                .withStyle(ChatFormatting.AQUA));
                    }

                    // Valores atuais e detalhes
                    tooltip.add(Component.translatable("gui.playerstats.tooltip.base_value", String.format(Locale.ROOT, "%.2f", baseValue))
                            .withStyle(ChatFormatting.GRAY));

                    if (limitValue != null) {
                        if (increment < 0) {
                            tooltip.add(Component.translatable("gui.playerstats.tooltip.min_value", String.format(Locale.ROOT, "%.2f", limitValue))
                                    .withStyle(ChatFormatting.GOLD));
                        } else {
                            tooltip.add(Component.translatable("gui.playerstats.tooltip.max_value", String.format(Locale.ROOT, "%.2f", limitValue))
                                    .withStyle(ChatFormatting.GOLD));
                        }
                    }

                    if (Math.abs(pointsBonus) > 0.0001) {
                        String bonusFormatted = (pointsBonus > 0 ? "+" : "") + String.format(Locale.ROOT, "%.2f", pointsBonus);
                        tooltip.add(Component.translatable("gui.playerstats.tooltip.points_bonus", bonusFormatted, pointsInvested)
                                .withStyle(ChatFormatting.DARK_GREEN));
                    }

                    ClientBoostCache.BoostInfo boost = ClientBoostCache.activeBoosts.get(attr);
                    if (boost != null) {
                        tooltip.add(Component.translatable("gui.playerstats.tooltip.boost",
                                String.format(Locale.ROOT, "%.2f", boost.amount), boost.secondsRemaining)
                                .withStyle(ChatFormatting.LIGHT_PURPLE));
                    }

                    tooltip.add(Component.translatable("gui.playerstats.tooltip.current_value", String.format(Locale.ROOT, "%.2f", currentValue))
                            .withStyle(ChatFormatting.WHITE));

                    // ID técnico
                    tooltip.add(Component.translatable("gui.playerstats.tooltip.attribute_id", attr.getDescriptionId())
                            .withStyle(ChatFormatting.DARK_GRAY));

                    // Mod de Origem
                    ResourceLocation attrLocation = BuiltInRegistries.ATTRIBUTE.getKey(attr);
                    String modNamespace = attrLocation != null ? attrLocation.getNamespace() : "minecraft";
                    String modName = net.neoforged.fml.ModList.get().getModContainerById(modNamespace)
                            .map(c -> c.getModInfo().getDisplayName())
                            .orElse(modNamespace.equals("minecraft") ? "Minecraft" : org.apache.commons.lang3.StringUtils.capitalize(modNamespace));

                    tooltip.add(Component.literal(modName).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));

                    guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
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

            int y = clipTop - scrollOffset + 3; //2 para alinhamento do texto com os botões

            for (AttributeInstance instance : cachedAbilities) {

                String name = AttributeUtils.getAttributeName(instance.getAttribute().value());

                if (y + 15 <= clipTop) {
                    y += LINE_HEIGHT;
                    continue;
                }

                int pos = leftPos + 40;
                String descId = instance.getAttribute().value().getDescriptionId();
                if (UniqueAbilitiesUtils.isLeveledAbility(descId)) {
                    int maxLvl = UniqueAbilitiesUtils.getMaxLevel(descId);
                    int currentVal = (int) Math.round(instance.getValue());
                    int cachedLvl = ClientAttributeCache.getAbilityLevel(descId);
                    int activeLvl = Math.max(currentVal, cachedLvl);

                    if (currentVal < 0 && activeLvl <= 0) {
                        // Não adquirida: CINZA
                        guiGraphics.drawString(font, name + " (Nv. 0/" + maxLvl + ")", pos, y, 0X888888, false);
                    } else if (currentVal == 0 || (currentVal < 0 && activeLvl > 0)) {
                        // Desativada: CINZA
                        guiGraphics.drawString(font, name + " (Nv. " + activeLvl + "/" + maxLvl + ")", pos, y, 0X888888, false);
                    } else {
                        // Ativa: COR PADRÃO
                        guiGraphics.drawString(font, name + " (Nv. " + activeLvl + "/" + maxLvl + ")", pos, y, 0X291d13, false);
                    }
                } else {
                    if(instance.getValue() == -1)
                        // Não adquirida: CINZA
                        guiGraphics.drawString(font, name, pos, y, 0X888888, false);
                    else if(instance.getValue() == 1)
                        // Ativa: COR PADRÃO
                        guiGraphics.drawString(font, Component.literal(name).append(Component.translatable("gui.playerstats.obtained_active")), pos, y, 0X291d13, false);
                    else
                        // Desativada: CINZA
                        guiGraphics.drawString(font, Component.literal(name).append(Component.translatable("gui.playerstats.obtained_inactive")), pos, y, 0X888888, false);
                }

                int mouseX1 = pos;
                int mouseX2 = pos + font.width(name) + 60;
                int mouseY1 = y;
                int mouseY2 = y + LINE_HEIGHT;

                if (mouseX >= mouseX1 && mouseX <= mouseX2 && mouseY >= mouseY1 && mouseY <= mouseY2) {
                    int currentVal = (int) Math.round(instance.getValue());
                    int cachedLvl = ClientAttributeCache.getAbilityLevel(descId);
                    int maxLvl = UniqueAbilitiesUtils.getMaxLevel(descId);

                    Component statusComp;
                    if (currentVal < 0 && cachedLvl <= 0) {
                        statusComp = Component.translatable("gui.playerstats.ability_not_unlocked").withStyle(s -> s.withColor(0xAAAAAA));
                    } else if (currentVal == 0 || (currentVal < 0 && cachedLvl > 0)) {
                        statusComp = Component.translatable("gui.playerstats.ability_status_inactive").withStyle(s -> s.withColor(0xFF5555));
                    } else {
                        statusComp = Component.translatable("gui.playerstats.ability_status_active").withStyle(s -> s.withColor(0x55FF55));
                    }

                    List<Component> tooltip = new java.util.ArrayList<>();
                    tooltip.add(Component.translatable(descId).withStyle(s -> s.withBold(true).withColor(0xFFFFFF)));
                    tooltip.add(statusComp);

                    if (descId.contains("vampirism")) {
                        int displayLvl = Math.max(Math.max(1, currentVal), cachedLvl);
                        int procChance = 30 + (displayLvl - 1) * 5;
                        int healPercent = 10 + (displayLvl - 1) * 2;
                        tooltip.add(Component.translatable("gui.playerstats.ability.vampirism_desc", procChance, healPercent, displayLvl, maxLvl));
                    } else if (descId.contains("milky_aura")) {
                        int displayLvl = Math.max(Math.max(1, currentVal), cachedLvl);
                        int radius = 1 + (displayLvl * 2); // 3, 5, 7 blocos
                        tooltip.add(Component.translatable("gui.playerstats.ability.milky_aura_desc", radius, displayLvl, maxLvl));
                    } else if (descId.contains("howl_buff")) {
                        int displayLvl = Math.max(Math.max(1, currentVal), cachedLvl);
                        int radius = 4 + (displayLvl * 4); // 8, 12, 16 blocos
                        int duration = 15 + (displayLvl * 15); // 30s, 45s, 60s
                        int cooldown = Math.max(60, 240 - displayLvl * 60); // 180s, 120s, 60s
                        tooltip.add(Component.translatable("gui.playerstats.ability.howl_buff_desc", radius, duration, cooldown, displayLvl, maxLvl));
                    } else if (descId.contains("scout_vision")) {
                        int displayLvl = Math.max(Math.max(1, currentVal), cachedLvl);
                        int radius = 16 + ((displayLvl - 1) * 8); // 16, 24, 32 blocos
                        tooltip.add(Component.translatable("gui.playerstats.ability.scout_vision_desc", radius, displayLvl, maxLvl));
                    } else {
                        String shortKey = descId.replace("attribute.playerstats.", "");
                        String descKey = "gui.playerstats.ability." + shortKey + "_desc";
                        tooltip.add(Component.translatable(descKey).withStyle(s -> s.withColor(0xDDDDDD)));
                    }

                    if (currentVal >= 0.0D || cachedLvl > 0) {
                        tooltip.add(Component.translatable("gui.playerstats.ability_toggle_hint").withStyle(s -> s.withItalic(true).withColor(0xAAAAAA)));
                    }

                    if (Config.DEBUG_MODE.get()) {
                        tooltip.add(Component.literal("ID: " + descId).withStyle(s -> s.withColor(0x888888)));
                    }

                    guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                }

                y += LINE_HEIGHT;
                if (y >= clipBottom) break;
            }
        }


    }


    private void rebuildButtons() {
        this.clearWidgets();
        addRenderableWidget(attributesButton);
        addRenderableWidget(uniqueAbilitiesButton);
        addRenderableWidget(searchBox);

        if (showAttributes) {
            int visibleLines = (clipBottom - clipTop) / LINE_HEIGHT;
            maxScroll = Math.max(0, (cachedAttributes.size() - visibleLines) * LINE_HEIGHT);

            int y = clipTop - scrollOffset;

            for (Attribute attr : cachedAttributes) {

                if (y + 17 <= clipTop) {
                    y += LINE_HEIGHT;
                    continue;
                }
                if (y + 4 >= clipBottom) break;

                AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);

                if (Config.DEBUG_MODE.get()) {
                    double increment = AttributeUtils.getIncrement(instance);
                    PlayerStats.LOGGER.info("Configurando atributo: {} Incremento: {}", attr.getDescriptionId(), increment);
                }

                CustomButton plusButton = new CustomButton(
                        leftPos + 20, // X
                        y,  // Y
                        14, // Largura
                        14, // Altura
                        Component.empty(),
                        PLUS_BUTTON_TEXTURE,
                        button -> {
                            sendAttributeChange(attr);
                        }
                );
                plusButton.setActiveSupplier(() -> !AttributeUtils.isMaxed(instance));
                this.addRenderableWidget(plusButton);

                y += LINE_HEIGHT;
            }
            addRenderableWidget(resetButton);
        }
        else {
            int visibleLines = (clipBottom - clipTop) / LINE_HEIGHT;
            maxScroll = Math.max(0, (cachedAbilities.size() - visibleLines) * LINE_HEIGHT);

            int y = clipTop - scrollOffset;

            for (AttributeInstance instance : cachedAbilities) {

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
                            sendAttributeChange(instance.getAttribute().value());
                        }
                ));

                y += LINE_HEIGHT;
            }
        }


    }

    private void sendAttributeChange(Attribute attribute) {
        AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attribute);
        if (instance != null && AttributeUtils.isMaxed(instance)) {
            return;
        }
        ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
        if (id != null) {
            PacketHandler.sendToServer(new ModifyAttributePacket(entity.getId(), id.toString(), false));
        } else {
            System.out.println("Atributo sem ResourceLocation válido!");
        }
    }

    private void sendAttributeToggle(Attribute attribute) {
        ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
        if (id != null) {
            PacketHandler.sendToServer(new ModifyAttributePacket(entity.getId(), id.toString(), true));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset -= scrollY * SCROLL_STEP;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        rebuildButtons();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Verifica se foi o botão direito do mouse (1)
        if (button == 1) {
            // Verifica se o mouse está dentro da caixa de texto
            if (searchBox.isMouseOver(mouseX, mouseY)) {
                searchBox.setValue("");       // limpa o texto
                scrollOffset = 0;             // reseta scroll (opcional)
                updateFilteredLists();
                rebuildButtons();             // atualiza a lista
                return true;
            }

            // Se estiver na aba de habilidades únicas, clique com o botão direito alterna On/Off
            if (!showAttributes) {
                int y = clipTop - scrollOffset;
                for (AttributeInstance instance : cachedAbilities) {
                    if (y + 17 > clipTop && y + 4 < clipBottom) {
                        // Verifica se clicou na área do botão '+' ou no texto da linha da habilidade
                        int buttonX = leftPos + 20;
                        int textX = leftPos + 40;
                        String name = AttributeUtils.getAttributeName(instance.getAttribute().value());
                        int textWidth = font.width(name) + 120;

                        boolean clickedButton = (mouseX >= buttonX && mouseX <= buttonX + 14 && mouseY >= y && mouseY <= y + 14);
                        boolean clickedText = (mouseX >= textX && mouseX <= textX + textWidth && mouseY >= y && mouseY <= y + LINE_HEIGHT);

                        if (clickedButton || clickedText) {
                            sendAttributeToggle(instance.getAttribute().value());
                            return true;
                        }
                    }
                    y += LINE_HEIGHT;
                }
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
