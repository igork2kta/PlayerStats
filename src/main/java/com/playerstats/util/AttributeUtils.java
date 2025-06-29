package com.playerstats.util;

import com.playerstats.Config;
import com.playerstats.PlayerStats;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;

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
}