package com.playerstats.util;

import com.playerstats.ModAttributes;
import com.playerstats.event.PlayerAttributePersistence;
import com.playerstats.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;

public class UniqueAbilitiesUtils {

    public static boolean validateAbility(LivingEntity entity, Player player, String attributeId, boolean state) {

        // Se ainda não tiver dono, seta o player atual como dono
        if (attributeId.contains("follow_owner") || attributeId.contains("defend_owner") || attributeId.contains("taunt")) {
            if (!Services.PLATFORM.getPersistentData(entity).contains("Owner")) {
                if (entity instanceof OwnableEntity ownable && ownable.getOwnerReference() != null && ownable.getOwnerReference().getUUID() != null) {
                    Services.PLATFORM.getPersistentData(entity).store("Owner", UUIDUtil.CODEC, ownable.getOwnerReference().getUUID());
                } else {
                    Services.PLATFORM.getPersistentData(entity).store("Owner", UUIDUtil.CODEC, player.getUUID());
                    if (entity instanceof AbstractHorse horse) {
                        horse.setOwner(player);
                        horse.setTamed(true);
                    }
                }
            }

            if (attributeId.contains("follow_owner") && state) disableAbility(ModAttributes.PATROL, entity);
        } else if (attributeId.contains("teleport_to_owner")) {
            // Não permite ativar se não tiver follow owner
            if (AttributeUtils.getAttributeValue(entity, ModAttributes.FOLLOW_OWNER) == -1) {
                player.sendSystemMessage(Component.translatable("gui.playerstats.teleport_locked"));
                return false;
            }
            if (state) disableAbility(ModAttributes.PATROL, entity);
        } else if (attributeId.contains("life_regen")) {
            if (state)
                entity.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION,
                        Integer.MAX_VALUE,
                        0,
                        true,
                        false
                ));
            else
                entity.removeEffect(MobEffects.REGENERATION);

        } else if (attributeId.contains("patrol") && state) {
            disableAbility(ModAttributes.FOLLOW_OWNER, entity);
            disableAbility(ModAttributes.TELEPORT_TO_OWNER, entity);

            Services.PLATFORM.getPersistentData(entity).putInt("PatrolX", entity.blockPosition().getX());
            Services.PLATFORM.getPersistentData(entity).putInt("PatrolY", entity.blockPosition().getY());
            Services.PLATFORM.getPersistentData(entity).putInt("PatrolZ", entity.blockPosition().getZ());

        } else return !attributeId.contains("rebirth") || state;

        return true;
    }

    public static boolean isLeveledAbility(String attributeId) {
        return attributeId.contains("vampirism")
                || attributeId.contains("milky_aura")
                || attributeId.contains("howl_buff")
                || attributeId.contains("scout_vision");
    }

    public static int getMaxLevel(String attributeId) {
        if (attributeId.contains("vampirism")) {
            return 5;
        }
        if (attributeId.contains("milky_aura") || attributeId.contains("howl_buff") || attributeId.contains("scout_vision")) {
            return 3;
        }
        return 1;
    }

    private static void disableAbility(Holder<Attribute> ability, LivingEntity entity) {
        AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, ability.value());
        if (instance == null) return;

        if (AttributeUtils.getAttributeValue(entity, ability) >= 1.0D) {
            PlayerAttributePersistence.applyModifier(instance, ability.value().getDescriptionId(), 1.0D);
            Identifier id = BuiltInRegistries.ATTRIBUTE.getKey(ability.value());
            if (id != null && entity instanceof net.minecraft.server.level.ServerPlayer sp) {
                com.playerstats.network.PacketHandler.sendToClient(
                        new com.playerstats.network.SyncAbilityLevelPacket(entity.getId(), id.toString(), 0),
                        sp
                );
            }
        }
    }
}
