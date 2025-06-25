package com.playerstats.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class AttributeUtils {

    public static double getIncrement(String name) {
        return switch (name) {
            case "Max Health", "Luck" -> 1;
            case "Max Mana", "Weight" -> 10;
            case "Speed", "Mana Regeneration" -> 0.01;
            case "Entity Reach", "Block Reach" -> 0.3;
            default -> 0.1;
        };
    }

    public static String getAttributeName(Attribute attr) {
        return Component.translatable(attr.getDescriptionId()).getString();
    }
}