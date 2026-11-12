package com.playerstats.event;

import com.playerstats.ModAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public class VampirismHandler {

    public static void handleLivingDamage(LivingEntity victim, DamageSource source, float damageDealt) {
        if (source == null || !(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        // Não aplica roubo de vida se o dano for a si mesmo
        if (attacker.equals(victim)) {
            return;
        }

        AttributeInstance vampirismAttr = attacker.getAttribute(ModAttributes.VAMPIRISM);
        if (vampirismAttr == null) {
            return;
        }

        double levelValue = vampirismAttr.getValue();
        int level = (int) Math.round(levelValue);

        if (level <= 0) {
            return;
        }

        // Chance de disparo: 30% no nível 1, aumentando 5% a cada nível (30%, 35%, 40%, 45%, 50%)
        float procChance = 0.30F + (level - 1) * 0.05F;
        if (attacker.getRandom().nextFloat() > procChance) {
            return;
        }

        if (damageDealt <= 0.0F) {
            return;
        }

        float healRatio = 0.10F + (level - 1) * 0.02F;
        float healAmount = Math.max(1.0F, damageDealt * healRatio);

        if (attacker.getHealth() < attacker.getMaxHealth()) {
            attacker.heal(healAmount);

            if (attacker.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.DAMAGE_INDICATOR,
                        victim.getX(),
                        victim.getY() + (victim.getBbHeight() / 2.0),
                        victim.getZ(),
                        5,
                        0.2,
                        0.2,
                        0.2,
                        0.05
                );

                serverLevel.sendParticles(
                        ParticleTypes.HEART,
                        attacker.getX(),
                        attacker.getY() + (attacker.getBbHeight() / 2.0),
                        attacker.getZ(),
                        3,
                        0.25,
                        0.25,
                        0.25,
                        0.05
                );

                serverLevel.playSound(
                        null,
                        attacker.getX(),
                        attacker.getY(),
                        attacker.getZ(),
                        SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH,
                        SoundSource.PLAYERS,
                        0.6F,
                        1.4F
                );
            }
        }
    }
}
