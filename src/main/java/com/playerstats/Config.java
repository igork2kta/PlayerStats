package com.playerstats;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = PlayerStats.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final ForgeConfigSpec COMMON;
    public static final ForgeConfigSpec SERVER;

    // COMMON config
    public static final ForgeConfigSpec.BooleanValue DEBUG_MODE;

    // SERVER config
    public static final ForgeConfigSpec.BooleanValue RESET_ON_DEATH;
    public static final ForgeConfigSpec.ConfigValue<Integer> HIGH_HEALTH;
    public static final ForgeConfigSpec.DoubleValue WITHER_CHANCE;
    public static final ForgeConfigSpec.DoubleValue ENDER_DRAGON_CHANCE;
    public static final ForgeConfigSpec.DoubleValue WARDEN_CHANCE;
    public static final ForgeConfigSpec.DoubleValue ELDER_GUARDIAN_CHANCE;
    public static final ForgeConfigSpec.DoubleValue HIGH_HEALTH_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_MOB_CHANCES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_ATTRIBUTE_INCREMENT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> IGNORED_ATTRIBUTES;
    public static final ForgeConfigSpec.BooleanValue CONSUME_EXPERIENCE;
    public static final ForgeConfigSpec.IntValue EXPERIENCE_COST_INCREMENT;

    public static final Map<String, Double> cachedCustomMobChances = new HashMap<>();
    public static final Map<String, Double> cachedCustomAttributeIncrement = new HashMap<>();
    public static final Set<String> cachedIgnoredAttributes = new HashSet<>();

    static {
        // COMMON
        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
        commonBuilder.push("geral");

        DEBUG_MODE = commonBuilder
                .comment("Activate debug mode")
                .define("debugMode", false);

        commonBuilder.pop();
        COMMON = commonBuilder.build();

        // SERVER
        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();
        serverBuilder.push("geral");

        RESET_ON_DEATH = serverBuilder
                .comment("Reset attributes on death")
                .define("resetOnDeath", false);

        HIGH_HEALTH = serverBuilder
                .comment("Amount of health considered high")
                .define("highHealthAmount", 50);

        WITHER_CHANCE = serverBuilder
                .comment("Chance to gain points when killing Wither")
                .defineInRange("witherChance", 0.5, 0.0, 1.0);

        ENDER_DRAGON_CHANCE = serverBuilder
                .comment("Chance to gain points when killing Ender Dragon")
                .defineInRange("enderDragonChance", 0.5, 0.0, 1.0);

        WARDEN_CHANCE = serverBuilder
                .comment("Chance to gain points when killing Warden")
                .defineInRange("wardenChance", 0.3, 0.0, 1.0);

        ELDER_GUARDIAN_CHANCE = serverBuilder
                .comment("Chance to gain points when killing Elder Guardian")
                .defineInRange("elderGuardianChance", 0.5, 0.0, 1.0);

        HIGH_HEALTH_CHANCE = serverBuilder
                .comment("Chance to gain points when killing mobs with high health")
                .defineInRange("highHealthChance", 0.05, 0.0, 1.0);

        CUSTOM_MOB_CHANCES = serverBuilder
                .comment("Custom mob chances (id=chance)")
                .defineListAllowEmpty("customMobChances", List.of(), o -> o instanceof String && ((String) o).contains("="));

        CUSTOM_ATTRIBUTE_INCREMENT = serverBuilder
                .comment("Custom attribute increment (id=increment)")
                .defineListAllowEmpty("customAttributeIncrement", List.of(), o -> o instanceof String && ((String) o).contains("="));

        IGNORED_ATTRIBUTES = serverBuilder
                .comment("List of attribute names to ignore")
                .defineListAllowEmpty("ignoredAttributes", List.of(
                        "attribute.name.generic.armor",
                        "forge.name_tag_distance",
                        "forge.entity_gravity",
                        "forge.step_height",
                        "attribute.name.generic.armor_toughness"
                ), o -> o instanceof String);

        EXPERIENCE_COST_INCREMENT = serverBuilder
                .comment("The cost of experience that will be incremented for the next upgrade (COMING SOON)")
                .defineInRange("experienceCostIncrement", 5, 1, 999);

        CONSUME_EXPERIENCE = serverBuilder
                .comment("Consume experience on upgrade? (COMING SOON)")
                .define("consumeExperience", true);

        serverBuilder.pop();
        SERVER = serverBuilder.build();
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
                try {
                    cachedCustomMobChances.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
                } catch (NumberFormatException e) {
                    PlayerStats.LOGGER.warn("Valor inválido: {}", line);
                }
            }
        }

        for (String line : CUSTOM_ATTRIBUTE_INCREMENT.get()) {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                try {
                    cachedCustomAttributeIncrement.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
                } catch (NumberFormatException e) {
                    PlayerStats.LOGGER.warn("Valor inválido de atributo: {}", line);
                }
            }
        }

        cachedIgnoredAttributes.addAll(IGNORED_ATTRIBUTES.get().stream().map(String::trim).toList());
    }
}
