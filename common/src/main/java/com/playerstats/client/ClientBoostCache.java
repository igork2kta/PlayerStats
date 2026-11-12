package com.playerstats.client;

import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.HashMap;
import java.util.Map;

public class ClientBoostCache {
    public static final Map<Attribute, BoostInfo> activeBoosts = new HashMap<>();

    public static class BoostInfo {
        public double amount;
        public int secondsRemaining;
        public BoostInfo(double amount, int secondsRemaining) {
            this.amount = amount;
            this.secondsRemaining = secondsRemaining;
        }
    }
}
