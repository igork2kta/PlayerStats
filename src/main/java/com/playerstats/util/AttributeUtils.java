package com.playerstats.util;

import com.playerstats.Config;
import com.playerstats.PlayerStats;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;

public class AttributeUtils {

    public static double getIncrement(String descriptionId) {

        if (Config.cachedCustomAttributeIncrement.containsKey(descriptionId)) {
            return Config.cachedCustomAttributeIncrement.get(descriptionId);
        }

        return switch (descriptionId) {
            case "attribute.name.generic.luck" -> 1;
            case "attribute.name.generic.max_health"-> 2;
            //case "Max Mana", "Weight" -> 10;
            case "attribute.name.generic.movement_speed"-> 0.01;
            //case "Mana Regeneration" -> 0.01;
            case "forge.entity_reach", "forge.block_reach" -> 0.3;
            default -> 0.1;
        };
    }

    public static final Set<String> IGNORED_ATTRIBUTES = Set.of(
            "attribute.name.generic.armor",
            "forge.name_tag_distance",
            "forge.entity.gravity",
            "forge.step_height",
            "attribute.name.generic.armor_toughness");


    public static String getAttributeName(Attribute attr) {
        return Component.translatable(attr.getDescriptionId()).getString();
    }


    public static List<Attribute> getAttributes(LivingEntity entity, String searchText){
        return BuiltInRegistries.ATTRIBUTE.stream()
                .filter(attr -> {
                    AttributeInstance instance = entity.getAttributes().getInstance(attr);
                    //Aqui, removemos os atributos que não são editáveis (pelo menos a maioria)
                    if (instance == null || !instance.getAttribute().isClientSyncable()) return false;
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
        return entity.getAttributes().getInstance(attribute);
    }

    public static List<AttributeInstance> getCustomAttributes(LivingEntity entity, String searchText){

        return entity.getAttributes().getSyncableAttributes().stream().filter(attr -> {
            // Só pega os atributos do seu mod
            if (attr.getAttribute().getDescriptionId().contains("playerstats")) {
                if (!searchText.isEmpty()) {
                    String name = AttributeUtils.getAttributeName(attr.getAttribute()).toLowerCase();
                    return name.contains(searchText);
                }
                return true;
            }
            return false;
        }).toList();




    }


}