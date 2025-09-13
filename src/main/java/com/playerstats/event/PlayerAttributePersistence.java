package com.playerstats.event;

import com.playerstats.Config;
import com.playerstats.PlayerStats;
import com.playerstats.client.ClientAttributeCache;
import com.playerstats.client.KeyMappings;
import com.playerstats.network.PacketHandler;
import com.playerstats.network.UpdatePointsPacket;
import com.playerstats.network.UpdateUpgradeCountPacket;
import com.playerstats.util.AttributeUtils;
import com.playerstats.util.UniqueAbilitiesUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;


public class PlayerAttributePersistence {

    private static final String ATTRIBUTE_UPGRADES_TAG = "PlayerStatsUpgrades";
    private static final String POINTS_TAG = "PlayerStatsPoints";
    private static final String UPGRADE_COUNT_TAG = "PlayerStatsUpgradeCount";
    private static final String ABILITY_POINTS_TAG = "PlayerAbilityPoints";

    public static void register() {
        // Escuta o evento de registro de pacotes
        EVENT_BUS.register(PlayerAttributePersistence.class);

    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag originalNBT = event.getOriginal().getPersistentData();
        Player player = event.getEntity();

        ClientAttributeCache.clean();
        CompoundTag root = player.getPersistentData();
        root.put(ATTRIBUTE_UPGRADES_TAG, originalNBT.getCompound(ATTRIBUTE_UPGRADES_TAG));

        if(Config.RESET_ON_DEATH.get()){
            resetAttributes(player, (ServerPlayer) player,true);
        }
        else{
            setPoints(player, getPoints(event.getOriginal()));
            setAbilityPoints(player, getPoints(event.getOriginal()));
            CompoundTag upgradesTag = root.getCompound(ATTRIBUTE_UPGRADES_TAG);

            for (String key : upgradesTag.getAllKeys()) {
                ResourceLocation id = ResourceLocation.tryParse(key);
                Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
                AttributeInstance instance = AttributeUtils.getAttributeInstance(player, attr);

                if (attr != null && instance != null) {
                    int upgradeCount = upgradesTag.getInt(key);
                    double increment = AttributeUtils.getIncrement(attr.getDescriptionId());

                    PlayerStats.LOGGER.info("Configurando atributo:" + attr.getDescriptionId() + " valor atual: " +  instance.getBaseValue() +  " upgrade count: " + upgradeCount + " increment: " + increment );
                    double totalIncrement = upgradeCount * increment;
                    applyModifier(instance, attr.getDescriptionId(), totalIncrement);
                }
            }
        }

        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(getUpgradeCount(player)), (ServerPlayer) player);
    }


    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {

        Player player = event.getEntity();

        //CompoundTag root = player.getPersistentData();

        //ensurePointsInitialized(player);

        ClientAttributeCache.clean();

        /*
        CompoundTag upgradesTag = root.getCompound(ATTRIBUTE_UPGRADES_TAG);
        for (String key : upgradesTag.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
            AttributeInstance instance = AttributeUtils.getAttributeInstance(player, attr);

            if (attr != null && instance != null) {
                int upgradeCount = upgradesTag.getInt(key);
                double increment = AttributeUtils.getIncrement(attr.getDescriptionId());

                PlayerStats.LOGGER.info("Configurando atributo:" + attr.getDescriptionId() + " valor atual: " +  instance.getBaseValue() +  " upgrade count: " + upgradeCount + " increment: " + increment );
                double totalIncrement = upgradeCount * increment;
                applyModifier(instance, attr.getDescriptionId(), totalIncrement);
            }
        }*/

        //Envia os dados para o cliente no login
        PacketHandler.sendToClient(new UpdatePointsPacket(getPoints(player), "attribute"), (ServerPlayer) player);
        PacketHandler.sendToClient(new UpdatePointsPacket(getAbilityPoints(player), "ability"), (ServerPlayer) player);
        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(getUpgradeCount(player)), (ServerPlayer) player);
    }

    public static void upgradeAttribute(LivingEntity entity, ServerPlayer player, String attributeId){

        ResourceLocation id = ResourceLocation.tryParse(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);

        if (attr != null) {

            AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);

            if(!attributeId.startsWith("playerstats:")) {
                int playerPoints = getPoints(player);
                int playerUpgrades = getUpgradeCount(entity);
                int xpIncrement = Config.XP_COST_INCREMENT.get();

                int xpCost = (playerUpgrades + 1) * xpIncrement; //Starts with 5 and increment by 5

                boolean consumeXp = Config.CONSUME_XP.get();
                if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE)
                    consumeXp = false;//Creative mod doesn't need XP

                if(instance == null) {
                    System.err.println("AttributeInstance is null for: " + id);
                    return;
                }

                if (playerPoints > 0 && (player.experienceLevel >= xpCost || !consumeXp)) {

                    // Ao aplicar o upgrade
                    applyUpgrade(entity, attr);
                    setPoints(player, playerPoints - 1);

                    PacketHandler.sendToClient(new UpdatePointsPacket(getPoints(player), "attribute"), player);

                    int count = PlayerAttributePersistence.getUpgradeCount(entity);
                    PacketHandler.sendToClient(new UpdateUpgradeCountPacket(count), player);

                    if (consumeXp) consumeExperience(player, xpCost);
                }
            }
            else{
                int playerPoints = getAbilityPoints(player);

                //Não possui, comprando
                if(instance.getValue() == -1.0D){

                    int xpCost = Config.REQUIRED_XP_FOR_ABILITY.get();

                    boolean consumeXp = Config.CONSUME_XP.get();
                    if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE)
                        consumeXp = false;//Creative mod doesn't need XP


                    if(playerPoints > 0 && (player.experienceLevel >= xpCost || !consumeXp)) {

                        //Base value = -1 + 2 = 1 = active
                        if(!UniqueAbilitiesUtils.enableDisableAbility(entity, player, attributeId, true))return;

                        applyModifier(instance, attr.getDescriptionId(), 2);
                        setAbilityPoints(player, playerPoints - 1);
                        if (consumeXp) consumeExperience(player, xpCost);
                    }
                }
                //Possui, ativando
                else if(instance.getValue() == 0.0D){
                    //Base value = -1 + 2 = 1 = active
                    UniqueAbilitiesUtils.enableDisableAbility(entity, player, attributeId, true);
                    applyModifier(instance, attr.getDescriptionId(), 2);
                }
                //Possui, desativando
                else if(instance.getValue() == 1.0D){
                    //Base value = -1 + 1 = 0 = inactive
                    UniqueAbilitiesUtils.enableDisableAbility(entity, player, attributeId, false);
                    applyModifier(instance, attr.getDescriptionId(), 1);
                }
            }
        }
        else {
            System.err.println("Unknown attribute ID: " + id);
        }

    }


    public static void applyUpgrade(LivingEntity player, Attribute attr) {
        ResourceLocation key = BuiltInRegistries.ATTRIBUTE.getKey(attr);
        CompoundTag upgrades = player.getPersistentData().getCompound(ATTRIBUTE_UPGRADES_TAG);

        int currentUpgrades = upgrades.getInt(key.toString()) +1;
        upgrades.putInt(key.toString(), currentUpgrades);
        player.getPersistentData().put(ATTRIBUTE_UPGRADES_TAG, upgrades);

        AttributeInstance instance = AttributeUtils.getAttributeInstance(player,attr);
        if (instance != null) {
            double increment = AttributeUtils.getIncrement(attr.getDescriptionId());
            applyModifier(instance, attr.getDescriptionId(), increment * currentUpgrades);
        }
    }

    public static void resetAttributes(LivingEntity entity, ServerPlayer player, boolean resetByDeath) {

        boolean consumeXp = Config.CONSUME_XP.get();
        int requiredXp = Config.REQUIRED_XP_FOR_RESET.get();
        if (player.experienceLevel < requiredXp && player.gameMode.getGameModeForPlayer() != GameType.CREATIVE && !resetByDeath && consumeXp) {
            player.sendSystemMessage(Component.translatable("gui.playerstats.cant_reset", requiredXp));
            return;
        }

        CompoundTag upgrades = entity.getPersistentData().getCompound(ATTRIBUTE_UPGRADES_TAG);
        int refundedPoints = 0;

        for (String key : upgrades.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
            AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);
            if (attr != null && instance != null) {
                int upgradesApplied = upgrades.getInt(key);
                // Remove o modificador
                instance.getModifiers().stream()
                        .filter(mod -> mod.id().getNamespace().equals("playerstats") && mod.id().getPath().equals(attr.getDescriptionId()))
                        .toList()
                        .forEach(instance::removeModifier);
                refundedPoints += upgradesApplied;
            }
        }


        entity.getPersistentData().remove(ATTRIBUTE_UPGRADES_TAG);
        entity.getPersistentData().remove(UPGRADE_COUNT_TAG);

        if(!resetByDeath) {

            setPoints(player, getPoints(player) + refundedPoints);

            if(consumeXp) consumeExperience(player, requiredXp);
            player.sendSystemMessage(Component.translatable("gui.playerstats.reset", refundedPoints));
        }

        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(0), player);

    }

    public static void consumeExperience(Player player, int level){
        int points;
        if (level <= 16) {
            points = level * level + 6 * level;
        } else if (level <= 31) {
            points = (int)(2.5 * level * level - 40.5 * level + 360);
        } else {
            points = (int)(4.5 * level * level - 162.5 * level + 2220);
        }
        player.giveExperiencePoints(points * -1);
    }

    /*
    public static boolean ensurePointsInitialized(Player player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(POINTS_TAG)) {
            if (Config.DEBUG_MODE.get()) {
                    PlayerStats.LOGGER.info("Configuring player points");

            }
            setPoints(player, 0);
            return false;
        }
        if (Config.DEBUG_MODE.get())
            PlayerStats.LOGGER.info("Player points already configured");
        return true;
    }
*/

    private static void applyModifier(AttributeInstance instance, String attrId, double value) {
        // Remover qualquer modificador antigo com o mesmo nome
        instance.getModifiers().stream()
                .filter(mod -> mod.id().getNamespace().equals("playerstats") && mod.id().getPath().equals(attrId))
                .toList()
                .forEach(instance::removeModifier);

        // Cria um ResourceLocation único para esse atributo
        ResourceLocation modifierId =  ResourceLocation.fromNamespaceAndPath("playerstats", attrId);

        AttributeModifier modifier = new AttributeModifier(
                modifierId,
                value,
                AttributeModifier.Operation.ADD_VALUE
        );

        instance.addPermanentModifier(modifier);
    }

    public static int getPoints(Player player) {
        return player.getPersistentData().getInt(POINTS_TAG);
    }

    public static void setPoints(Player player, int points) {
        player.getPersistentData().putInt(POINTS_TAG, points);
        PacketHandler.sendToClient(new UpdatePointsPacket(points, "attribute"), (ServerPlayer) player);
    }

    public static int getAbilityPoints(Player player) {
        return player.getPersistentData().getInt(ABILITY_POINTS_TAG);
    }

    public static void setAbilityPoints(Player player, int points) {
        player.getPersistentData().putInt(ABILITY_POINTS_TAG, points);
        PacketHandler.sendToClient(new UpdatePointsPacket(points, "ability"), (ServerPlayer) player);
    }

    public static void addPoints(Player player, int points) {

        CompoundTag tag = player.getPersistentData();
        int playerPoints = tag.getInt(POINTS_TAG) + points;
        tag.putInt(POINTS_TAG, playerPoints);
        PacketHandler.sendToClient(new UpdatePointsPacket(playerPoints, "attribute"), (ServerPlayer) player);

        if (FMLEnvironment.dist == Dist.CLIENT)
            player.sendSystemMessage(Component.translatable("event.playerstats.ability_point_given", KeyMappings.OPEN_STATS_KEY.get().getKey().getDisplayName()));
    }

    public static void addAbilityPoints(Player player, int points) {
        CompoundTag tag = player.getPersistentData();
        int playerPoints = tag.getInt(ABILITY_POINTS_TAG) + points;
        tag.putInt(ABILITY_POINTS_TAG, playerPoints);
        PacketHandler.sendToClient(new UpdatePointsPacket(playerPoints, "ability"), (ServerPlayer) player);
        if (FMLEnvironment.dist == Dist.CLIENT)
            player.sendSystemMessage(Component.translatable("event.playerstats.ability_point_given", KeyMappings.OPEN_ENTITY_STATS_KEY.get().getKey().getDisplayName()));

    }

    public static int getUpgradeCount(LivingEntity entity) {
        return entity.getPersistentData().getInt(UPGRADE_COUNT_TAG);
    }



}
