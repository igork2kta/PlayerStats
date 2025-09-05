package com.playerstats.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;


public class UniqueAbilitiesUtils {

    public static boolean enableDisableAbility(LivingEntity entity, Player player, String attributeId, boolean state){

        if(attributeId.contains("follow_owner")){
            if(state) entity.getPersistentData().putBoolean("follow_owner", true);
            else entity.getPersistentData().putBoolean("follow_owner", false);


        }
        else if(attributeId.contains("teleport_to_owner")) {
            if(entity.getPersistentData().contains("follow_owner")){
                if(state) entity.getPersistentData().putBoolean("teleport_to_owner", true);
                else entity.getPersistentData().putBoolean("teleport_to_owner", false);
            }
            else{
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
