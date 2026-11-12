package com.playerstats.util;

import com.playerstats.ModAttributes;
import com.playerstats.event.PlayerAttributePersistence;
import com.playerstats.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public class UniqueAbilitiesUtils {

    public static boolean validateAbility(LivingEntity entity, Player player, String attributeId, boolean state) {
        CompoundTag persistentData = Services.PLATFORM.getPersistentData(entity);

        // Se ainda não tiver dono, seta o player atual como dono
        if (attributeId.contains("follow_owner") || attributeId.contains("defend_owner") || attributeId.contains("taunt") || attributeId.contains("howl_buff")) {
            if (!persistentData.contains("Owner"))
                persistentData.putUUID("Owner", player.getUUID());

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

            persistentData.putInt("PatrolX", entity.blockPosition().getX());
            persistentData.putInt("PatrolY", entity.blockPosition().getY());
            persistentData.putInt("PatrolZ", entity.blockPosition().getZ());
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

        if (AttributeUtils.getAttributeValue(entity, ability) == 1) {
            PlayerAttributePersistence.applyModifier(instance, ability.value().getDescriptionId(), 1);
        }
    }
}
