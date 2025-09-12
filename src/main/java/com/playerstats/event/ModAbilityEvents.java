package com.playerstats.event;

import com.playerstats.ModAttributes;
import com.playerstats.entities.goals.DefendOwnerTargetGoal;
import com.playerstats.entities.goals.FollowOwnerGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "playerstats")
public class ModAbilityEvents {

    @SubscribeEvent
    public static void onHorseTamed(AnimalTameEvent event){
        if (event.getEntity() instanceof AbstractHorse horse ) {
            horse.getPersistentData().putUUID("Owner", event.getTamer().getUUID());
        }
    }


    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof AbstractHorse horse) {
            horse.goalSelector.addGoal(5, new FollowOwnerGoal(horse));
        }
        if (event.getEntity() instanceof IronGolem golem) {
            golem.targetSelector.addGoal(1, new DefendOwnerTargetGoal(golem)); //maior prioridade
            golem.targetSelector.addGoal(3, new FollowOwnerGoal(golem)); //menor prioridade
        }
    }


    @SubscribeEvent
    public static void onHorseTick(LivingEvent.LivingTickEvent event) {

        try{
            if (!(event.getEntity() instanceof AbstractHorse horse)) return;
            Level level = horse.level();
            if (level.isClientSide) return;

            //por algum motivo isso da exceção às vezes
            if (horse.getAttributes().getInstance(ModAttributes.FROST_WALKER.getHolder().get()).getValue() == 1) {
                BlockPos pos = horse.blockPosition();

                // reaproveitar a lógica vanilla do Frost Walker
                int frostLevel = 1; // pode ser fixo ou dinâmico
                FrostWalkerEnchantment.onEntityMoved(horse, level, pos, frostLevel);
            }
        }
        catch(Exception ex){}

    }

}
