package com.playerstats.forge.config;

import com.playerstats.Config;
import com.playerstats.util.AttributeUtils;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;

public class ForgeConfig implements Config.IConfigValues {

    public static final ForgeConfigSpec COMMON;
    public static final ForgeConfigSpec SERVER;

    public static final ForgeConfigSpec.BooleanValue DEBUG_MODE;
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

    public static final ForgeConfigSpec.BooleanValue CONSUME_XP;
    public static final ForgeConfigSpec.IntValue XP_COST_INCREMENT;
    public static final ForgeConfigSpec.IntValue REQUIRED_XP_FOR_RESET;
    public static final ForgeConfigSpec.IntValue REQUIRED_XP_FOR_ABILITY;

    public static final ForgeConfigSpec.IntValue BOOST_AMOUNT_MIN_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue BOOST_AMOUNT_MAX_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue BOOST_DURATION_MIN_MINUTES;
    public static final ForgeConfigSpec.IntValue BOOST_DURATION_MAX_MINUTES;

    private static final Map<String, Double> cachedCustomMobChances = new HashMap<>();
    private static final Map<String, Double> cachedCustomAttributeIncrement = new HashMap<>();
    private static final Set<String> cachedIgnoredAttributes = new HashSet<>();

    static {
        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
        commonBuilder.comment("General configuration options").translation("playerstats.configuration.geral").push("geral");
        DEBUG_MODE = commonBuilder.comment("Activate debug mode").translation("playerstats.configuration.debug_mode").define("debugMode", false);
        commonBuilder.pop();
        COMMON = commonBuilder.build();

        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();
        serverBuilder.comment("General configuration options").translation("playerstats.configuration.geral").push("geral");
        RESET_ON_DEATH = serverBuilder.comment("Reset attributes on death").translation("playerstats.configuration.reset_on_death").define("resetOnDeath", false);
        HIGH_HEALTH = serverBuilder.comment("Amount of health considered high").translation("playerstats.configuration.high_health_amount").define("highHealthAmount", 50);
        serverBuilder.pop();

        serverBuilder.comment("Chances of acquiring points upon defeating bosses and high-health mobs").translation("playerstats.configuration.mob_chances").push("mob_chances");
        WITHER_CHANCE = serverBuilder.comment("Chance to gain points when killing Wither").translation("playerstats.configuration.wither_chance").defineInRange("witherChance", 0.5, 0.0, 1.0);
        ENDER_DRAGON_CHANCE = serverBuilder.comment("Chance to gain points when killing Ender Dragon").translation("playerstats.configuration.ender_dragon_chance").defineInRange("enderDragonChance", 0.5, 0.0, 1.0);
        WARDEN_CHANCE = serverBuilder.comment("Chance to gain points when killing Warden").translation("playerstats.configuration.warden_chance").defineInRange("wardenChance", 0.3, 0.0, 1.0);
        ELDER_GUARDIAN_CHANCE = serverBuilder.comment("Chance to gain points when killing Elder Guardian").translation("playerstats.configuration.elder_guardian_chance").defineInRange("elderGuardianChance", 0.5, 0.0, 1.0);
        HIGH_HEALTH_CHANCE = serverBuilder.comment("Chance to gain points when killing high-health mobs").translation("playerstats.configuration.high_health_chance").defineInRange("highHealthChance", 0.05, 0.0, 1.0);
        CUSTOM_MOB_CHANCES = serverBuilder.comment("Custom chances for specific mobs in the format: modid:entity_name=chance (e.g. \"minecraft:zombie=0.01\")")
                .translation("playerstats.configuration.custom_mob_chances")
                .defineList("customMobChances", List.of(), o -> o instanceof String s && s.contains("="));
        serverBuilder.pop();

        serverBuilder.comment("Experience settings").translation("playerstats.configuration.experience").push("experience");
        CONSUME_XP = serverBuilder.comment("Consume experience when upgrading attributes").translation("playerstats.configuration.consume_xp").define("consumeExperience", true);
        XP_COST_INCREMENT = serverBuilder.comment("Amount of XP cost increment per upgrade").translation("playerstats.configuration.xp_cost_increment").defineInRange("xpCostIncrement", 5, 1, 100);
        REQUIRED_XP_FOR_RESET = serverBuilder.comment("Amount of XP required to reset attributes").translation("playerstats.configuration.required_xp_for_reset").defineInRange("requiredXpForReset", 50, 0, 1000);
        REQUIRED_XP_FOR_ABILITY = serverBuilder.comment("Amount of XP required to unlock an ability").translation("playerstats.configuration.required_xp_for_ability").defineInRange("requiredXpForAbility", 30, 0, 1000);
        serverBuilder.pop();

        serverBuilder.comment("Attribute Boost Scroll settings").translation("playerstats.configuration.attribute_boost").push("attribute_boost");
        BOOST_AMOUNT_MIN_MULTIPLIER = serverBuilder.comment("Minimum multiplier for boost amount").translation("playerstats.configuration.boost_amount_min_multiplier").defineInRange("boostAmountMinMultiplier", 1, 1, 100);
        BOOST_AMOUNT_MAX_MULTIPLIER = serverBuilder.comment("Maximum multiplier for boost amount").translation("playerstats.configuration.boost_amount_max_multiplier").defineInRange("boostAmountMaxMultiplier", 3, 1, 100);
        BOOST_DURATION_MIN_MINUTES = serverBuilder.comment("Minimum duration for boost in minutes").translation("playerstats.configuration.boost_duration_min_minutes").defineInRange("boostDurationMinMinutes", 10, 1, 1440);
        BOOST_DURATION_MAX_MINUTES = serverBuilder.comment("Maximum duration for boost in minutes").translation("playerstats.configuration.boost_duration_max_minutes").defineInRange("boostDurationMaxMinutes", 30, 1, 1440);
        serverBuilder.pop();

        serverBuilder.comment("Attribute increments and exclusions").translation("playerstats.configuration.attributes").push("attributes");
        CUSTOM_ATTRIBUTE_INCREMENT = serverBuilder.comment("Custom increment values per upgrade in format: attribute_description_id=increment")
                .translation("playerstats.configuration.custom_attribute_increment")
                .defineList("customAttributeIncrement", List.of(), o -> o instanceof String s && s.contains("="));
        IGNORED_ATTRIBUTES = serverBuilder.comment("Attributes that should not appear in the stats screen")
                .translation("playerstats.configuration.ignored_attributes")
                .defineList("ignoredAttributes", new ArrayList<>(AttributeUtils.IGNORED_ATTRIBUTES), o -> o instanceof String);
        serverBuilder.pop();

        SERVER = serverBuilder.build();
    }

    public static void reloadCache() {
        cachedCustomMobChances.clear();
        for (String entry : CUSTOM_MOB_CHANCES.get()) {
            String[] split = entry.split("=");
            if (split.length == 2) {
                try {
                    cachedCustomMobChances.put(split[0].trim(), Double.parseDouble(split[1].trim()));
                } catch (NumberFormatException ignored) {}
            }
        }

        cachedCustomAttributeIncrement.clear();
        for (String entry : CUSTOM_ATTRIBUTE_INCREMENT.get()) {
            String[] split = entry.split("=");
            if (split.length == 2) {
                try {
                    cachedCustomAttributeIncrement.put(split[0].trim(), Double.parseDouble(split[1].trim()));
                } catch (NumberFormatException ignored) {}
            }
        }

        cachedIgnoredAttributes.clear();
        cachedIgnoredAttributes.addAll(IGNORED_ATTRIBUTES.get());
    }

    @Override public boolean isDebugMode() { return DEBUG_MODE.get(); }
    @Override public boolean isResetOnDeath() { return RESET_ON_DEATH.get(); }
    @Override public int getHighHealth() { return HIGH_HEALTH.get(); }
    @Override public double getWitherChance() { return WITHER_CHANCE.get(); }
    @Override public double getEnderDragonChance() { return ENDER_DRAGON_CHANCE.get(); }
    @Override public double getWardenChance() { return WARDEN_CHANCE.get(); }
    @Override public double getElderGuardianChance() { return ELDER_GUARDIAN_CHANCE.get(); }
    @Override public double getHighHealthChance() { return HIGH_HEALTH_CHANCE.get(); }
    @Override public int getExperienceCostIncrement() { return XP_COST_INCREMENT.get(); }
    @Override public int getRequiredXpForReset() { return REQUIRED_XP_FOR_RESET.get(); }
    @Override public boolean isConsumeExperience() { return CONSUME_XP.get(); }
    @Override public int getRequiredXpForAbility() { return REQUIRED_XP_FOR_ABILITY.get(); }
    @Override public int getBoostAmountMinMultiplier() { return BOOST_AMOUNT_MIN_MULTIPLIER.get(); }
    @Override public int getBoostAmountMaxMultiplier() { return BOOST_AMOUNT_MAX_MULTIPLIER.get(); }
    @Override public int getBoostDurationMinMinutes() { return BOOST_DURATION_MIN_MINUTES.get(); }
    @Override public int getBoostDurationMaxMinutes() { return BOOST_DURATION_MAX_MINUTES.get(); }
    @Override public Map<String, Double> getCustomMobChances() { return cachedCustomMobChances; }
    @Override public Map<String, Double> getCustomAttributeIncrement() { return cachedCustomAttributeIncrement; }
    @Override public Set<String> getIgnoredAttributes() { return cachedIgnoredAttributes; }
}
