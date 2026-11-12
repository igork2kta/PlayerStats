package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;

public class SyncAbilityLevelPacket {
    private final String attributeId;
    private final int level;

    public SyncAbilityLevelPacket(String attributeId, int level) {
        this.attributeId = attributeId;
        this.level = level;
    }

    public String getAttributeId() { return attributeId; }
    public int getLevel() { return level; }

    public static void encode(SyncAbilityLevelPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.attributeId);
        buf.writeInt(msg.level);
    }

    public static SyncAbilityLevelPacket decode(FriendlyByteBuf buf) {
        return new SyncAbilityLevelPacket(buf.readUtf(), buf.readInt());
    }
}
