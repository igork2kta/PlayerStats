package com.playerstats.fabric.mixin;

import com.playerstats.ModAttributes;
import com.playerstats.mixin.AttributeSupplierAccessor;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(DefaultAttributes.class)
public class MixinDefaultAttributes {

    @Shadow
    private static Map<EntityType<? extends LivingEntity>, AttributeSupplier> SUPPLIERS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void playerstats$injectAttributes(CallbackInfo ci) {
        for (Map.Entry<EntityType<? extends LivingEntity>, AttributeSupplier> entry : SUPPLIERS.entrySet()) {
            EntityType<? extends LivingEntity> type = entry.getKey();
            AttributeSupplier supplier = entry.getValue();

            Map<Holder<Attribute>, AttributeInstance> instances = new HashMap<>(
                    ((AttributeSupplierAccessor) supplier).playerstats$getInstances()
            );

            ModAttributes.applyEntityAttributes(type, (t, attr) -> {
                instances.put(attr, new AttributeInstance(attr, instance -> {}));
            });

            ((AttributeSupplierAccessor) supplier).playerstats$setInstances(instances);
        }
    }
}
