package com.playerstats;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "playerstats", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, "playerstats");

    public static final RegistryObject<Attribute> FOLLOW_OWNER =
            ATTRIBUTES.register("follow_owner",
                    () -> new RangedAttribute("attribute.playerstats.follow_owner", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true)); // precisa ser syncable para ir ao cliente

    public static final RegistryObject<Attribute> TELEPORT_TO_OWNER =
            ATTRIBUTES.register("teleport_to_owner",
                    () -> new RangedAttribute("attribute.playerstats.teleport_to_owner", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true)); // precisa ser syncable para ir ao cliente

    public static final RegistryObject<Attribute> LIFE_REGEN =
            ATTRIBUTES.register("life_regen",
                    () -> new RangedAttribute("attribute.playerstats.life_regen", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true)); // precisa ser syncable para ir ao cliente



    //Aqui são atribuidos os atributos customizados às entidadess
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {

        // Itera sobre todas as entidades vivas
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            // Ignora jogador
            if (type == EntityType.PLAYER) {
                continue;
            }
            else{
                //Regen para todas as entidades, menos player
                event.add(type, ModAttributes.LIFE_REGEN.get());
            }
            if(type == EntityType.HORSE || type == EntityType.DONKEY ||  type == EntityType.MULE){
                event.add(type, ModAttributes.FOLLOW_OWNER.get());
                event.add(type, ModAttributes.TELEPORT_TO_OWNER.get());
            }

        }
    }
}
