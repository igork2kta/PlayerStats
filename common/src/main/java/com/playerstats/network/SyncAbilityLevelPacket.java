package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;

public class SyncAbilityLevelPacket {
    private final int entityId;
    private final String attributeId;
    private final int level;

    public SyncAbilityLevelPacket(int entityId, String attributeId, int level) {
        this.entityId = entityId;
        this.attributeId = attributeId;
        this.level = level;
    }

    public SyncAbilityLevelPacket(String attributeId, int level) {
        this(-1, attributeId, level);
    }

    public int getEntityId() { return entityId; }
    public String getAttributeId() { return attributeId; }
    public int getLevel() { return level; }

    public static void encode(SyncAbilityLevelPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeUtf(msg.attributeId);
        buf.writeInt(msg.level);
    }

    public static SyncAbilityLevelPacket decode(FriendlyByteBuf buf) {
        return new SyncAbilityLevelPacket(buf.readInt(), buf.readUtf(), buf.readInt());
    }
}
