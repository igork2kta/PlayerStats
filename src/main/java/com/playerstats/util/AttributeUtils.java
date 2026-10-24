package com.playerstats.util;

import com.playerstats.Config;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class AttributeUtils {

    /**
     * Sanitiza qualquer identificador de atributo ou translation key (ex: apothic_attributes:arrow_velocity)
     * para um path valido em ResourceLocation ([a-z0-9/._-]).
     */
    public static String sanitizeToPath(String raw) {
        if (raw == null) return "unknown";
        return raw.replace(':', '.');
    }

    public static double getIncrement(AttributeInstance instance) {
        if (instance == null) return 0.1;
        Attribute attr = instance.getAttribute().value();
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

        if (Config.cachedCustomAttributeIncrement.containsKey(descriptionId)) {
            return Config.cachedCustomAttributeIncrement.get(descriptionId);
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

        if (Config.cachedCustomAttributeIncrement.containsKey(descriptionId)) {
            return Config.cachedCustomAttributeIncrement.get(descriptionId);
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

        // Se a base for 0.0 (Zero-Base), deduz pela escala do RangedAttribute
        if (attr instanceof RangedAttribute ranged) {
            double max = ranged.getMaxValue();
            double inc;
            if (max <= 1.0) {
                inc = 0.05;      // Ex: 5% para taxas/porcentagens (crit, knockback resistance, etc.)
            } else if (max <= 10.0) {
                inc = 0.5;       // Ex: escalas curtas (sorte, knockback)
            } else {
                inc = 1.0;       // Ex: escalas inteiras (armadura, vida extra, etc.)
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
        Attribute attr = instance.getAttribute().value();
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
        Attribute attr = instance.getAttribute().value();
        double increment = getIncrement(instance);
        double currentValue = instance.getValue();

        if ("attribute.name.generic.fall_damage_multiplier".equals(attr.getDescriptionId())) {
            return currentValue <= 0.5001;
        }

        if (attr instanceof RangedAttribute ranged) {
            if (increment > 0) {
                return currentValue >= ranged.getMaxValue() - 0.0001;
            } else if (increment < 0) {
                return currentValue <= ranged.getMinValue() + 0.0001;
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
        AttributeInstance instance = entity.getAttributes().getInstance(attribute);
        return instance != null ? instance.getValue() : -1.0D;
    }


}