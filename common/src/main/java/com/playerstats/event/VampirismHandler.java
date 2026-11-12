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

    public static void handleLivingDamage(LivingEntity target, DamageSource source, float amount) {
        if (source == null || !(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        if (attacker.equals(target)) {
            return;
        }

        AttributeInstance vampirismAttr = attacker.getAttribute(ModAttributes.VAMPIRISM.get());
        if (vampirismAttr == null) {
            return;
        }

        double levelValue = vampirismAttr.getValue();
        int level = (int) Math.round(levelValue);

        if (level <= 0) {
            return;
        }

        float procChance = 0.30F + (level - 1) * 0.05F;
        if (attacker.getRandom().nextFloat() > procChance) {
            return;
        }

        if (amount <= 0.0F) {
            return;
        }

        float healRatio = 0.10F + (level - 1) * 0.02F;
        float healAmount = Math.max(1.0F, amount * healRatio);

        if (attacker.getHealth() < attacker.getMaxHealth()) {
            attacker.heal(healAmount);

            if (attacker.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.HEART,
                        attacker.getX(),
                        attacker.getY() + attacker.getBbHeight() * 0.7D,
                        attacker.getZ(),
                        Math.min(5, level + 1),
                        0.25D,
                        0.25D,
                        0.25D,
                        0.05D
                );

                serverLevel.playSound(
                        null,
                        attacker.getX(),
                        attacker.getY(),
                        attacker.getZ(),
                        SoundEvents.SOUL_ESCAPE,
                        SoundSource.PLAYERS,
                        0.4F,
                        1.4F + (level * 0.1F)
                );
            }
        }
    }
}
