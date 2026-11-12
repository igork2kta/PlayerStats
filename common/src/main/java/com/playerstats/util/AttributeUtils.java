package com.playerstats.util;

import com.playerstats.Config;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class AttributeUtils {

    public static double getIncrement(AttributeInstance instance) {
        if (instance == null) return 0.1;
        Attribute attr = instance.getAttribute();
        return calculateIncrement(attr, instance.getBaseValue());
    }

    public static double getIncrement(Attribute attr) {
        if (attr == null) return 0.1;
        double base = "attribute.name.generic.movement_speed".equals(attr.getDescriptionId()) ? 0.1 : attr.getDefaultValue();
        return calculateIncrement(attr, base);
    }

    public static double getIncrement(LivingEntity entity, Attribute attr) {
        AttributeInstance instance = entity != null ? getAttributeInstance(entity, attr) : null;
        return instance != null ? getIncrement(instance) : getIncrement(attr);
    }

    public static double getIncrement(String descriptionId) {
        if (descriptionId == null) return 0.1;

        if (Config.getCachedCustomAttributeIncrement().containsKey(descriptionId)) {
            return Config.getCachedCustomAttributeIncrement().get(descriptionId);
        }

        Attribute attr = getAttributeFromDescriptionId(descriptionId);
        if (attr != null) {
            return getIncrement(attr);
        }

        return 0.1;
    }

    private static double calculateIncrement(Attribute attr, double base) {
        if (attr == null) return 0.1;
        String descriptionId = attr.getDescriptionId();

        if (Config.getCachedCustomAttributeIncrement().containsKey(descriptionId)) {
            return Config.getCachedCustomAttributeIncrement().get(descriptionId);
        }

        String lowerId = descriptionId.toLowerCase();
        boolean isReduction = "attribute.name.generic.burning_time".equals(descriptionId)
                || "attribute.name.generic.fall_damage_multiplier".equals(descriptionId);

        if (base > 0) {
            double inc = Math.round((base * 0.1) * 10000.0) / 10000.0;
            if (lowerId.contains("attack_damage")) {
                inc = Math.max(0.5, inc);
            }
            return isReduction ? -inc : inc;
        }

        if (attr instanceof RangedAttribute ranged) {
            double max = ranged.getMaxValue();
            double inc;
            if (max <= 1.0) {
                inc = 0.05;
            } else if (max <= 10.0) {
                inc = 0.5;
            } else {
                inc = 1.0;
            }
            return isReduction ? -inc : inc;
        }

        return isReduction ? -0.1 : 0.1;
    }

    public static Attribute getAttributeFromDescriptionId(String descriptionId) {
        if (descriptionId == null) return null;
        for (Attribute attr : BuiltInRegistries.ATTRIBUTE) {
            if (descriptionId.equals(attr.getDescriptionId())) {
                return attr;
            }
        }
        return null;
    }

    public static Double getAttributeLimit(AttributeInstance instance) {
        if (instance == null) return null;
        Attribute attr = instance.getAttribute();
        return getAttributeLimit(attr, getIncrement(instance));
    }

    public static Double getAttributeLimit(Attribute attr) {
        if (attr == null) return null;
        return getAttributeLimit(attr, getIncrement(attr));
    }

    public static Double getAttributeLimit(LivingEntity entity, Attribute attr) {
        AttributeInstance instance = entity != null ? getAttributeInstance(entity, attr) : null;
        return instance != null ? getAttributeLimit(instance) : getAttributeLimit(attr);
    }

    private static Double getAttributeLimit(Attribute attr, double increment) {
        if ("attribute.name.generic.fall_damage_multiplier".equals(attr.getDescriptionId())) {
            return 0.5;
        }

        if (attr instanceof RangedAttribute ranged) {
            if (increment < 0) {
                return ranged.getMinValue();
            } else {
                double max = ranged.getMaxValue();
                if (max < 100000.0) {
                    return max;
                }
            }
        }
        return null;
    }

    public static boolean isMaxed(AttributeInstance instance) {
        if (instance == null) return false;
        return isMaxed(instance, instance.getValue());
    }

    public static boolean isMaxed(AttributeInstance instance, double value) {
        if (instance == null) return false;
        Attribute attr = instance.getAttribute();
        double increment = getIncrement(instance);

        if ("attribute.name.generic.fall_damage_multiplier".equals(attr.getDescriptionId())) {
            return value <= 0.5001;
        }

        if (attr instanceof RangedAttribute ranged) {
            if (increment > 0) {
                return value >= ranged.getMaxValue() - 0.0001;
            } else if (increment < 0) {
                return value <= ranged.getMinValue() + 0.0001;
            }
        }
        return false;
    }

    public static boolean isMaxed(LivingEntity entity, Attribute attr, AttributeInstance instance) {
        return isMaxed(instance);
    }

    public static boolean isMaxed(Attribute attr, AttributeInstance instance) {
        return isMaxed(instance);
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

    public static List<Attribute> getAttributes(LivingEntity entity, String searchText) {
        return BuiltInRegistries.ATTRIBUTE.stream()
                .filter(attr -> {
                    AttributeInstance instance = entity.getAttributes().getInstance(attr);
                    if (instance == null || !instance.getAttribute().isClientSyncable()) return false;
                    if (attr.getDescriptionId().contains("playerstats")) return false;
                    if (Config.getCachedIgnoredAttributes().contains(attr.getDescriptionId())) return false;
                    if (!searchText.isEmpty()) {
                        String name = AttributeUtils.getAttributeName(attr).toLowerCase();
                        return name.contains(searchText);
                    }
                    return true;
                }).toList();
    }

    public static AttributeInstance getAttributeInstance(LivingEntity entity, Attribute attribute) {
        return entity.getAttributes().getInstance(attribute);
    }

    public static List<AttributeInstance> getCustomAttributes(LivingEntity entity, String searchText) {
        return entity.getAttributes().getSyncableAttributes().stream().filter(attr -> {
            if (attr.getAttribute().getDescriptionId().contains("playerstats")) {
                if (entity instanceof Player player) {
                    if (attr.getAttribute().getDescriptionId().contains("rebirth") && !player.level().getLevelData().isHardcore()) return false;
                }

                if (!searchText.isEmpty()) {
                    String name = AttributeUtils.getAttributeName(attr.getAttribute()).toLowerCase();
                    return name.contains(searchText);
                }
                return true;
            }
            return false;
        }).toList();
    }

    public static double getAttributeValue(LivingEntity entity, Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance != null ? instance.getValue() : -1.0D;
    }

    public static double getAttributeValue(LivingEntity entity, Supplier<Attribute> attribute) {
        return getAttributeValue(entity, attribute.get());
    }
}
