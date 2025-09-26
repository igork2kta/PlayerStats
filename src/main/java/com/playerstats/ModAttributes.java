package com.playerstats;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
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
                            .setSyncable(true));

    public static final RegistryObject<Attribute> LIFE_REGEN =
            ATTRIBUTES.register("life_regen",
                    () -> new RangedAttribute("attribute.playerstats.life_regen", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final RegistryObject<Attribute> FROST_WALKER =
            ATTRIBUTES.register("frost_walker",
                    () -> new RangedAttribute("attribute.playerstats.frost_walker", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));


    public static final RegistryObject<Attribute> DEFEND_OWNER =
            ATTRIBUTES.register("defend_owner",
                    () -> new RangedAttribute("attribute.playerstats.defend_owner", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final RegistryObject<Attribute> REBIRTH =
            ATTRIBUTES.register("rebirth",
                    () -> new RangedAttribute("attribute.playerstats.rebirth", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    //Atrair atenção de mobs hostis
    public static final RegistryObject<Attribute> TAUNT =
            ATTRIBUTES.register("taunt",
                    () -> new RangedAttribute("attribute.playerstats.taunt", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    //Atrair atenção de mobs hostis
    public static final RegistryObject<Attribute> SCOUT_VISION =
            ATTRIBUTES.register("scout_vision",
                    () -> new RangedAttribute("attribute.playerstats.scout_vision", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    //Atrair atenção de mobs hostis
    public static final RegistryObject<Attribute> HOWL_BUFF  =
            ATTRIBUTES.register("howl_buff",
                    () -> new RangedAttribute("attribute.playerstats.howl_buff", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    //Atrair atenção de mobs hostis
    public static final RegistryObject<Attribute> PATROL  =
            ATTRIBUTES.register("patrol",
                    () -> new RangedAttribute("attribute.playerstats.patrol", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));



    //Aqui são atribuidos os atributos customizados às entidadess
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {

        // Itera sobre todas as entidades vivas
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {

            // Adiciona apenas para os tipos de criatura pacíficos e player
            if (type.getCategory() == MobCategory.CREATURE || type == EntityType.PLAYER) {
                event.add(type, ModAttributes.REBIRTH.get()); //Será ocultado para o player caso não seja hardcore
            }

            if (type != EntityType.PLAYER) {
                //Regen para todas as entidades, menos player
                event.add(type, ModAttributes.LIFE_REGEN.get());
            }

            if(type == EntityType.HORSE || type == EntityType.DONKEY ||  type == EntityType.MULE || type == EntityType.CAMEL){
                event.add(type, ModAttributes.FOLLOW_OWNER.get());
                event.add(type, ModAttributes.TELEPORT_TO_OWNER.get());
                event.add(type, ModAttributes.FROST_WALKER.get());
            }

            if(type == EntityType.IRON_GOLEM){
                event.add(type, ModAttributes.FOLLOW_OWNER.get());
                event.add(type, ModAttributes.TELEPORT_TO_OWNER.get());
                event.add(type, ModAttributes.DEFEND_OWNER.get());
                event.add(type, ModAttributes.TAUNT.get());
                event.add(type, ModAttributes.PATROL.get());
            }

            if(type == EntityType.WOLF){
                event.add(type, ModAttributes.TAUNT.get());
                event.add(type, ModAttributes.HOWL_BUFF.get());
                event.add(type, ModAttributes.PATROL.get());
            }

            if(type == EntityType.PARROT){
                event.add(type, ModAttributes.SCOUT_VISION.get());
            }

        }
    }
}
