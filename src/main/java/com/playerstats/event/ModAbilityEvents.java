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
            golem.targetSelector.addGoal(3, new FollowOwnerGoal(golem)); //menor prioridade
            golem.goalSelector.addGoal(5, new PatrolGoal(golem, 1.0D, 16.0D));

        }
    }

    //FROST WALKER
    @SubscribeEvent
    public static void onHorseTick(LivingEvent.LivingTickEvent event) {

            if (!(event.getEntity() instanceof AbstractHorse horse)) return;
            Level level = horse.level();
            if (level.isClientSide) return;

            //por algum motivo isso da exceção às vezes
            boolean hasFrostWalker = ModAttributes.FROST_WALKER.getHolder()
                    .flatMap(holder -> Optional.ofNullable(horse.getAttributes().getInstance(holder)))
                    .map(instance -> instance.getValue() == 1)
                    .orElse(false);

            if (hasFrostWalker) {
                BlockPos pos = horse.blockPosition();

                // reaproveitar a lógica vanilla do Frost Walker
                int frostLevel = 1; // pode ser fixo ou dinâmico
                FrostWalkerEnchantment.onEntityMoved(horse, level, pos, frostLevel);
            }
    }

    //REBIRTH (droppar fragmento)
    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();

        AttributeInstance revivable = entity.getAttribute(ModAttributes.REBIRTH.get());
        if (revivable != null && revivable.getValue() == 1.0D) {

            ItemStack soulFragment = new ItemStack(ModItems.SOUL_FRAGMENT.get());
            if (entity instanceof ServerPlayer player) {
                CompoundTag soulData = new CompoundTag();
                player.saveWithoutId(soulData); // salva inventário, XP etc.
                soulFragment.getOrCreateTag().put("StoredEntity", soulData);
                soulData.putString("id", "minecraft:player"); // <- forçado aqui
                // Salva explicitamente UUID
                soulData.putString("UUID", player.getUUID().toString());
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
        // Checa cooldown
        long lastHowl = howlCooldowns.getOrDefault(wolf.getUUID(), 0L);
        if (wolf.level().getGameTime() - lastHowl < HOWL_COOLDOWN_TICKS) return;

        // Checa se o atributo HOWL_BUFF está ativo
        if (wolf.getAttribute(ModAttributes.HOWL_BUFF.get()).getValue() != 1) return;

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




}
