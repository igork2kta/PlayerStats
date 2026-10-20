package com.playerstats.event;

import com.playerstats.ModAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = "playerstats")
public class VampirismHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        // Não aplica roubo de vida se o dano for a si mesmo
        if (attacker.equals(event.getEntity())) {
            return;
        }

        AttributeInstance vampirismAttr = attacker.getAttribute(ModAttributes.VAMPIRISM.getDelegate());
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

        // Quantidade de cura: 10% do dano causado + 2% por nível acima de 1 (10%, 12%, 14%, 16%, 18%)
        float damageDealt = event.getNewDamage();
        if (damageDealt <= 0.0F) {
            return;
        }

        float healRatio = 0.10F + (level - 1) * 0.02F;
        float healAmount = Math.max(1.0F, damageDealt * healRatio);

        if (attacker.getHealth() < attacker.getMaxHealth()) {
            attacker.heal(healAmount);

            // Efeito de partículas de cura e som suave de absorção de sangue/vida
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

                // Som temático
                serverLevel.playSound(
                        null,
                        attacker.getX(),
                        attacker.getY(),
                        attacker.getZ(),
                        SoundEvents.SOUL_ESCAPE.value(),
                        SoundSource.PLAYERS,
                        0.4F,
                        1.4F + (level * 0.1F)
                );
            }
        }
    }
}

