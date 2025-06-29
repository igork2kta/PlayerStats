package com.playerstats;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = PlayerStats.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final ForgeConfigSpec COMMON;
    public static final ForgeConfigSpec.BooleanValue DEBUG_MODE;
    public static final ForgeConfigSpec.ConfigValue HIGH_HEALTH;
    public static final ForgeConfigSpec.DoubleValue WITHER_CHANCE;
    public static final ForgeConfigSpec.DoubleValue ENDER_DRAGON_CHANCE;
    public static final ForgeConfigSpec.DoubleValue WARDEN_CHANCE;
    public static final ForgeConfigSpec.DoubleValue ELDER_GUARDIAN_CHANCE;
    public static final ForgeConfigSpec.DoubleValue HIGH_HEALTH_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_MOB_CHANCES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_ATTRIBUTE_INCREMENT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> IGNORED_ATTRIBUTES;

    public static Map<String, Double> cachedCustomMobChances = new HashMap<>();
    public static Map<String, Double> cachedCustomAttributeIncrement = new HashMap<>();
    public static Set<String> cachedIgnoredAttributes = new HashSet<>();

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("geral");

        DEBUG_MODE = builder
                .comment("Enables the mod's debug mode")
                .translation("config.playerstats.debug_mode")
                .define("debugMode", false);

        HIGH_HEALTH = builder
                .comment("Amount of health considered high")
                .translation("config.playerstats.high_health")
                .define("highHealthAmount", 50);

        WITHER_CHANCE = builder
                .comment("Chance to gain points when killing Wither")
                .translation("config.playerstats.wither_chance")
                .defineInRange("witherChance", 0.5, 0.0, 1.0);

        ENDER_DRAGON_CHANCE = builder
                .comment("Chance to gain points when killing Ender Dragon")
                .translation("config.playerstats.ender_dragon_chance")
                .defineInRange("enderDragonChance", 0.5, 0.0, 1.0);

        WARDEN_CHANCE = builder
                .comment("Chance to gain points when killing Warden")
                .translation("config.playerstats.warden_chance")
                .defineInRange("wardenChance", 0.3, 0.0, 1.0);

        ELDER_GUARDIAN_CHANCE = builder
                .comment("Chance to gain points when killing Elder Guardian")
                .translation("config.playerstats.elder_guardian_chance")
                .defineInRange("elderGuardianChance", 0.5, 0.0, 1.0);

        HIGH_HEALTH_CHANCE = builder
                .comment("Chance to gain points when killing mobs with high health")
                .translation("config.playerstats.high_health_chance")
                .defineInRange("highHealthChance", 0.05, 0.0, 1.0);

        CUSTOM_MOB_CHANCES = builder
                .comment("List of custom mob chances in the format id=chance (e.g. 'entity.minecraft.chicken=0.15', you can see the exact name by enabling debug mode, opening the developer console, and killing the desired mob.)")
                .translation("config.playerstats.custom_mob_chances")
                .defineListAllowEmpty(
                        "customMobChances",
                        List.of(""),
                        entry -> entry instanceof String && ((String) entry).contains("=")
                );

        CUSTOM_ATTRIBUTE_INCREMENT = builder
                .comment("List of custom attribute (from mods or vanilla) increment on upgrade id=increment (e.g. 'attribute.name.generic.max_health=2', you can see the exact name by enabling debug mode, opening the developer console, and opening the status window. O padrão é 0.1)")
                .translation("config.playerstats.custom_attribute_increment")
                .defineListAllowEmpty(
                        "customAttributeIncrement",
                        List.of(""),
                        entry -> entry instanceof String && ((String) entry).contains("=")
                );

        IGNORED_ATTRIBUTES = builder
                .comment("List of attribute names to ignore (e.g. 'attribute.name.generic.armor', 'attribute.name.generic.movement_speed')")
                .translation("config.playerstats.ignored_attributes")
                .defineListAllowEmpty(
                        "ignoredAttributes",
                        List.of("attribute.name.generic.armor",
                                "forge.name_tag_distance",
                                "forge.entity_gravity",
                                "forge.step_height",
                                "attribute.name.generic.armor_toughness"),
                        entry -> entry instanceof String
                );

        builder.pop();

        COMMON = builder.build();
    }

    public static Map<String, Double> getCustomMobChances() {
        return cachedCustomMobChances;
    }

    public static Map<String, Double> getCachedCustomAttributeIncrement() {
        return cachedCustomAttributeIncrement;
    }

    public static void reloadCustomMobChances() {
        cachedCustomMobChances.clear();
        cachedCustomAttributeIncrement.clear();
        cachedIgnoredAttributes.clear();
        for (String line : CUSTOM_MOB_CHANCES.get()) {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                String key = parts[0].trim();
                try {
                    double value = Double.parseDouble(parts[1].trim());
                    cachedCustomMobChances.put(key, value);
                } catch (NumberFormatException e) {
                    PlayerStats.LOGGER.warn("Valor inválido para mob customizado: '{}'", line);
                }
            }
        }
        for (String line : CUSTOM_ATTRIBUTE_INCREMENT.get()) {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                String key = parts[0].trim();
                try {
                    double value = Double.parseDouble(parts[1].trim());
                    cachedCustomAttributeIncrement.put(key, value);
                } catch (NumberFormatException e) {
                    PlayerStats.LOGGER.warn("Valor inválido para incremento de atributo customizado: '{}'", line);
                }
            }
        }

        cachedIgnoredAttributes.addAll(IGNORED_ATTRIBUTES.get().stream().map(String::trim).toList());
    }
}
