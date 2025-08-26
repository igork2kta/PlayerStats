package com.playerstats.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "playerstats", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "playerstats");

    public static final RegistryObject<EntityType<DualSaddleHorse>> DUAL_SADDLE_HORSE =
            ENTITIES.register("dual_saddle_horse",
                    () -> EntityType.Builder.of(DualSaddleHorse::new, MobCategory.CREATURE)
                            .sized(1.3965F, 1.6F) // igual cavalo vanilla
                            .build("dual_saddle_horse"));


    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DUAL_SADDLE_HORSE.get(),
                Horse.createBaseHorseAttributes().build());
    }
}


