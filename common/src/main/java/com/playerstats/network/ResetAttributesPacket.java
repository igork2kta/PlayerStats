package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;

public class ResetAttributesPacket {
    private final int entityId;

    public ResetAttributesPacket(int entityId) {
        this.entityId = entityId;
    }

    public int getEntityId() { return entityId; }

    public static void encode(ResetAttributesPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static ResetAttributesPacket decode(FriendlyByteBuf buf) {
        return new ResetAttributesPacket(buf.readInt());
    }
}
