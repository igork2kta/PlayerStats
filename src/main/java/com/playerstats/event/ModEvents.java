package com.playerstats.event;

import com.playerstats.ModAttributes;
import com.playerstats.client.KeyBindings;
import com.playerstats.entities.goals.HorseFollowOwnerGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;


public class ModEvents {

    public static void register() {
        EVENT_BUS.register(KeyBindings.class);
    }

    @SubscribeEvent
    public static void onHorseTamed(AnimalTameEvent event){
        if (event.getEntity() instanceof AbstractHorse horse ) {
            horse.getPersistentData().putUUID("Owner", event.getTamer().getUUID());
        }
    }


    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof AbstractHorse horse) {
            horse.goalSelector.addGoal(5, new HorseFollowOwnerGoal(horse));
        }
    }


}
