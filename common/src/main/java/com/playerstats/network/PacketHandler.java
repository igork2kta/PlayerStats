package com.playerstats.network;

import com.playerstats.client.ClientBoostCache;
import com.playerstats.event.PlayerAttributePersistence;
import com.playerstats.platform.Services;
import com.playerstats.util.AttributeUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.Map;

public class PacketHandler {

    public static void sendToClient(CustomPacketPayload packet, ServerPlayer player) {
        Services.PLATFORM.sendToPlayer(player, packet);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        Services.PLATFORM.sendToServer(packet);
    }

    // Handlers Server-Side
    public static void handleModifyAttribute(ModifyAttributePacket msg, ServerPlayer player) {
        var level = player.level();
        var entity = level.getEntity(msg.entityId());

        if (entity instanceof LivingEntity living) {
            var attribute = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.tryParse(msg.attributeId()));
            if (attribute != null) {
                var instance = AttributeUtils.getAttributeInstance(living, attribute);
                if (instance != null) {
                    if (msg.isToggle()) {
                        if (msg.attributeId().startsWith("playerstats:")) {
                            PlayerAttributePersistence.toggleAbility(living, player, msg.attributeId());
                        } else {
                            PlayerAttributePersistence.toggleAttributeUpgrade(living, player, msg.attributeId());
                        }
                    } else {
                        PlayerAttributePersistence.upgradeAttribute(living, player, msg.attributeId());
                    }
                }
            }
        }
    }

    public static void handleRequestEntityUpgrades(RequestEntityUpgradesPacket msg, ServerPlayer player) {
        var entity = player.level().getEntity(msg.entityId());
        if (entity instanceof LivingEntity living) {
            PlayerAttributePersistence.syncEntityUpgrades(living, player);
        }
    }

    public static void handleResetAttributes(ResetAttributesPacket msg, ServerPlayer player) {
        var level = player.level();
        var entity = level.getEntity(msg.entityId());
        if (entity instanceof LivingEntity living) {
            PlayerAttributePersistence.resetAttributes(living, player, false);
        }
    }

    public static void handleActivateInfiniteIce(ActivateInfiniteIcePacket msg, ServerPlayer player) {
        com.playerstats.event.InfiniteIceHandler.activateAbility(player);
    }

    // Handlers Client-Side
    public static void handleUpdatePoints(UpdatePointsPacket msg) {
        if ("attribute".equals(msg.pointsType())) {
            com.playerstats.client.ClientAttributeCache.setPoints(msg.points());
        } else if ("ability".equals(msg.pointsType())) {
            com.playerstats.client.ClientAttributeCache.setAbilityPoints(msg.points());
        }
    }

    public static void handleUpdateUpgradeCount(UpdateUpgradeCountPacket packet) {
        com.playerstats.client.ClientAttributeCache.setUpgradeCount(packet.upgradeCount());
    }

    public static void handleSyncAbilityLevel(SyncAbilityLevelPacket packet) {
        com.playerstats.client.ClientAttributeCache.setAbilityLevel(packet.attributeId(), packet.level());
    }

    public static void handleSyncEntityUpgrades(SyncEntityUpgradesPacket msg) {
        com.playerstats.client.ClientAttributeCache.setEntityUpgrades(
                msg.entityId(),
                msg.upgrades(),
                msg.disabledAttributes()
        );
    }

    public static void handleBoostsSync(BoostsSyncPacket pkt) {
        ClientBoostCache.activeBoosts.clear();
        for (Map.Entry<ResourceLocation, BoostsSyncPacket.BoostData> entry : pkt.boosts.entrySet()) {
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(entry.getKey());
            if (attribute != null) {
                BoostsSyncPacket.BoostData bd = entry.getValue();
                ClientBoostCache.activeBoosts.put(attribute,
                        new ClientBoostCache.BoostInfo(bd.amount, bd.secondsRemaining));
            }
        }
    }
}
