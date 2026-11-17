package com.playerstats.fabric.mixin;

import com.playerstats.event.MilkyAuraHandler;
import com.playerstats.event.ModAbilityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    @Inject(method = "tick", at = @At("TAIL"))
    private void playerstats$onTick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        MilkyAuraHandler.handleEntityTick(self);
        ModAbilityEvents.handleHorseTick(self);
    }

    @Inject(method = "dropCustomDeathLoot", at = @At("TAIL"))
    private void playerstats$onDropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean bl, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        List<ItemEntity> drops = new ArrayList<>();
        ModAbilityEvents.handleMobDrops(self, drops);
        for (ItemEntity drop : drops) {
            serverLevel.addFreshEntity(drop);
        }
    }
}
