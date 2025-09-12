package com.playerstats;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;


public class ModAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, "playerstats");

    public static final DeferredHolder<Attribute, Attribute> FOLLOW_OWNER =
            ATTRIBUTES.register("follow_owner",
                    () -> new RangedAttribute("attribute.playerstats.follow_owner", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> TELEPORT_TO_OWNER =
            ATTRIBUTES.register("teleport_to_owner",
                    () -> new RangedAttribute("attribute.playerstats.teleport_to_owner", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> LIFE_REGEN =
            ATTRIBUTES.register("life_regen",
                    () -> new RangedAttribute("attribute.playerstats.life_regen", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            if (type == EntityType.PLAYER) {
                continue;
            } else {
                event.add(type, ModAttributes.LIFE_REGEN.getDelegate());
            }
            if (type == EntityType.HORSE || type == EntityType.DONKEY || type == EntityType.MULE) {
                event.add(type, ModAttributes.FOLLOW_OWNER.getDelegate());
                event.add(type, ModAttributes.TELEPORT_TO_OWNER.getDelegate());
            }
        }
    }

}
