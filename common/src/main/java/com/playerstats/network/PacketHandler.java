package com.playerstats.network;

import com.playerstats.client.ClientBoostCache;
import com.playerstats.event.PlayerAttributePersistence;
import com.playerstats.platform.Services;
import com.playerstats.util.AttributeUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
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
            var attribute = BuiltInRegistries.ATTRIBUTE.getValue(Identifier.tryParse(msg.attributeId()));
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
            if (AttributeUtils.getAttributeValue(living, com.playerstats.ModAttributes.FOLLOW_OWNER) >= 1.0D
                    || AttributeUtils.getAttributeValue(living, com.playerstats.ModAttributes.DEFEND_OWNER) >= 1.0D
                    || AttributeUtils.getAttributeValue(living, com.playerstats.ModAttributes.TAUNT) >= 1.0D) {
                if (!Services.PLATFORM.getPersistentData(living).contains("Owner")) {
                    if (living instanceof net.minecraft.world.entity.OwnableEntity ownable && ownable.getOwnerReference() != null && ownable.getOwnerReference().getUUID() != null) {
                        Services.PLATFORM.getPersistentData(living).store("Owner", net.minecraft.core.UUIDUtil.CODEC, ownable.getOwnerReference().getUUID());
                    } else {
                        Services.PLATFORM.getPersistentData(living).store("Owner", net.minecraft.core.UUIDUtil.CODEC, player.getUUID());
                        if (living instanceof net.minecraft.world.entity.animal.equine.AbstractHorse horse) {
                            horse.setOwner(player);
                            horse.setTamed(true);
                        }
                    }
                }
            }
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
        com.playerstats.client.ClientAttributeCache.setUpgradeCount(packet.entityId(), packet.upgradeCount());
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.screen instanceof com.playerstats.client.StatsScreen screen) {
            if (screen.getEntity() != null && screen.getEntity().getId() == packet.entityId()) {
                screen.onEntityUpgradesUpdated();
            }
        }
    }

    public static void handleSyncAbilityLevel(SyncAbilityLevelPacket packet) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        int entityId = packet.entityId();
        if (entityId == -1 && mc.player != null) {
            entityId = mc.player.getId();
        }

        if (entityId != -1) {
            com.playerstats.client.ClientAttributeCache.setAbilityLevel(entityId, packet.attributeId(), packet.level());
        } else {
            com.playerstats.client.ClientAttributeCache.setAbilityLevel(packet.attributeId(), packet.level());
        }

        LivingEntity living = null;
        if (mc.player != null && (entityId == -1 || mc.player.getId() == entityId)) {
            living = mc.player;
        } else if (mc.level != null && entityId != -1) {
            var ent = mc.level.getEntity(entityId);
            if (ent instanceof LivingEntity l) {
                living = l;
            }
        }

        if (living != null) {
            Attribute attr = BuiltInRegistries.ATTRIBUTE.getValue(Identifier.tryParse(packet.attributeId()));
            if (attr != null) {
                var inst = AttributeUtils.getAttributeInstance(living, attr);
                if (inst != null) {
                    if (inst.getBaseValue() != -1.0D) {
                        inst.setBaseValue(-1.0D);
                    }
                    if (packet.level() > 0) {
                        if (com.playerstats.util.UniqueAbilitiesUtils.isLeveledAbility(packet.attributeId())) {
                            PlayerAttributePersistence.applyModifier(inst, attr.getDescriptionId(), 1.0D + packet.level());
                        } else {
                            PlayerAttributePersistence.applyModifier(inst, attr.getDescriptionId(), 2.0D);
                        }
                    } else if (packet.level() == 0) {
                        PlayerAttributePersistence.applyModifier(inst, attr.getDescriptionId(), 1.0D);
                    } else {
                        PlayerAttributePersistence.removePlayerStatsModifier(inst, attr.getDescriptionId());
                    }
                }
            }
        }

        if (mc.screen instanceof com.playerstats.client.StatsScreen screen) {
            if (screen.getEntity() != null && (screen.getEntity().getId() == entityId || packet.entityId() == -1)) {
                screen.onEntityUpgradesUpdated();
            }
        }
    }

    public static void handleSyncEntityUpgrades(SyncEntityUpgradesPacket msg) {
        com.playerstats.client.ClientAttributeCache.setEntityUpgrades(
                msg.entityId(),
                msg.upgradeCount(),
                msg.upgrades(),
                msg.disabledAttributes()
        );

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level != null) {
            var ent = mc.level.getEntity(msg.entityId());
            if (ent instanceof LivingEntity living) {
                for (var attr : BuiltInRegistries.ATTRIBUTE) {
                    Identifier key = BuiltInRegistries.ATTRIBUTE.getKey(attr);
                    if (key != null && !key.getNamespace().equals("playerstats") && !msg.upgrades().containsKey(key.toString())) {
                        var inst = AttributeUtils.getAttributeInstance(living, attr);
                        if (inst != null) {
                            PlayerAttributePersistence.removePlayerStatsModifier(inst, attr.getDescriptionId());
                        }
                    }
                }
            }
        }

        if (mc.screen instanceof com.playerstats.client.StatsScreen screen) {
            if (screen.getEntity() != null && screen.getEntity().getId() == msg.entityId()) {
                screen.onEntityUpgradesUpdated();
            }
        }
    }

    public static void handleBoostsSync(BoostsSyncPacket pkt) {
        ClientBoostCache.activeBoosts.clear();
        for (Map.Entry<Identifier, BoostsSyncPacket.BoostData> entry : pkt.boosts.entrySet()) {
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.getValue(entry.getKey());
            if (attribute != null) {
                BoostsSyncPacket.BoostData bd = entry.getValue();
                ClientBoostCache.activeBoosts.put(attribute,
                        new ClientBoostCache.BoostInfo(bd.amount, bd.secondsRemaining));
            }
        }
    }
}
