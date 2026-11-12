package com.playerstats;

import com.playerstats.util.AttributeUtils;

import java.util.*;

public class Config {

    public interface Value<T> {
        T get();
    }

    public interface IConfigValues {
        boolean isDebugMode();
        boolean isResetOnDeath();
        int getHighHealth();
        double getWitherChance();
        double getEnderDragonChance();
        double getWardenChance();
        double getElderGuardianChance();
        double getHighHealthChance();
        int getExperienceCostIncrement();
        int getRequiredXpForReset();
        boolean isConsumeExperience();
        int getRequiredXpForAbility();
        int getBoostAmountMinMultiplier();
        int getBoostAmountMaxMultiplier();
        int getBoostDurationMinMinutes();
        int getBoostDurationMaxMinutes();
        Map<String, Double> getCustomMobChances();
        Map<String, Double> getCustomAttributeIncrement();
        Set<String> getIgnoredAttributes();
    }

    public static class DefaultConfigValues implements IConfigValues {
        private final Map<String, Double> customMobChances = new HashMap<>();
        private final Map<String, Double> customAttributeIncrement = new HashMap<>();
        private final Set<String> ignoredAttributes = new HashSet<>(AttributeUtils.IGNORED_ATTRIBUTES);

        @Override public boolean isDebugMode() { return false; }
        @Override public boolean isResetOnDeath() { return false; }
        @Override public int getHighHealth() { return 50; }
        @Override public double getWitherChance() { return 0.5; }
        @Override public double getEnderDragonChance() { return 0.5; }
        @Override public double getWardenChance() { return 0.3; }
        @Override public double getElderGuardianChance() { return 0.5; }
        @Override public double getHighHealthChance() { return 0.05; }
        @Override public int getExperienceCostIncrement() { return 5; }
        @Override public int getRequiredXpForReset() { return 50; }
        @Override public boolean isConsumeExperience() { return true; }
        @Override public int getRequiredXpForAbility() { return 30; }
        @Override public int getBoostAmountMinMultiplier() { return 1; }
        @Override public int getBoostAmountMaxMultiplier() { return 3; }
        @Override public int getBoostDurationMinMinutes() { return 10; }
        @Override public int getBoostDurationMaxMinutes() { return 30; }
        @Override public Map<String, Double> getCustomMobChances() { return customMobChances; }
        @Override public Map<String, Double> getCustomAttributeIncrement() { return customAttributeIncrement; }
        @Override public Set<String> getIgnoredAttributes() { return ignoredAttributes; }
    }

    private static IConfigValues INSTANCE = new DefaultConfigValues();

    public static void setInstance(IConfigValues instance) {
        INSTANCE = instance;
    }

    public static final Value<Boolean> DEBUG_MODE = () -> INSTANCE.isDebugMode();
    public static final Value<Boolean> RESET_ON_DEATH = () -> INSTANCE.isResetOnDeath();
    public static final Value<Integer> HIGH_HEALTH = () -> INSTANCE.getHighHealth();
    public static final Value<Double> WITHER_CHANCE = () -> INSTANCE.getWitherChance();
    public static final Value<Double> ENDER_DRAGON_CHANCE = () -> INSTANCE.getEnderDragonChance();
    public static final Value<Double> WARDEN_CHANCE = () -> INSTANCE.getWardenChance();
    public static final Value<Double> ELDER_GUARDIAN_CHANCE = () -> INSTANCE.getElderGuardianChance();
    public static final Value<Double> HIGH_HEALTH_CHANCE = () -> INSTANCE.getHighHealthChance();
    public static final Value<Integer> XP_COST_INCREMENT = () -> INSTANCE.getExperienceCostIncrement();
    public static final Value<Integer> REQUIRED_XP_FOR_RESET = () -> INSTANCE.getRequiredXpForReset();
    public static final Value<Boolean> CONSUME_XP = () -> INSTANCE.isConsumeExperience();
    public static final Value<Integer> REQUIRED_XP_FOR_ABILITY = () -> INSTANCE.getRequiredXpForAbility();
    public static final Value<Integer> BOOST_AMOUNT_MIN_MULTIPLIER = () -> INSTANCE.getBoostAmountMinMultiplier();
    public static final Value<Integer> BOOST_AMOUNT_MAX_MULTIPLIER = () -> INSTANCE.getBoostAmountMaxMultiplier();
    public static final Value<Integer> BOOST_DURATION_MIN_MINUTES = () -> INSTANCE.getBoostDurationMinMinutes();
    public static final Value<Integer> BOOST_DURATION_MAX_MINUTES = () -> INSTANCE.getBoostDurationMaxMinutes();

    public static Map<String, Double> getCustomMobChances() {
        return INSTANCE.getCustomMobChances();
    }

    public static Map<String, Double> getCachedCustomAttributeIncrement() {
        return INSTANCE.getCustomAttributeIncrement();
    }

    public static Set<String> getCachedIgnoredAttributes() {
        return INSTANCE.getIgnoredAttributes();
    }

    public static void reloadCustomMobChances() {
        // Recarrega caches se necessário
    }
}
