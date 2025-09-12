package com.playerstats.util;

import com.playerstats.ModAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;


public class UniqueAbilitiesUtils {

    public static boolean enableDisableAbility(LivingEntity entity, Player player, String attributeId, boolean state){

        //Se ainda não tiver dono, seta o player atual como dono
        if(attributeId.contains("follow_owner") || attributeId.contains("defend_owner")){
            if(!entity.getPersistentData().contains("Owner"))
                entity.getPersistentData().putUUID("Owner", player.getUUID());
        }
        else if(attributeId.contains("teleport_to_owner")) {
            //Não permite ativar se não tiver follow owner
            if(AttributeUtils.getAttributeValue(entity, ModAttributes.FOLLOW_OWNER) == -1){
                player.sendSystemMessage(Component.translatable("gui.playerstats.teleport_locked"));
                return false;
            }
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
        return true;
    }
}
