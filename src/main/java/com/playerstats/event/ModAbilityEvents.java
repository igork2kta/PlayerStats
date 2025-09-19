package com.playerstats.event;

import com.playerstats.ModAttributes;
import com.playerstats.entities.goals.DefendOwnerTargetGoal;
import com.playerstats.entities.goals.FollowOwnerGoal;
import com.playerstats.items.ModItems;
import com.playerstats.util.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
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
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = "playerstats")
public class ModAbilityEvents {

    @SubscribeEvent
    public static void onHorseTamed(AnimalTameEvent event){
        if (event.getEntity() instanceof AbstractHorse horse ) {
            horse.getPersistentData().putUUID("Owner", event.getTamer().getUUID());
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
            golem.targetSelector.addGoal(3, new FollowOwnerGoal(golem)); //menor prioridade
        }
    }

    //FROST WALKER
    @SubscribeEvent
    public static void onHorseTick(PlayerTickEvent.Post event) {

        if (!(event.getEntity().getVehicle() instanceof AbstractHorse horse)) return;

        Level level = horse.level();
        if (level.isClientSide) return;

        boolean hasFrostWalker = Optional.ofNullable(horse.getAttribute(ModAttributes.FROST_WALKER.getDelegate()))
            .map(attr -> attr.getValue() == 1)
            .orElse(false);

        if (hasFrostWalker) {

            applyFrostWalker(horse, horse.level(), horse.blockPosition(), 1);
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
                CompoundTag soulData = new CompoundTag();
                player.saveWithoutId(soulData); // salva inventário, XP etc.
                soulData.putString("id", "minecraft:player");
                soulData.putString("UUID", player.getUUID().toString()); // Salva explicitamente UUID
                soulFragment.set(ModDataComponents.STORED_ENTITY, soulData);

            }
            else {
                // mobs normais
                CompoundTag soulData = new CompoundTag();
                entity.save(soulData);
                soulData.putString("StoredEntityName", entity.getDisplayName().getString());
                soulFragment.set(ModDataComponents.STORED_ENTITY, soulData);
            }


            ItemEntity drop = new ItemEntity(entity.level(),
                    entity.getX(), entity.getY(), entity.getZ(),
                    soulFragment);

            event.getDrops().add(drop);
        }
    }


    //TAUNT
    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        LivingEntity attacker = event.getEntity();
        LivingEntity originalTarget = event.getOriginalAboutToBeSetTarget();

        // só vale se for hostil mirando em um player
        if (!(originalTarget instanceof ServerPlayer player)) return;

        Level level = attacker.level();

        // procura Iron Golem próximo com atributo TAUNT ativo
        List<IronGolem> golems = level.getEntitiesOfClass(IronGolem.class,
                attacker.getBoundingBox().inflate(16.0D),
                g -> g.getAttribute(ModAttributes.TAUNT.getDelegate()) != null
                        && g.getAttribute(ModAttributes.TAUNT.getDelegate()).getValue() == 1.0D
                        && g.isAlive());

        if (!golems.isEmpty()) {
            IronGolem golem = golems.get(0); // escolhe o primeiro, ou pode pegar o mais próximo
            event.setNewAboutToBeSetTarget(golem);
            golem.setTarget(attacker);

            // Partículas visuais sobre o Golem
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        golem.getX(), golem.getY() + 1.0, golem.getZ(),
                        10, 0.5, 0.5, 0.5, 0.05);
            }
        }

        // procura lobos hostis/neutros próximos para redirecionar
        List<LivingEntity> wolves = level.getEntitiesOfClass(LivingEntity.class,
                attacker.getBoundingBox().inflate(16.0D),
                w -> w.isAlive() && w.getType() == EntityType.WOLF && w.getAttribute(ModAttributes.TAUNT.getDelegate()) != null
                        && w.getAttribute(ModAttributes.TAUNT.getDelegate()).getValue() == 1.0D);

        if (!wolves.isEmpty()) {
            Wolf wolf = (Wolf) wolves.get(0);

            event.setNewAboutToBeSetTarget(wolf); // redireciona o ataque para o lobo
            wolf.setTarget(attacker);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        wolf.getX(), wolf.getY() + 0.5, wolf.getZ(),
                        10, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    //HOWL_BUFF
    private static final Map<UUID, Long> howlCooldowns = new HashMap<>();
    private static final long HOWL_COOLDOWN_TICKS = 20 * 60 * 5; // 5 minutos

    @SubscribeEvent
    public static void onWolfTargetChange(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf)) return;
        if(event.getNewAboutToBeSetTarget() == null) return;
        // Checa cooldown
        long lastHowl = howlCooldowns.getOrDefault(wolf.getUUID(), 0L);
        if (wolf.level().getGameTime() - lastHowl < HOWL_COOLDOWN_TICKS) return;

        // Checa se o atributo HOWL_BUFF está ativo
        if (wolf.getAttribute(ModAttributes.HOWL_BUFF.getDelegate()).getValue() != 1) return;

        // Atualiza cooldown
        howlCooldowns.put(wolf.getUUID(), wolf.level().getGameTime());

        // Raio do Howl
        double howlRadius = 8.0D;

        // Buffa aliados próximos (outros lobos vivos)
        List<Wolf> allies = wolf.level().getEntitiesOfClass(Wolf.class,
                wolf.getBoundingBox().inflate(howlRadius),
                w -> w.isAlive() && w != wolf);

        for (Wolf ally : allies) {
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1)); // +dano 60s
            ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1)); // +velocidade 60s
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1)); // +velocidade 60s
        }

        Player owner = (Player) wolf.getOwner();
        owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1)); // +dano 60s
        owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1)); // +velocidade 60s
        owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1)); // +velocidade 60s

        wolf.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1)); // +dano 60s
        wolf.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1)); // +velocidade 60s
        wolf.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1)); // +velocidade 60s

        // Efeito visual e sonoro
        if (wolf.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SOUL, wolf.getX(), wolf.getY() + 1, wolf.getZ(),
                    20, 0.5, 0.5, 0.5, 0.1);
        }
        wolf.playSound(SoundEvents.WOLF_HOWL, 1.0F, 1.0F);

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
