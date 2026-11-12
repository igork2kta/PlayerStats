package com.playerstats.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AttributeSupplier.class)
public interface AttributeSupplierAccessor {

    @Accessor("instances")
    Map<Holder<Attribute>, AttributeInstance> playerstats$getInstances();

    @Accessor("instances")
    @Mutable
    void playerstats$setInstances(Map<Holder<Attribute>, AttributeInstance> instances);
}
