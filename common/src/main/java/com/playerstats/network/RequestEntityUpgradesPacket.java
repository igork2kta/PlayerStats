package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;

public class RequestEntityUpgradesPacket {
    private final int entityId;

    public RequestEntityUpgradesPacket(int entityId) {
        this.entityId = entityId;
    }

    public int getEntityId() { return entityId; }

    public static void encode(RequestEntityUpgradesPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static RequestEntityUpgradesPacket decode(FriendlyByteBuf buf) {
        return new RequestEntityUpgradesPacket(buf.readInt());
    }
}
