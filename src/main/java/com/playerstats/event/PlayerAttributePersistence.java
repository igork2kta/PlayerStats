package com.playerstats.event;

import com.playerstats.Config;
import com.playerstats.PlayerStats;
import com.playerstats.client.ClientAttributeCache;
import com.playerstats.network.PacketHandler;
import com.playerstats.network.UpdatePointsPacket;
import com.playerstats.network.UpdateUpgradeCountPacket;
import com.playerstats.util.AttributeUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "playerstats")
public class PlayerAttributePersistence {

    private static final String ATTRIBUTE_UPGRADES_TAG = "PlayerStatsUpgrades";
    private static final String POINTS_TAG = "PlayerStatsPoints";
    private static final String UPGRADE_COUNT_TAG = "PlayerStatsUpgradeCount";

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag originalNBT = event.getOriginal().getPersistentData();
        if (originalNBT.contains(ATTRIBUTE_UPGRADES_TAG)) {

            Player player = event.getEntity();

            //player.getPersistentData().put(ATTRIBUTE_UPGRADES_TAG, originalNBT.getCompound(ATTRIBUTE_UPGRADES_TAG));

            ClientAttributeCache.clean();
            CompoundTag root = player.getPersistentData();
            root.put(ATTRIBUTE_UPGRADES_TAG, originalNBT.getCompound(ATTRIBUTE_UPGRADES_TAG));

            if(Config.RESET_ON_DEATH.get()){
                resetAttributes((ServerPlayer) player, true);
            }
            else{
                CompoundTag upgradesTag = root.getCompound(ATTRIBUTE_UPGRADES_TAG);
                for (String key : upgradesTag.getAllKeys()) {
                    ResourceLocation id = new ResourceLocation(key);
                    Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
                    AttributeInstance instance = player.getAttribute(attr);

                    if (attr != null && instance != null) {
                        int upgradeCount = upgradesTag.getInt(key);
                        double increment = AttributeUtils.getIncrement(attr.getDescriptionId());

                        PlayerStats.LOGGER.info("Configurando atributo:" + attr.getDescriptionId() + " valor atual: " +  instance.getBaseValue() +  " upgrade count: " + upgradeCount + " increment: " + increment );
                        double totalIncrement = upgradeCount * increment;
                        // Antes: instance.setBaseValue(...);
                        applyModifier(instance, attr.getDescriptionId(), totalIncrement);
                    }
                }
            }


            PacketHandler.sendToClient(new UpdatePointsPacket(getPoints(player)), (ServerPlayer) player);
            PacketHandler.sendToClient(new UpdateUpgradeCountPacket(getUpgradeCount(player)), (ServerPlayer) player);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerLoggedInEvent event) {

        Player player = event.getEntity();

        CompoundTag root = player.getPersistentData();

        ensurePointsInitialized(player);

        //if(ClientAttributeCache.pointsInitialized)return;

        ClientAttributeCache.clean();

        CompoundTag upgradesTag = root.getCompound(ATTRIBUTE_UPGRADES_TAG);
        for (String key : upgradesTag.getAllKeys()) {
            ResourceLocation id = new ResourceLocation(key);
            Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
            AttributeInstance instance = player.getAttribute(attr);

            if (attr != null && instance != null) {
                int upgradeCount = upgradesTag.getInt(key);
                double increment = AttributeUtils.getIncrement(attr.getDescriptionId());

                PlayerStats.LOGGER.info("Configurando atributo:" + attr.getDescriptionId() + " valor atual: " +  instance.getBaseValue() +  " upgrade count: " + upgradeCount + " increment: " + increment );
                double totalIncrement = upgradeCount * increment;
                // Antes: instance.setBaseValue(...);
                applyModifier(instance, attr.getDescriptionId(), totalIncrement);
            }
        }

        PacketHandler.sendToClient(new UpdatePointsPacket(getPoints(player)), (ServerPlayer) player);
        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(getUpgradeCount(player)), (ServerPlayer) player);
    }

    public static void upgradeAttribute(ServerPlayer player, String attributeId){

        ResourceLocation id = new ResourceLocation(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);

        if (attr != null) {

            AttributeInstance instance = player.getAttribute(attr);
            int playerPoints = getPoints(player);
            int playerXpLevel = player.experienceLevel;
            int playerUpgrades = getUpgradeCount(player);
            int xpCost = (playerUpgrades + 1) * 5; //Starts with 5 and increment by 5

            if(player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) playerXpLevel = xpCost; //Creative mod doesn't need XP

            if (instance != null && playerPoints > 0 && playerXpLevel >= xpCost) {
                /*
                double increment = AttributeUtils.getIncrement(attr.getDescriptionId());
                double newValue = instance.getBaseValue() + increment;

                applyUpgrade(player, attr);
                setPoints(player, playerPoints - 1);

                //Increment attribute value to player
                instance.setBaseValue(newValue);

                int newPoints = getPoints(player);
                PacketHandler.sendToClient(new UpdatePointsPacket(newPoints), player);


                int count = PlayerAttributePersistence.getUpgradeCount(player);
                PacketHandler.sendToClient(new UpdateUpgradeCountPacket(count), player);
                //player.giveExperienceLevels(-xpCost); // ✅ remove níveis
                consumeExperience(player,xpCost);*/
                // Ao aplicar o upgrade
                applyUpgrade(player, attr); // Isso já chama applyModifier agora

                setPoints(player, playerPoints - 1);

                int newPoints = getPoints(player);
                PacketHandler.sendToClient(new UpdatePointsPacket(newPoints), player);

                int count = PlayerAttributePersistence.getUpgradeCount(player);
                PacketHandler.sendToClient(new UpdateUpgradeCountPacket(count), player);

                consumeExperience(player, xpCost);
            } else {
                System.err.println("AttributeInstance is null for: " + id);
            }
        } else {
            System.err.println("Unknown attribute ID: " + id);
        }

    }

    public static boolean setAttribute(ServerPlayer player, String attributeId, double value){

        ResourceLocation id = new ResourceLocation(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);

        if (attr != null) {

            AttributeInstance instance = player.getAttribute(attr);

            //applyUpgrade(player, attr);

            //Increment attribute value to player
            instance.setBaseValue(value);
            return true;
        } else {
            System.err.println("AttributeInstance is null for: " + id);
            return false;
        }


    }

    public static void applyUpgrade(Player player, Attribute attr) {
        ResourceLocation key = BuiltInRegistries.ATTRIBUTE.getKey(attr);
        CompoundTag upgrades = player.getPersistentData().getCompound(ATTRIBUTE_UPGRADES_TAG);

        int currentUpgrades = upgrades.getInt(key.toString()) +1;
        upgrades.putInt(key.toString(), currentUpgrades);
        player.getPersistentData().put(ATTRIBUTE_UPGRADES_TAG, upgrades);

        AttributeInstance instance = player.getAttribute(attr);
        if (instance != null) {
            double increment = AttributeUtils.getIncrement(attr.getDescriptionId());
            //currentUpgrades = getUpgrades(player, key.toString()) + 1;
            applyModifier(instance, attr.getDescriptionId(), increment * currentUpgrades);
        }

        decrementPoints(player);
        incrementUpgradeCount(player);

    }

    private static int getUpgrades(Player player, String attrKey) {
        CompoundTag upgrades = player.getPersistentData().getCompound(ATTRIBUTE_UPGRADES_TAG);
        return upgrades.getInt(attrKey);
    }

    public static void resetAttributes(ServerPlayer player, boolean resetByDeath) {
        if (player.experienceLevel < 50 && player.gameMode.getGameModeForPlayer() != GameType.CREATIVE && !resetByDeath) {
            player.sendSystemMessage(Component.translatable("gui.playerstats.cant_reset"));
            return;
        }

        CompoundTag upgrades = player.getPersistentData().getCompound(ATTRIBUTE_UPGRADES_TAG);
        int refundedPoints = 0;

        for (String key : upgrades.getAllKeys()) {
            ResourceLocation id = new ResourceLocation(key);
            Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
            AttributeInstance instance = player.getAttribute(attr);
            if (attr != null && instance != null) {
                int upgradesApplied = upgrades.getInt(key);
                double increment = AttributeUtils.getIncrement(attr.getDescriptionId());
                //instance.setBaseValue(instance.getBaseValue() - (upgradesApplied * increment));
                // Remove o modificador
                instance.getModifiers().stream()
                        .filter(mod -> mod.getName().equals("playerstats:" + attr.getDescriptionId()))
                        .toList()
                        .forEach(instance::removeModifier);
                refundedPoints += upgradesApplied;
            }
        }


        player.getPersistentData().remove(ATTRIBUTE_UPGRADES_TAG);
        player.getPersistentData().remove(UPGRADE_COUNT_TAG);

        if(!resetByDeath) {

            setPoints(player, getPoints(player) + refundedPoints);
            consumeExperience(player, 50);
            player.sendSystemMessage(Component.translatable("gui.playerstats.reset", refundedPoints));
        }

        PacketHandler.sendToClient(new UpdatePointsPacket(getPoints(player)), player);
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


    private static void applyModifier(AttributeInstance instance, String attrId, double value) {
        // Remover qualquer modificador antigo com o mesmo nome
        instance.getModifiers().stream()
                .filter(mod -> mod.getName().equals("playerstats:" + attrId))
                .toList()
                .forEach(instance::removeModifier);

        AttributeModifier modifier = new AttributeModifier(
                UUID.randomUUID(),
                "playerstats:" + attrId,
                value,
                AttributeModifier.Operation.ADDITION
        );

        instance.addPermanentModifier(modifier);
    }




    public static int getPoints(Player player) {
        return player.getPersistentData().getInt(POINTS_TAG);
    }

    public static void setPoints(Player player, int points) {
        player.getPersistentData().putInt(POINTS_TAG, points);
    }

    public static void addPoints(Player player, int points) {
        CompoundTag tag = player.getPersistentData();
        int playerPoints = tag.getInt(POINTS_TAG);
        tag.putInt(POINTS_TAG, playerPoints + points);
    }

    public static void decrementPoints(Player player) {
        CompoundTag tag = player.getPersistentData();
        int current = tag.getInt(POINTS_TAG);
        tag.putInt(POINTS_TAG, Math.max(0, current - 1));
    }

    public static int getUpgradeCount(Player player) {
        return player.getPersistentData().getInt(UPGRADE_COUNT_TAG);
    }

    public static void incrementUpgradeCount(Player player) {
        CompoundTag tag = player.getPersistentData();
        int count = tag.getInt(UPGRADE_COUNT_TAG);
        tag.putInt(UPGRADE_COUNT_TAG, count + 1);
    }
}
