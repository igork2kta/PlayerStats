package com.playerstats.util;

import com.playerstats.ModAttributes;
import com.playerstats.event.PlayerAttributePersistence;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.DeferredHolder;


public class UniqueAbilitiesUtils {

    public static boolean validateAbility(LivingEntity entity, Player player, String attributeId, boolean state){

        //Se ainda não tiver dono, seta o player atual como dono
        if(attributeId.contains("follow_owner") || attributeId.contains("defend_owner") || attributeId.contains("taunt")){
            if(!entity.getPersistentData().contains("Owner"))
                entity.getPersistentData().putUUID("Owner", player.getUUID());

            if(attributeId.contains("follow_owner")) disableAbility(ModAttributes.PATROL, entity);
        }

        else if(attributeId.contains("teleport_to_owner")) {
            //Não permite ativar se não tiver follow owner
            if(AttributeUtils.getAttributeValue(entity, ModAttributes.FOLLOW_OWNER) == -1){
                player.sendSystemMessage(Component.translatable("gui.playerstats.teleport_locked"));
                return false;
            }
            if(state) disableAbility(ModAttributes.PATROL, entity);
        }
        else if(attributeId.contains("life_regen")) {
                if(state)
                    entity.addEffect(new MobEffectInstance(
                            MobEffects.REGENERATION,
                            Integer.MAX_VALUE, // duração absurda
                            0,                 // nível do efeito (0 = Regeneration I)
                            true,              // ambient (sem partículas chamativas)
                            false              // showParticles (false = invisível)
                    ));
                else
                    entity.removeEffect(MobEffects.REGENERATION);

        }

        else if(attributeId.contains("patrol") && state){
            disableAbility(ModAttributes.FOLLOW_OWNER, entity);
            disableAbility(ModAttributes.TELEPORT_TO_OWNER, entity);

            entity.getPersistentData().putDouble("PatrolX", entity.getX());
            entity.getPersistentData().putDouble("PatrolY", entity.getY());
            entity.getPersistentData().putDouble("PatrolZ", entity.getZ());

        }

        //Não permite desligar o renascimento
        else return !attributeId.contains("rebirth") || state;

        return true;
    }

    private static void disableAbility(DeferredHolder<Attribute, Attribute> ability, LivingEntity entity){

        AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, ability.get());
        if(instance == null) return;

        if(AttributeUtils.getAttributeValue(entity, ability) == 1){
            PlayerAttributePersistence.applyModifier(instance, ability.get().getDescriptionId(), 1);
        }
    }
}
