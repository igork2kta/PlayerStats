package com.playerstats.fabric.mixin;

import com.playerstats.platform.IPersistentDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    private void playerstats$savePersistentData(ValueOutput valueOutput, CallbackInfo ci) {
        if (this.playerstats$persistentData != null && !this.playerstats$persistentData.isEmpty()) {
            valueOutput.store("PlayerStatsData", CompoundTag.CODEC, this.playerstats$persistentData);
        }
    }

    @Inject(method = "load", at = @At("HEAD"))
    private void playerstats$loadPersistentData(ValueInput valueInput, CallbackInfo ci) {
        valueInput.read("PlayerStatsData", CompoundTag.CODEC).ifPresent(tag -> {
            this.playerstats$persistentData = tag;
        });
        if (this.playerstats$persistentData == null) {
            valueInput.read("ForgeData", CompoundTag.CODEC).ifPresent(tag -> {
                this.playerstats$persistentData = tag.copy();
            });
        }
    }
}
