package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;

public class UpdateUpgradeCountPacket {
    private final int entityId;
    private final int upgradeCount;

    public UpdateUpgradeCountPacket(int entityId, int upgradeCount) {
        this.entityId = entityId;
        this.upgradeCount = upgradeCount;
    }

    public int getEntityId() { return entityId; }
    public int getUpgradeCount() { return upgradeCount; }

    public static void encode(UpdateUpgradeCountPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeInt(msg.upgradeCount);
    }

    public static UpdateUpgradeCountPacket decode(FriendlyByteBuf buf) {
        return new UpdateUpgradeCountPacket(buf.readInt(), buf.readInt());
    }
}
