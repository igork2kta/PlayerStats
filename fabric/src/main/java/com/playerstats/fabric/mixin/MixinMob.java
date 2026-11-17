package com.playerstats.fabric.mixin;

import com.playerstats.event.ModAbilityEvents;
import com.playerstats.event.TauntHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Mob.class)
public class MixinMob {

    @ModifyVariable(method = "setTarget", at = @At("HEAD"), argsOnly = true)
    private LivingEntity playerstats$modifyTarget(LivingEntity target) {
        Mob self = (Mob) (Object) this;
        LivingEntity evaluated = TauntHandler.evaluateTarget(self, target);
        ModAbilityEvents.handleWolfTargetChange(self, evaluated);
        return evaluated;
    }
}
