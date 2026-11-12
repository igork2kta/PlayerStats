package com.playerstats.event;

import com.playerstats.ModAttributes;
import com.playerstats.entities.goals.DefendOwnerTargetGoal;
import com.playerstats.entities.goals.FollowOwnerGoal;
import com.playerstats.entities.goals.PatrolGoal;
import com.playerstats.items.ModItems;
import com.playerstats.sounds.ModSounds;
import com.playerstats.util.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.*;

@EventBusSubscriber(modid = "playerstats")
public class ModAbilityEvents {

    private static final Map<AbstractHorse, BlockPos> lastHorsePositions = new WeakHashMap<>();

    @SubscribeEvent
    public static void onHorseTamed(AnimalTameEvent event){
        if (event.getEntity() instanceof AbstractHorse horse ) {
            horse.getPersistentData().store("Owner", UUIDUtil.CODEC, event.getTamer().getUUID());
        }
    }

    //FOLLOW OWNER / TELEPORT TO OWNER
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof AbstractHorse horse) {
            horse.goalSelector.addGoal(5, new FollowOwnerGoal(horse));
        }
        if (event.getEntity() instanceof IronGolem golem) {
            golem.targetSelector.addGoal(1, new DefendOwnerTargetGoal(golem)); //maior prioridade
            golem.goalSelector.addGoal(4, new FollowOwnerGoal(golem));
            golem.goalSelector.addGoal(5, new PatrolGoal(golem, 1.0D, 16.0D));
        }
        if (event.getEntity() instanceof Wolf wolf) {
            wolf.goalSelector.addGoal(5, new PatrolGoal(wolf, 1.0D, 16.0D));
        }
    }

    //FROST WALKER
    @SubscribeEvent
    public static void onHorseTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractHorse horse)) return;

        Level level = horse.level();
        if (level.isClientSide() || !horse.onGround()) return;

        AttributeInstance attr = horse.getAttribute(ModAttributes.FROST_WALKER.getDelegate());
        if (attr == null || attr.getValue() != 1.0D) return;

        BlockPos currentPos = horse.blockPosition();
        BlockPos lastPos = lastHorsePositions.get(horse);

        boolean hasMoved = !currentPos.equals(lastPos);
        boolean shouldRefreshStill = (level.getGameTime() % 20 == 0);

        if (hasMoved || shouldRefreshStill) {
            lastHorsePositions.put(horse, currentPos);
            applyFrostWalker(horse, level, currentPos, 1);
        }
    }

    //REBIRTH (droppar fragmento)
    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();

        AttributeInstance revivable = entity.getAttribute(ModAttributes.REBIRTH.getDelegate());
        if (revivable != null && revivable.getValue() == 1.0D) {

            ItemStack soulFragment = new ItemStack(ModItems.SOUL_FRAGMENT.get());
            if (entity instanceof ServerPlayer player) {
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
                player.saveWithoutId(output);
                CompoundTag soulData = output.buildResult(); // salva inventÃ¡rio, XP etc.
                soulData.putString("id", "minecraft:player");
                soulData.putString("UUID", player.getUUID().toString()); // Salva explicitamente UUID
                soulFragment.set(ModDataComponents.STORED_ENTITY, soulData);
                soulData.putString("StoredEntityName", player.getDisplayName().getString());
            }
            else {
                // mobs normais
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
                entity.save(output);
                CompoundTag soulData = output.buildResult();
                soulData.putString("StoredEntityName", entity.getDisplayName().getString());
                soulFragment.set(ModDataComponents.STORED_ENTITY, soulData);
            }


            ItemEntity drop = new ItemEntity(entity.level(),
                    entity.getX(), entity.getY(), entity.getZ(),
                    soulFragment);

            event.getDrops().add(drop);
        }
    }

    //HOWL_BUFF
    private static final Map<UUID, Long> howlCooldowns = new HashMap<>();
    private static final long HOWL_COOLDOWN_TICKS = 20 * 60 * 5; // 5 minutos

    @SubscribeEvent
    public static void onWolfTargetChange(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf)) return;
        if(event.getNewAboutToBeSetTarget() == null) return;
        if (wolf.getOwner() == null) return;

        // Checa se o atributo HOWL_BUFF estÃ¡ ativo (1 a 3)
        var attrInstance = wolf.getAttribute(ModAttributes.HOWL_BUFF.getDelegate());
        if (attrInstance == null || attrInstance.getValue() <= 0.0D) return;

        int level = (int) Math.round(attrInstance.getValue());

        // Checa cooldown
        long lastHowl = howlCooldowns.getOrDefault(wolf.getUUID(), 0L);
        if (wolf.level().getGameTime() - lastHowl < HOWL_COOLDOWN_TICKS) return;

        // Atualiza cooldown
        howlCooldowns.put(wolf.getUUID(), wolf.level().getGameTime());

        // Raio e duraÃ§Ã£o por nÃ­vel
        double howlRadius = 4.0D + (level * 4.0D); // Nv 1: 8b, Nv 2: 12b, Nv 3: 16b
        int durationTicks = 20 * (15 + level * 15); // Nv 1: 30s (600t), Nv 2: 45s (900t), Nv 3: 60s (1200t)
        int amplifier = level >= 3 ? 1 : 0; // Nv 1 e 2: efeito nÃ­vel 1 (amplifier 0), Nv 3: efeito nÃ­vel 2 (amplifier 1)

        // Buffa aliados prÃ³ximos (outros lobos vivos)
        List<Wolf> allies = wolf.level().getEntitiesOfClass(Wolf.class,
                wolf.getBoundingBox().inflate(howlRadius),
                w -> w.isAlive() && w != wolf);

        for (Wolf ally : allies) {
            ally.addEffect(new MobEffectInstance(MobEffects.STRENGTH, durationTicks, amplifier));
            ally.addEffect(new MobEffectInstance(MobEffects.SPEED, durationTicks, amplifier));
            ally.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, durationTicks, amplifier));
        }

        // Buffa o dono do lobo
        if (wolf.getOwner() instanceof Player owner) {
            owner.addEffect(new MobEffectInstance(MobEffects.STRENGTH, durationTicks, amplifier));
            owner.addEffect(new MobEffectInstance(MobEffects.SPEED, durationTicks, amplifier));
            owner.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, durationTicks, amplifier));
        }

        // Buffa o prÃ³prio lobo
        wolf.addEffect(new MobEffectInstance(MobEffects.STRENGTH, durationTicks, amplifier));
        wolf.addEffect(new MobEffectInstance(MobEffects.SPEED, durationTicks, amplifier));
        wolf.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, durationTicks, amplifier));

        // Efeito visual e sonoro
        if (wolf.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SOUL, wolf.getX(), wolf.getY() + 1, wolf.getZ(),
                    20 + level * 10, 0.5, 0.5, 0.5, 0.1);
        }
        wolf.playSound(ModSounds.WOLF_HOWL.get(), 1.0F, 1.0F);

    }





    public static void applyFrostWalker(AbstractHorse horse, Level level, BlockPos pos, int levelFrost) {
        if (!horse.onGround()) return;

        BlockState frostedIce = Blocks.FROSTED_ICE.defaultBlockState();
        float radius = Math.min(16, 2 + levelFrost);

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset((int) -radius, -1, (int)-radius), pos.offset((int)radius, -1, (int)radius))) {
            if (blockpos.closerToCenterThan(horse.position(), radius)) {
                mutable.set(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                if (level.getBlockState(mutable).isAir()) {
                    BlockState state = level.getBlockState(blockpos);
                    if (state.getBlock() == Blocks.WATER
                            && state.getValue(LiquidBlock.LEVEL) == 0
                            && frostedIce.canSurvive(level, blockpos)
                            && level.isUnobstructed(frostedIce, blockpos, CollisionContext.empty())) {
                        level.setBlockAndUpdate(blockpos, frostedIce);
                        level.scheduleTick(blockpos, Blocks.FROSTED_ICE, Mth.nextInt(horse.getRandom(), 60, 120));
                    }
                }
            }
        }
    }



}
