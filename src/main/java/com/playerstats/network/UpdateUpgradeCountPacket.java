package com.playerstats.network;

import com.playerstats.client.ClientAttributeCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateUpgradeCountPacket {
    private final int upgradeCount;

    public UpdateUpgradeCountPacket(int upgradeCount) {
        this.upgradeCount = upgradeCount;
    }

    public static void encode(UpdateUpgradeCountPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.upgradeCount);
    }

    public static UpdateUpgradeCountPacket decode(FriendlyByteBuf buf) {
        return new UpdateUpgradeCountPacket(buf.readInt());
    }

    public static void handle(UpdateUpgradeCountPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientAttributeCache.setUpgradeCount(msg.upgradeCount);
        });
        ctx.get().setPacketHandled(true);
    }
}

