package com.playerstats.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UniqueAbilitiesUtils {

    private static final Map<String, MutableComponent> abilities = Map.of(
          "life_regen",  Component.translatable("abilitiess.playerstats.life_regen"),
          "follow_owner", Component.translatable("abilitiess.playerstats.follow_owner"),
          "teleport_to_owner", Component.translatable("abilitiess.playerstats.teleport_to_owner")
    );


    public static Map<String, MutableComponent> getUniqueAbilities(LivingEntity entity, String searchText) {
        Map<String, MutableComponent> entityAbilities = abilities;

        if (entity instanceof Player) {
            entityAbilities.remove("life_regen");
        }
        else{
        }

        if(!(entity instanceof AbstractHorse)) {
            entityAbilities.remove("follow_owner");
            entityAbilities.remove("teleport_to_owner");
        }


        return entityAbilities.entrySet()
                .stream()
                .filter(entry -> {
                    if (!searchText.isEmpty()) {
                        return entry.getKey().toLowerCase().contains(searchText.toLowerCase())
                                || entry.getValue().getString().toLowerCase().contains(searchText.toLowerCase());
                    }
                    return true;
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    public static String gettag(String ability) {
        return abilities.get(ability).toString();

    }


    public static boolean enableDisableAbility(LivingEntity entity, Player player, String attributeId, boolean state){

        if(attributeId.contains("follow_owner")){
            if(state) entity.getPersistentData().putBoolean("follow_owner", true);
            else entity.getPersistentData().putBoolean("follow_owner", false);


        }
        else if(attributeId.contains("teleport_to_owner")) {
            if(entity.getPersistentData().getBoolean("follow_owner")){
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
