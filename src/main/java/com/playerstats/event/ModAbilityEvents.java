package com.playerstats.event;

import com.playerstats.ModAttributes;
import com.playerstats.entities.goals.DefendOwnerTargetGoal;
import com.playerstats.entities.goals.FollowOwnerGoal;
import com.playerstats.entities.goals.PatrolGoal;
import com.playerstats.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.swing.text.html.parser.Entity;
import java.util.*;


@Mod.EventBusSubscriber(modid = "playerstats")
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
            golem.targetSelector.addGoal(4, new FollowOwnerGoal(golem)); //menor prioridade
            golem.goalSelector.addGoal(5, new PatrolGoal(golem, 1.0D, 16.0D));
        }
        if (event.getEntity() instanceof Wolf wolf) {
            wolf.goalSelector.addGoal(5, new PatrolGoal(wolf, 1.0D, 16.0D));
        }
    }

    private static final Map<AbstractHorse, BlockPos> lastHorsePositions = new WeakHashMap<>();

    // FROST WALKER
    @SubscribeEvent
    public static void onHorseTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof AbstractHorse horse)) return;

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
    //REBIRTH (droppar fragmento)
    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        AttributeInstance revivable = entity.getAttribute(ModAttributes.REBIRTH.get());
        if (revivable != null && revivable.getValue() == 1.0D) {
            ItemStack soulFragment = new ItemStack(ModItems.SOUL_FRAGMENT.get());
            System.out.println(Entity.NAME);
            if (entity instanceof ServerPlayer player) {
                CompoundTag soulData = new CompoundTag();
                player.saveWithoutId(soulData); // salva inventário, XP etc.
                soulFragment.getOrCreateTag().put("StoredEntity", soulData);
                soulData.putString("id", "minecraft:player"); // <- forçado aqui
                // Salva explicitamente UUID
                soulData.putString("UUID", player.getUUID().toString());
                soulFragment.getOrCreateTag().putString("StoredEntityName",
                        Component.Serializer.toJson(player.getDisplayName()));
            }
            else {
                // mobs normais
                CompoundTag soulData = new CompoundTag();
                entity.save(soulData);
                soulFragment.getOrCreateTag().put("StoredEntity", soulData);
                soulFragment.getOrCreateTag().putString("StoredEntityName",
                        Component.Serializer.toJson(entity.getDisplayName()));
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
        if (wolf.getOwner() == null) return;
        if(event.getNewTarget() == null) return;

        // Checa se o atributo HOWL_BUFF está ativo (1 a 3)
        var attrInstance = wolf.getAttribute(ModAttributes.HOWL_BUFF.get());
        if (attrInstance == null || attrInstance.getValue() <= 0.0D) return;

        int level = (int) Math.round(attrInstance.getValue());
        // Checa cooldown
        long lastHowl = howlCooldowns.getOrDefault(wolf.getUUID(), 0L);

        if (wolf.level().getGameTime() - lastHowl < HOWL_COOLDOWN_TICKS) return;

        // Atualiza cooldown
        howlCooldowns.put(wolf.getUUID(), wolf.level().getGameTime());

        // Raio e duração por nível
        double howlRadius = 4.0D + (level * 4.0D); // Nv 1: 8b, Nv 2: 12b, Nv 3: 16b
        int durationTicks = 20 * (15 + level * 15); // Nv 1: 30s (600t), Nv 2: 45s (900t), Nv 3: 60s (1200t)
        int amplifier = level >= 3 ? 1 : 0; // Nv 1 e 2: efeito nível 1 (amplifier 0), Nv 3: efeito nível 2 (amplifier 1)


        // Buffa aliados próximos (outros lobos vivos)
        List<Wolf> allies = wolf.level().getEntitiesOfClass(Wolf.class,
                wolf.getBoundingBox().inflate(howlRadius),
                w -> w.isAlive() && w != wolf);

        for (Wolf ally : allies) {
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, durationTicks, amplifier)); // +dano 60s
            ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, durationTicks, amplifier)); // +velocidade 60s
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, durationTicks, amplifier)); // +resistência 60s
        }

        // Buffa o dono do lobo
        // Vai que algum animal adiciona a habilidade a um lobo selvagem, então checa se o dono é um player
        if (wolf.getOwner() instanceof Player owner) {
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, durationTicks, amplifier)); // +dano 60s
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, durationTicks, amplifier)); // +velocidade 60s
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, durationTicks, amplifier)); // +resistência 60s
        }

        // Buffa o próprio lobo
        wolf.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, durationTicks, amplifier)); // +dano 60s
        wolf.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, durationTicks, amplifier)); // +velocidade 60s
        wolf.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, durationTicks, amplifier)); // +resistência 60s

        // Efeito visual e sonoro
        if (wolf.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SOUL, wolf.getX(), wolf.getY() + 1, wolf.getZ(),
                    20 + level * 10, 0.5, 0.5, 0.5, 0.1);
        }
        wolf.playSound(SoundEvents.WOLF_HOWL, 1.0F, 1.0F);

    }




}
