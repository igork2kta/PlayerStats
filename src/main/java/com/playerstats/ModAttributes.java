package com.playerstats;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
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

    public static final DeferredHolder<Attribute, Attribute> FROST_WALKER =
            ATTRIBUTES.register("frost_walker",
                    () -> new RangedAttribute("attribute.playerstats.frost_walker", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> DEFEND_OWNER =
            ATTRIBUTES.register("defend_owner",
                    () -> new RangedAttribute("attribute.playerstats.defend_owner", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> REBIRTH =
            ATTRIBUTES.register("rebirth",
                    () -> new RangedAttribute("attribute.playerstats.rebirth", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    //Atrair atenção de mobs hostis
    public static final DeferredHolder<Attribute, Attribute> TAUNT =
            ATTRIBUTES.register("taunt",
                    () -> new RangedAttribute("attribute.playerstats.taunt", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    //Atrair atenção de mobs hostis
    public static final DeferredHolder<Attribute, Attribute> SCOUT_VISION =
            ATTRIBUTES.register("scout_vision",
                    () -> new RangedAttribute("attribute.playerstats.scout_vision", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    //Atrair atenção de mobs hostis
    public static final DeferredHolder<Attribute, Attribute> HOWL_BUFF  =
            ATTRIBUTES.register("howl_buff",
                    () -> new RangedAttribute("attribute.playerstats.howl_buff", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));


    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {

        // Itera sobre todas as entidades vivas
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {

            // Adiciona apenas para os tipos de criatura pacíficos e player
            if (type.getCategory() == MobCategory.CREATURE || type == EntityType.PLAYER) {
                event.add(type, ModAttributes.REBIRTH.getDelegate()); //Será ocultado para o player caso não seja hardcore
            }

            if (type != EntityType.PLAYER) {
                //Regen para todas as entidades, menos player
                event.add(type, ModAttributes.LIFE_REGEN.getDelegate());
            }

            if(type == EntityType.HORSE || type == EntityType.DONKEY ||  type == EntityType.MULE || type == EntityType.CAMEL){
                event.add(type, ModAttributes.FOLLOW_OWNER.getDelegate());
                event.add(type, ModAttributes.TELEPORT_TO_OWNER.getDelegate());
                event.add(type, ModAttributes.FROST_WALKER.getDelegate());
            }

            if(type == EntityType.IRON_GOLEM){
                event.add(type, ModAttributes.FOLLOW_OWNER.getDelegate());
                event.add(type, ModAttributes.TELEPORT_TO_OWNER.getDelegate());
                event.add(type, ModAttributes.DEFEND_OWNER.getDelegate());
                event.add(type, ModAttributes.TAUNT.getDelegate());
            }

            if(type == EntityType.WOLF){
                event.add(type, ModAttributes.TAUNT.getDelegate());
                event.add(type, ModAttributes.HOWL_BUFF.getDelegate());
            }

            if(type == EntityType.PARROT){
                event.add(type, ModAttributes.SCOUT_VISION.getDelegate());
            }

        }
    }
}
