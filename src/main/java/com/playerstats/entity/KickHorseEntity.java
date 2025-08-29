package com.playerstats.entity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KickHorseEntity extends Horse implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Definindo a animação
    private static final RawAnimation KICK_ANIM = RawAnimation.begin().then("animation.horse.kick", Animation.LoopType.PLAY_ONCE);


    public KickHorseEntity(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.JUMP_STRENGTH, 0.7D);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // Aqui registramos o controller da animação
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            // animação default = idle
            return state.setAndContinue(KICK_ANIM);
        })
                // aqui registramos o gatilho "kick"
                .triggerableAnim("kick", KICK_ANIM));
    }

    // Método utilitário para disparar o coice
    public void doKick() {
        this.triggerAnim("controller", "kick");
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.level().getEntitiesOfClass(net.minecraft.world.entity.player.Player.class, this.getBoundingBox().inflate(1.5D))
                    .forEach(player -> {
                        if (player.isAlive() && player.position().z > this.position().z) {
                            // dispara animação de coice
                            this.triggerAnim("controller", "kick");

                            // opcional: causar dano
                            player.hurt(this.damageSources().mobAttack(this), 6.0F);
                        }
                    });
        }
    }

}
