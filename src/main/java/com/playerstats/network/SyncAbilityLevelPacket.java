package com.playerstats.network;

import com.playerstats.client.ClientAttributeCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncAbilityLevelPacket {
    private final String attributeId;
    private final int level;

    public SyncAbilityLevelPacket(String attributeId, int level) {
        this.attributeId = attributeId;
        this.level = level;
    }

    public static void encode(SyncAbilityLevelPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.attributeId);
        buf.writeInt(msg.level);
    }

    public static SyncAbilityLevelPacket decode(FriendlyByteBuf buf) {
        return new SyncAbilityLevelPacket(buf.readUtf(), buf.readInt());
    }

    public static void handle(SyncAbilityLevelPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientAttributeCache.setAbilityLevel(msg.attributeId, msg.level);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}

