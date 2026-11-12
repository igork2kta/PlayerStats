package com.playerstats.network;

import com.playerstats.PlayerStats;
import com.playerstats.client.ClientAttributeCache;
import com.playerstats.client.ClientBoostCache;
import com.playerstats.event.InfiniteIceHandler;
import com.playerstats.event.PlayerAttributePersistence;
import com.playerstats.platform.Services;
import com.playerstats.util.AttributeUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.Map;

public class PacketHandler {

    public static final ResourceLocation UPDATE_POINTS = new ResourceLocation(PlayerStats.MODID, "update_points");
    public static final ResourceLocation RESET_ATTRIBUTES = new ResourceLocation(PlayerStats.MODID, "reset_attributes");
    public static final ResourceLocation UPDATE_UPGRADE_COUNT = new ResourceLocation(PlayerStats.MODID, "update_upgrade_count");
    public static final ResourceLocation BOOSTS_SYNC = new ResourceLocation(PlayerStats.MODID, "boosts_sync");
    public static final ResourceLocation MODIFY_ATTRIBUTE = new ResourceLocation(PlayerStats.MODID, "modify_attribute");
    public static final ResourceLocation ACTIVATE_INFINITE_ICE = new ResourceLocation(PlayerStats.MODID, "activate_infinite_ice");
    public static final ResourceLocation SYNC_ABILITY_LEVEL = new ResourceLocation(PlayerStats.MODID, "sync_ability_level");
    public static final ResourceLocation REQUEST_ENTITY_UPGRADES = new ResourceLocation(PlayerStats.MODID, "request_entity_upgrades");
    public static final ResourceLocation SYNC_ENTITY_UPGRADES = new ResourceLocation(PlayerStats.MODID, "sync_entity_upgrades");

    // Client -> Server handlers
    public static void handleActivateInfiniteIce(ActivateInfiniteIcePacket msg, ServerPlayer player) {
        if (player != null) {
            InfiniteIceHandler.activateAbility(player);
        }
    }

    public static void handleModifyAttribute(ModifyAttributePacket msg, ServerPlayer player) {
        if (player != null) {
            var level = player.level();
            var entity = level.getEntity(msg.getEntityId());
            if (entity instanceof LivingEntity living) {
                var attribute = BuiltInRegistries.ATTRIBUTE.get(new ResourceLocation(msg.getAttributeId()));
                if (attribute != null) {
                    var instance = AttributeUtils.getAttributeInstance(living, attribute);
                    if (instance != null) {
                        if (msg.isToggle()) {
                            if (msg.getAttributeId().startsWith("playerstats:")) {
                                PlayerAttributePersistence.toggleAbility(living, player, msg.getAttributeId());
                            } else {
                                PlayerAttributePersistence.toggleAttributeUpgrade(living, player, msg.getAttributeId());
                            }
                        } else {
                            PlayerAttributePersistence.upgradeAttribute(living, player, msg.getAttributeId());
                        }
                    }
                }
            }
        }
    }

    public static void handleRequestEntityUpgrades(RequestEntityUpgradesPacket msg, ServerPlayer player) {
        if (player != null) {
            var entity = player.level().getEntity(msg.getEntityId());
            if (entity instanceof LivingEntity living) {
                PlayerAttributePersistence.syncEntityUpgrades(living, player);
            }
        }
    }

    public static void handleResetAttributes(ResetAttributesPacket msg, ServerPlayer player) {
        if (player != null) {
            var level = player.level();
            var entity = level.getEntity(msg.getEntityId());
            if (entity instanceof LivingEntity living) {
                PlayerAttributePersistence.resetAttributes(living, player, false);
            }
        }
    }

    // Server -> Client handlers
    public static void handleUpdatePoints(UpdatePointsPacket msg) {
        if ("attribute".equals(msg.getPointsType())) {
            ClientAttributeCache.setPoints(msg.getPoints());
        } else if ("ability".equals(msg.getPointsType())) {
            ClientAttributeCache.setAbilityPoints(msg.getPoints());
        }
    }

    public static void handleUpdateUpgradeCount(UpdateUpgradeCountPacket msg) {
        ClientAttributeCache.setUpgradeCount(msg.getUpgradeCount());
    }

    public static void handleSyncAbilityLevel(SyncAbilityLevelPacket msg) {
        ClientAttributeCache.setAbilityLevel(msg.getAttributeId(), msg.getLevel());
    }

    public static void handleSyncEntityUpgrades(SyncEntityUpgradesPacket msg) {
        ClientAttributeCache.setEntityUpgrades(msg.getEntityId(), msg.getUpgrades(), msg.getDisabledAttributes());
    }

    public static void handleBoostsSync(BoostsSyncPacket msg) {
        ClientBoostCache.activeBoosts.clear();
        for (Map.Entry<ResourceLocation, BoostsSyncPacket.BoostData> entry : msg.getBoosts().entrySet()) {
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(entry.getKey());
            if (attribute != null) {
                BoostsSyncPacket.BoostData bd = entry.getValue();
                ClientBoostCache.activeBoosts.put(attribute,
                        new ClientBoostCache.BoostInfo(bd.amount, bd.secondsRemaining));
            }
        }
    }

    public static void sendToClient(ServerPlayer player, Object packet) {
        Services.PLATFORM.sendToPlayer(player, packet);
    }

    public static void sendToServer(Object packet) {
        Services.PLATFORM.sendToServer(packet);
    }
}
