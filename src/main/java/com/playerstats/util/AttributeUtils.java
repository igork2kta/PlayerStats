package com.playerstats.util;

import com.playerstats.Config;
import com.playerstats.ModAttributes;
import com.playerstats.PlayerStats;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class AttributeUtils {

    public static double getIncrement(String descriptionId) {

        if (Config.cachedCustomAttributeIncrement.containsKey(descriptionId)) {
            return Config.cachedCustomAttributeIncrement.get(descriptionId);
        }

        return switch (descriptionId) {
            case "attribute.name.generic.luck" -> 1;
            case "attribute.name.generic.max_health"-> 2;
            case "attribute.name.generic.movement_speed"-> 0.01;
            case "attribute.name.player.entity_interaction_range", "attribute.name.player.block_interaction_range" -> 0.3;
            case "attribute.name.generic.burning_time" -> - 0.1;
            case "attribute.name.generic.jump_strength" -> 0.07;
            case "attribute.name.generic.safe_fall_distance" -> 0.5;
            default -> 0.1;
        };
    }

    public static final List<String> IGNORED_ATTRIBUTES = List.of(
            "attribute.name.generic.armor",
            "attribute.name.forge.name_tag_distance",
            "attribute.name.generic.gravity",
            "attribute.name.generic.step_height",
            "attribute.name.generic.armor_toughness",
            "neoforge.creative_flight",
            "attribute.name.generic.max_absorption",
            "neoforge.name_tag_distance",
            "attribute.name.generic.scale",
            "attribute.name.generic.movement_efficiency");


    public static String getAttributeName(Attribute attr) {
        return Component.translatable(attr.getDescriptionId()).getString();
    }


    public static List<Attribute> getAttributes(LivingEntity entity, String searchText){
        return BuiltInRegistries.ATTRIBUTE.stream()
                .filter(attr -> {

                    AttributeInstance instance = getAttributeInstance(entity, attr);
                    //Aqui, removemos os atributos que não são editáveis (pelo menos a maioria)
                    if (!attr.isClientSyncable()) return false;
                    if (instance == null) return false;
                    //Aqui removemos os atributos do mod, vai aparecer em outra aba com tratamneto diferente
                    if (attr.getDescriptionId().contains("playerstats")) return false;
                    //Aqui, removemos os que não queremos que apareça
                    if (Config.cachedIgnoredAttributes.contains(attr.getDescriptionId())) return false;
                    if (!searchText.isEmpty()) {
                        String name = AttributeUtils.getAttributeName(attr).toLowerCase();
                        return name.contains(searchText);
                    }
                    return true;
                }).toList();
    }

    public static AttributeInstance getAttributeInstance(LivingEntity entity, Attribute attribute){

        // Converte Attribute para Holder<Attribute>
        ResourceLocation attrId = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
        if (attrId == null) return null;


        var optionalHolder = BuiltInRegistries.ATTRIBUTE.getHolder(attrId);
        if (optionalHolder.isEmpty()) return null;


        Holder<Attribute> holder = optionalHolder.get();

        return entity.getAttribute(holder);
    }

    public static Attribute getAttributeFromId(String attributeId) {
        ResourceLocation id = ResourceLocation.tryParse(attributeId);
        if (id == null) {
            return null;
        }
        return BuiltInRegistries.ATTRIBUTE.get(id);
    }

    public static List<AttributeInstance> getCustomAttributes(LivingEntity entity, String searchText){

        return entity.getAttributes().getSyncableAttributes().stream().filter(attr -> {
            // Só pega os atributos do seu mod
            if (attr.getAttribute().value().getDescriptionId().contains("playerstats")) {
                if(entity instanceof  Player player){
                    //Não permite rebirth para player se não for Hardcore
                    if(attr.getAttribute().value().getDescriptionId().contains("rebirth") && !player.level().getLevelData().isHardcore()) return false;
                }

                if (!searchText.isEmpty()) {
                    String name = AttributeUtils.getAttributeName(attr.getAttribute().value()).toLowerCase();
                    return name.contains(searchText);
                }
                return true;
            }
            return false;
        }).toList();
    }

    public static double getAttributeValue(LivingEntity entity, DeferredHolder<Attribute, Attribute> attribute){
        return entity.getAttributes().getInstance(attribute).getValue();
    }


}