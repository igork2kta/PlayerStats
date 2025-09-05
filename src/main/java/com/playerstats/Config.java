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

    // SERVER config - Geral
    public static final ForgeConfigSpec.BooleanValue RESET_ON_DEATH;
    public static final ForgeConfigSpec.ConfigValue<Integer> HIGH_HEALTH;

    // SERVER config - Chances de mobs
    public static final ForgeConfigSpec.DoubleValue WITHER_CHANCE;
    public static final ForgeConfigSpec.DoubleValue ENDER_DRAGON_CHANCE;
    public static final ForgeConfigSpec.DoubleValue WARDEN_CHANCE;
    public static final ForgeConfigSpec.DoubleValue ELDER_GUARDIAN_CHANCE;
    public static final ForgeConfigSpec.DoubleValue HIGH_HEALTH_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_MOB_CHANCES;

    // SERVER config - Incrementos e atributos
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_ATTRIBUTE_INCREMENT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> IGNORED_ATTRIBUTES;

    // SERVER config - Experiência
    public static final ForgeConfigSpec.BooleanValue CONSUME_XP;
    public static final ForgeConfigSpec.IntValue XP_COST_INCREMENT;
    public static final ForgeConfigSpec.IntValue REQUIRED_XP_FOR_RESET;
    public static final ForgeConfigSpec.IntValue REQUIRED_XP_FOR_ABILITY;

    // SERVER config - Boost de atributos
    public static final ForgeConfigSpec.IntValue BOOST_AMOUNT_MIN_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue BOOST_AMOUNT_MAX_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue BOOST_DURATION_MIN_MINUTES;
    public static final ForgeConfigSpec.IntValue BOOST_DURATION_MAX_MINUTES;


    //Cache
    public static final Map<String, Double> cachedCustomMobChances = new HashMap<>();
    public static final Map<String, Double> cachedCustomAttributeIncrement = new HashMap<>();
    public static final Set<String> cachedIgnoredAttributes = new HashSet<>();


    static {
        // ===================== COMMON =====================
        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
        commonBuilder.push("geral");

        DEBUG_MODE = commonBuilder
                .comment("Activate debug mode")
                .define("debugMode", false);

        commonBuilder.pop();
        COMMON = commonBuilder.build();

        // ===================== SERVER =====================
        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();

        // ----- Geral -----
        serverBuilder.push("geral");

        RESET_ON_DEATH = serverBuilder
                .comment("Reset attributes on death")
                .define("resetOnDeath", false);

        HIGH_HEALTH = serverBuilder
                .comment("Amount of health considered high")
                .define("highHealthAmount", 50);

        serverBuilder.pop();

        // ----- Chances de Mobs -----
        serverBuilder.push("mob_chances");

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

        serverBuilder.pop();

        // ----- Incrementos e Atributos -----
        serverBuilder.push("attribute_settings");

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

        serverBuilder.pop();

        // ----- Experiência -----
        serverBuilder.push("experience");

        XP_COST_INCREMENT = serverBuilder
                .comment("The cost of experience that will be incremented for the next upgrade")
                .defineInRange("experienceCostIncrement", 5, 1, 999);

        REQUIRED_XP_FOR_RESET = serverBuilder
                .comment("The cost of experience that will be required for reset")
                .defineInRange("requiredXpForReset", 50, 1, 999);

        CONSUME_XP = serverBuilder
                .comment("Consume experience on upgrade?")
                .define("consumeExperience", true);

        REQUIRED_XP_FOR_ABILITY = serverBuilder
                .comment("The cost of experience that will be required to by an ability")
                .defineInRange("requiredXpForAbility", 30, 1, 999);

        serverBuilder.pop();

        // ----- Boost de Atributos -----
        serverBuilder.push("attribute_boost");

        BOOST_AMOUNT_MIN_MULTIPLIER = serverBuilder
                .comment("Minimum multiplier for attribute boost (1.0 = base increment)")
                .defineInRange("boostAmountMinMultiplier", 1, 1, 10);

        BOOST_AMOUNT_MAX_MULTIPLIER = serverBuilder
                .comment("Maximum multiplier for attribute boost (3.0 = triple the base increment)")
                .defineInRange("boostAmountMaxMultiplier", 3, 1, 10);

        BOOST_DURATION_MIN_MINUTES = serverBuilder
                .comment("Minimum duration of boost in minutes")
                .defineInRange("boostDurationMinMinutes", 10, 1, 240);

        BOOST_DURATION_MAX_MINUTES = serverBuilder
                .comment("Maximum duration of boost in minutes")
                .defineInRange("boostDurationMaxMinutes", 30, 1, 240);

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
