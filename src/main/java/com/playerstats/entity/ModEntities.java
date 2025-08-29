package com.playerstats.entity;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    // Cria um registro para entidades
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "playerstats");

    // Registra o cavalo que dá coice
    public static final RegistryObject<EntityType<KickHorseEntity>> KICK_HORSE =
            ENTITY_TYPES.register("kick_horse",
                    () -> EntityType.Builder.of(KickHorseEntity::new, MobCategory.CREATURE)
                            .sized(1.3965F, 1.6F) // largura e altura igual cavalo vanilla
                            .build("kick_horse"));
}
