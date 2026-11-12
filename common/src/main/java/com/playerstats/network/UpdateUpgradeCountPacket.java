package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;

public class UpdateUpgradeCountPacket {
    private final int upgradeCount;

    public UpdateUpgradeCountPacket(int upgradeCount) {
        this.upgradeCount = upgradeCount;
    }

    public int getUpgradeCount() { return upgradeCount; }

    public static void encode(UpdateUpgradeCountPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.upgradeCount);
    }

    public static UpdateUpgradeCountPacket decode(FriendlyByteBuf buf) {
        return new UpdateUpgradeCountPacket(buf.readInt());
    }
}
