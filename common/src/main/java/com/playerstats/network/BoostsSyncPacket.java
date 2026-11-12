package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class BoostsSyncPacket {

    private final Map<ResourceLocation, BoostData> boosts;

    public BoostsSyncPacket(Map<ResourceLocation, BoostData> boosts) {
        this.boosts = boosts;
    }

    public Map<ResourceLocation, BoostData> getBoosts() { return boosts; }

    public static class BoostData {
        public final double amount;
        public final int secondsRemaining;

        public BoostData(double amount, int secondsRemaining) {
            this.amount = amount;
            this.secondsRemaining = secondsRemaining;
        }
    }

    public static void encode(BoostsSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.boosts.size());
        for (Map.Entry<ResourceLocation, BoostData> entry : pkt.boosts.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeDouble(entry.getValue().amount);
            buf.writeInt(entry.getValue().secondsRemaining);
        }
    }

    public static BoostsSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<ResourceLocation, BoostData> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation attrId = buf.readResourceLocation();
            double amount = buf.readDouble();
            int seconds = buf.readInt();
            map.put(attrId, new BoostData(amount, seconds));
        }
        return new BoostsSyncPacket(map);
    }
}
