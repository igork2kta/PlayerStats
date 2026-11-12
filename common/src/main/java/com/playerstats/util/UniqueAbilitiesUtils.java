package com.playerstats.util;

import com.playerstats.ModAttributes;
import com.playerstats.event.PlayerAttributePersistence;
import com.playerstats.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public class UniqueAbilitiesUtils {

    public static boolean validateAbility(LivingEntity entity, Player player, String attributeId, boolean state) {
        if (attributeId.contains("follow_owner") || attributeId.contains("defend_owner") || attributeId.contains("taunt")) {
            if (!Services.PLATFORM.getPersistentData(entity).contains("Owner")) {
                Services.PLATFORM.getPersistentData(entity).putUUID("Owner", player.getUUID());
            }

            if (attributeId.contains("follow_owner") && state) {
                disableAbility(ModAttributes.PATROL, entity);
            }
        } else if (attributeId.contains("teleport_to_owner")) {
            if (AttributeUtils.getAttributeValue(entity, ModAttributes.FOLLOW_OWNER.get()) == -1) {
                player.sendSystemMessage(Component.translatable("gui.playerstats.teleport_locked"));
                return false;
            }
            if (state) {
                disableAbility(ModAttributes.PATROL, entity);
            }
        } else if (attributeId.contains("life_regen")) {
            if (state) {
                entity.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION,
                        Integer.MAX_VALUE,
                        0,
                        true,
                        false
                ));
            } else {
                entity.removeEffect(MobEffects.REGENERATION);
            }
        } else if (attributeId.contains("patrol") && state) {
            disableAbility(ModAttributes.FOLLOW_OWNER, entity);
            disableAbility(ModAttributes.TELEPORT_TO_OWNER, entity);

            Services.PLATFORM.getPersistentData(entity).putInt("PatrolX", entity.blockPosition().getX());
            Services.PLATFORM.getPersistentData(entity).putInt("PatrolY", entity.blockPosition().getY());
            Services.PLATFORM.getPersistentData(entity).putInt("PatrolZ", entity.blockPosition().getZ());
        } else {
            return !attributeId.contains("rebirth") || state;
        }

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

    private static void disableAbility(Supplier<Attribute> ability, LivingEntity entity) {
        AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, ability.get());
        if (instance == null) return;

        if (AttributeUtils.getAttributeValue(entity, ability.get()) == 1) {
            PlayerAttributePersistence.applyModifier(instance, ability.get().getDescriptionId(), 1);
        }
    }
}
