package com.playerstats;

import com.playerstats.util.AttributeUtils;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

public class Config {
    public static final ModConfigSpec COMMON;
    public static final ModConfigSpec SERVER;


    // COMMON config
    public static final ModConfigSpec.BooleanValue DEBUG_MODE;

    // SERVER config - Geral
    public static final ModConfigSpec.BooleanValue RESET_ON_DEATH;
    public static final ModConfigSpec.ConfigValue<Integer> HIGH_HEALTH;

    // SERVER config - Chances de mobs
    public static final ModConfigSpec.DoubleValue WITHER_CHANCE;
    public static final ModConfigSpec.DoubleValue ENDER_DRAGON_CHANCE;
    public static final ModConfigSpec.DoubleValue WARDEN_CHANCE;
    public static final ModConfigSpec.DoubleValue ELDER_GUARDIAN_CHANCE;
    public static final ModConfigSpec.DoubleValue HIGH_HEALTH_CHANCE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CUSTOM_MOB_CHANCES;

    // SERVER config - Incrementos e atributos
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CUSTOM_ATTRIBUTE_INCREMENT;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> IGNORED_ATTRIBUTES;

    // SERVER config - Experiência

    public static final ModConfigSpec.BooleanValue CONSUME_XP;
    public static final ModConfigSpec.IntValue XP_COST_INCREMENT;
    public static final ModConfigSpec.IntValue REQUIRED_XP_FOR_RESET;
    public static final ModConfigSpec.IntValue REQUIRED_XP_FOR_ABILITY;


    // SERVER config - Boost de atributos
    public static final ModConfigSpec.IntValue BOOST_AMOUNT_MIN_MULTIPLIER;
    public static final ModConfigSpec.IntValue BOOST_AMOUNT_MAX_MULTIPLIER;
    public static final ModConfigSpec.IntValue BOOST_DURATION_MIN_MINUTES;
    public static final ModConfigSpec.IntValue BOOST_DURATION_MAX_MINUTES;

    // Cache
    public static final Map<String, Double> cachedCustomMobChances = new HashMap<>();
    public static final Map<String, Double> cachedCustomAttributeIncrement = new HashMap<>();
    public static final Set<String> cachedIgnoredAttributes = new HashSet<>();

    private static final ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();


    static {
        // ===================== COMMON =====================

        commonBuilder
                .comment("General configuration options")
                .translation("playerstats.configuration.geral")
                .push("geral");

        DEBUG_MODE = commonBuilder
                .comment("Activate debug mode")
                .translation("playerstats.configuration.debug_mode")
                .define("debugMode", false);

        commonBuilder.pop();
        COMMON = commonBuilder.build();

        // ===================== SERVER =====================
        ModConfigSpec.Builder serverBuilder = new ModConfigSpec.Builder();

        // ----- Geral -----
        serverBuilder
                .comment("General configuration options")
                .translation("playerstats.configuration.geral")
                .push("geral");

        RESET_ON_DEATH = serverBuilder
                .comment("Reset attributes on death")
                .translation("playerstats.configuration.reset_on_death")
                .define("resetOnDeath", false);

        HIGH_HEALTH = serverBuilder
                .comment("Amount of health considered high")
                .translation("playerstats.configuration.high_health_amount")
                .define("highHealthAmount", 50);

        serverBuilder.pop();

        // ----- Chances de Mobs -----
        serverBuilder
                .comment("Chances of acquiring points upon defeating bosses and high-health mobs")
                .translation("playerstats.configuration.mob_chances")
                .push("mob_chances");

        WITHER_CHANCE = serverBuilder
                .comment("Chance to gain points when killing Wither")
                .translation("playerstats.configuration.wither_chance")
                .defineInRange("witherChance", 0.5, 0.0, 1.0);

        ENDER_DRAGON_CHANCE = serverBuilder
                .comment("Chance to gain points when killing Ender Dragon")
                .translation("playerstats.configuration.ender_dragon_chance")
                .defineInRange("enderDragonChance", 0.5, 0.0, 1.0);

        WARDEN_CHANCE = serverBuilder
                .comment("Chance to gain points when killing Warden")
                .translation("playerstats.configuration.warden_chance")
                .defineInRange("wardenChance", 0.3, 0.0, 1.0);

        ELDER_GUARDIAN_CHANCE = serverBuilder
                .comment("Chance to gain points when killing Elder Guardian")
                .translation("playerstats.configuration.elder_guardian_chance")
                .defineInRange("elderGuardianChance", 0.5, 0.0, 1.0);

        HIGH_HEALTH_CHANCE = serverBuilder
                .comment("Chance to gain points when killing mobs with high health")
                .translation("playerstats.configuration.high_health_chance")
                .defineInRange("highHealthChance", 0.05, 0.0, 1.0);

        CUSTOM_MOB_CHANCES = serverBuilder
                .comment("Custom mob chances (id=chance)")
                .translation("playerstats.configuration.custom_mob_chances")
                .defineListAllowEmpty("customMobChances", List.of(), () -> "entity.minecraft.chicken=0.1",
                        o -> o instanceof String && ((String) o).contains("="));

        serverBuilder.pop();

        // ----- Incrementos e Atributos -----
        serverBuilder
                .comment("Customize attribute increments and ignored attributes")
                .translation("playerstats.configuration.attribute_settings")
                .push("attribute_settings");

        CUSTOM_ATTRIBUTE_INCREMENT = serverBuilder
                .comment("Custom attribute increment (id=increment)")
                .translation("playerstats.configuration.custom_attribute_increment")
                .defineListAllowEmpty("customAttributeIncrement", List.of(), () -> "attribute.name.generic.max_health=2.0",
                        o -> o instanceof String && ((String) o).contains("="));

        IGNORED_ATTRIBUTES = serverBuilder
                .comment("List of attribute names to ignore")
                .translation("playerstats.configuration.ignored_attributes")
                .defineListAllowEmpty("ignoredAttributes", List.of(), () -> "attribute.name.generic.armor",
                        o -> o instanceof String);

        serverBuilder.pop();

        // ----- Experiência -----
        serverBuilder
                .comment("Experience costs for upgrading attributes, abilities, and resetting")
                .translation("playerstats.configuration.experience")
                .push("experience");

        XP_COST_INCREMENT = serverBuilder
                .comment("The cost of experience that will be incremented for the next upgrade")
                .translation("playerstats.configuration.experience_cost_increment")
                .defineInRange("experienceCostIncrement", 5, 1, 999);

        REQUIRED_XP_FOR_RESET = serverBuilder
                .comment("The cost of experience that will be required for reset")
                .translation("playerstats.configuration.required_xp_for_reset")
                .defineInRange("requiredXpForReset", 50, 1, 999);

        CONSUME_XP = serverBuilder
                .comment("Consume experience on upgrade?")
                .translation("playerstats.configuration.consume_experience")
                .define("consumeExperience", true);

        REQUIRED_XP_FOR_ABILITY = serverBuilder
                .comment("The cost of experience that will be required to by an ability")
                .translation("playerstats.configuration.required_xp_for_ability")
                .defineInRange("requiredXpForAbility", 30, 1, 999);

        serverBuilder.pop();

        // ----- Boost de Atributos -----
        serverBuilder
                .comment("Configure multipliers and duration for attribute boost scrolls")
                .translation("playerstats.configuration.attribute_boost")
                .push("attribute_boost");

        BOOST_AMOUNT_MIN_MULTIPLIER = serverBuilder
                .comment("Minimum multiplier for attribute boost (1.0 = base increment)")
                .translation("playerstats.configuration.boost_amount_min_multiplier")
                .defineInRange("boostAmountMinMultiplier", 1, 1, 10);

        BOOST_AMOUNT_MAX_MULTIPLIER = serverBuilder
                .comment("Maximum multiplier for attribute boost (3.0 = triple the base increment)")
                .translation("playerstats.configuration.boost_amount_max_multiplier")
                .defineInRange("boostAmountMaxMultiplier", 3, 1, 10);

        BOOST_DURATION_MIN_MINUTES = serverBuilder
                .comment("Minimum duration of boost in minutes")
                .translation("playerstats.configuration.boost_duration_min_minutes")
                .defineInRange("boostDurationMinMinutes", 10, 1, 240);

        BOOST_DURATION_MAX_MINUTES = serverBuilder
                .comment("Maximum duration of boost in minutes")
                .translation("playerstats.configuration.boost_duration_max_minutes")
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

        cachedIgnoredAttributes.addAll(
                AttributeUtils.IGNORED_ATTRIBUTES
        );
        cachedIgnoredAttributes.addAll(
                IGNORED_ATTRIBUTES.get().stream().map(String::trim).toList()
        );
    }
}
