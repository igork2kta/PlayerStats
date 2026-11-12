package com.playerstats.fabric.mixin;

import com.playerstats.platform.IPersistentDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity implements IPersistentDataHolder {

    @Unique
    private CompoundTag playerstats$persistentData;

    @Override
    public CompoundTag playerstats$getPersistentData() {
        if (this.playerstats$persistentData == null) {
            this.playerstats$persistentData = new CompoundTag();
        }
        return this.playerstats$persistentData;
    }

    @Inject(method = "saveWithoutId", at = @At("HEAD"))
    private void playerstats$savePersistentData(CompoundTag compoundTag, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.playerstats$persistentData != null && !this.playerstats$persistentData.isEmpty()) {
            compoundTag.put("PlayerStatsData", this.playerstats$persistentData);
        }
    }

    @Inject(method = "load", at = @At("HEAD"))
    private void playerstats$loadPersistentData(CompoundTag compoundTag, CallbackInfo ci) {
        if (compoundTag.contains("PlayerStatsData", 10)) {
            this.playerstats$persistentData = compoundTag.getCompound("PlayerStatsData");
        } else if (compoundTag.contains("ForgeData", 10)) {
            this.playerstats$persistentData = compoundTag.getCompound("ForgeData").copy();
        }
    }
}
