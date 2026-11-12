package com.playerstats.event;

import com.playerstats.ModAttributes;
import com.playerstats.entities.goals.DefendOwnerTargetGoal;
import com.playerstats.entities.goals.FollowOwnerGoal;
import com.playerstats.entities.goals.PatrolGoal;
import com.playerstats.items.ModItems;
import com.playerstats.mixin.MobAccessor;
import com.playerstats.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;

import java.util.*;

public class ModAbilityEvents {

    public static void handleHorseTamed(AbstractHorse horse, Player tamer) {
        if (horse != null && tamer != null) {
            Services.PLATFORM.getPersistentData(horse).putUUID("Owner", tamer.getUUID());
        }
    }

    public static void handleEntityJoin(Entity entity, Level level) {
        if (entity instanceof AbstractHorse horse) {
            ((MobAccessor) horse).playerstats$getGoalSelector().addGoal(5, new FollowOwnerGoal(horse));
        }
        if (entity instanceof IronGolem golem) {
            ((MobAccessor) golem).playerstats$getTargetSelector().addGoal(1, new DefendOwnerTargetGoal(golem));
            ((MobAccessor) golem).playerstats$getTargetSelector().addGoal(4, new FollowOwnerGoal(golem));
            ((MobAccessor) golem).playerstats$getGoalSelector().addGoal(5, new PatrolGoal(golem, 1.0D, 16.0D));
        }
        if (entity instanceof Wolf wolf) {
            ((MobAccessor) wolf).playerstats$getGoalSelector().addGoal(5, new PatrolGoal(wolf, 1.0D, 16.0D));
        }
    }

    private static final Map<AbstractHorse, BlockPos> lastHorsePositions = new WeakHashMap<>();

    public static void handleHorseTick(LivingEntity entity) {
        if (!(entity instanceof AbstractHorse horse)) return;

        Level level = horse.level();
        if (level.isClientSide || !horse.onGround()) return;

        AttributeInstance attr = horse.getAttribute(ModAttributes.FROST_WALKER.get());
        if (attr == null || attr.getValue() != 1.0D) return;

        BlockPos currentPos = horse.blockPosition();
        BlockPos lastPos = lastHorsePositions.get(horse);

        boolean hasMoved = !currentPos.equals(lastPos);
        boolean shouldRefreshStill = (level.getGameTime() % 20 == 0);

        if (hasMoved || shouldRefreshStill) {
            lastHorsePositions.put(horse, currentPos);
            FrostWalkerEnchantment.onEntityMoved(horse, level, currentPos, 1);
        }
    }

    public static void handleMobDrops(LivingEntity entity, Collection<ItemEntity> drops) {
        AttributeInstance revivable = entity.getAttribute(ModAttributes.REBIRTH.get());
        if (revivable != null && revivable.getValue() == 1.0D) {
            ItemStack soulFragment = new ItemStack(ModItems.SOUL_FRAGMENT.get());
            if (entity instanceof ServerPlayer player) {
                CompoundTag soulData = new CompoundTag();
                player.saveWithoutId(soulData);
                soulData.putString("id", "minecraft:player");
                soulData.putString("UUID", player.getUUID().toString());
                soulFragment.getOrCreateTag().put("StoredEntity", soulData);
                soulFragment.getOrCreateTag().putString("StoredEntityName",
                        Component.Serializer.toJson(player.getDisplayName()));
            } else {
                CompoundTag soulData = new CompoundTag();
                entity.save(soulData);
                soulFragment.getOrCreateTag().put("StoredEntity", soulData);
                soulFragment.getOrCreateTag().putString("StoredEntityName",
                        Component.Serializer.toJson(entity.getDisplayName()));
            }

            ItemEntity drop = new ItemEntity(entity.level(),
                    entity.getX(), entity.getY(), entity.getZ(),
                    soulFragment);

            drops.add(drop);
        }
    }

    private static final Map<UUID, Long> howlCooldowns = new HashMap<>();

    public static void handleWolfCombat(Wolf wolf, LivingEntity target) {
        if (wolf == null || target == null || target == wolf) return;
        if (wolf.level().isClientSide()) return;

        LivingEntity owner = wolf.getOwner();
        if (owner == null && wolf.level() instanceof ServerLevel sl) {
            CompoundTag data = Services.PLATFORM.getPersistentData(wolf);
            if (data.hasUUID("Owner")) {
                owner = sl.getPlayerByUUID(data.getUUID("Owner"));
            }
        }
        if (owner != null && owner == target) return;

        var attrInstance = wolf.getAttribute(ModAttributes.HOWL_BUFF.get());
        if (attrInstance == null || attrInstance.getValue() <= 0.0D) return;

        int level = Math.max(1, Math.min(3, (int) Math.round(attrInstance.getValue())));

        Long lastHowl = howlCooldowns.get(wolf.getUUID());
        long cooldownTicks = 20L * Math.max(60, 240 - level * 60);
        if (lastHowl != null && wolf.level().getGameTime() - lastHowl < cooldownTicks) return;

        howlCooldowns.put(wolf.getUUID(), wolf.level().getGameTime());

        double howlRadius = 4.0D + (level * 4.0D);
        int durationTicks = 20 * (15 + level * 15);
        int amplifier = level >= 3 ? 1 : 0;

        wolf.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, durationTicks, amplifier));
        wolf.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, durationTicks, amplifier));
        wolf.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, durationTicks, amplifier));

        List<Wolf> allies = wolf.level().getEntitiesOfClass(Wolf.class,
                wolf.getBoundingBox().inflate(howlRadius),
                w -> w.isAlive() && w != wolf);

        for (Wolf ally : allies) {
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, durationTicks, amplifier));
            ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, durationTicks, amplifier));
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, durationTicks, amplifier));
        }

        if (owner instanceof Player playerOwner && playerOwner.isAlive() && wolf.distanceToSqr(playerOwner) <= (howlRadius * 2) * (howlRadius * 2)) {
            playerOwner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, durationTicks, amplifier));
            playerOwner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, durationTicks, amplifier));
            playerOwner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, durationTicks, amplifier));
        }

        if (wolf.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SOUL, wolf.getX(), wolf.getY() + 0.6, wolf.getZ(),
                    25 + level * 10, 0.4, 0.5, 0.4, 0.05);
        }

        wolf.playSound(SoundEvents.WOLF_HOWL, 1.4F, 1.0F);
    }

    public static void handleWolfTargetChange(LivingEntity entity, LivingEntity newTarget) {
        if (entity instanceof Wolf wolf && newTarget != null) {
            handleWolfCombat(wolf, newTarget);
        }
    }

    public static void handleWolfDamage(LivingEntity entity, DamageSource source) {
        if (entity instanceof Wolf wolf && source.getEntity() instanceof LivingEntity attacker) {
            handleWolfCombat(wolf, attacker);
        } else if (source.getEntity() instanceof Wolf wolf && entity != null) {
            handleWolfCombat(wolf, entity);
        }
    }
}
