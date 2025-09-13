package com.playerstats.event;

import com.playerstats.ModAttributes;
import com.playerstats.entities.goals.DefendOwnerTargetGoal;
import com.playerstats.entities.goals.FollowOwnerGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "playerstats")
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
    public static void onHorseTick(PlayerTickEvent.Post event) {

        try{
            if (!(event.getEntity().getVehicle() instanceof AbstractHorse horse)) return;
            Level level = horse.level();
            if (level.isClientSide) return;

            //por algum motivo isso da exceção às vezes
            if (horse.getAttributes().getInstance(ModAttributes.FROST_WALKER.getDelegate()).getValue() == 1) {
                BlockPos pos = horse.blockPosition();

                // reaproveitar a lógica vanilla do Frost Walker
                int frostLevel = 1; // pode ser fixo ou dinâmico
                //FrostWalkerEnchantment.onEntityMoved(horse, level, pos, frostLevel);
                applyFrostWalker(horse, horse.level(), horse.blockPosition(), 1);
            }
        }
        catch(Exception ex){}

    }


    public static void applyFrostWalker(Entity entity, Level level, BlockPos pos, int levelFrost) {
        if (!entity.onGround()) return;

        BlockState frostedIce = Blocks.FROSTED_ICE.defaultBlockState();
        float radius = Math.min(16, 2 + levelFrost);

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset((int) -radius, -1, (int)-radius), pos.offset((int)radius, -1, (int)radius))) {
            if (blockpos.closerToCenterThan(entity.position(), radius)) {
                mutable.set(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                if (level.getBlockState(mutable).isAir()) {
                    BlockState state = level.getBlockState(blockpos);
                    if (state.getBlock() == Blocks.WATER
                            && state.getValue(LiquidBlock.LEVEL) == 0
                            && frostedIce.canSurvive(level, blockpos)
                            && level.isUnobstructed(frostedIce, blockpos, CollisionContext.empty())) {
                        level.setBlockAndUpdate(blockpos, frostedIce);
                        level.scheduleTick(blockpos, Blocks.FROSTED_ICE, Mth.nextInt(entity.getRandom(), 60, 120));
                    }
                }
            }
        }
    }



}
