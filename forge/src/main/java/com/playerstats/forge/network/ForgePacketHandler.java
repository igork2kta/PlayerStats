package com.playerstats.forge.network;

import com.playerstats.PlayerStats;
import com.playerstats.network.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ForgePacketHandler {
    private static final String PROTOCOL_VERSION = "1.0";

    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PlayerStats.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;

        INSTANCE.registerMessage(id++,
                UpdatePointsPacket.class,
                UpdatePointsPacket::encode,
                UpdatePointsPacket::decode,
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> PacketHandler.handleUpdatePoints(msg));
                    ctx.get().setPacketHandled(true);
                }
        );

        INSTANCE.registerMessage(id++,
                ResetAttributesPacket.class,
                ResetAttributesPacket::encode,
                ResetAttributesPacket::decode,
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> PacketHandler.handleResetAttributes(msg, ctx.get().getSender()));
                    ctx.get().setPacketHandled(true);
                }
        );

        INSTANCE.registerMessage(id++,
                UpdateUpgradeCountPacket.class,
                UpdateUpgradeCountPacket::encode,
                UpdateUpgradeCountPacket::decode,
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> PacketHandler.handleUpdateUpgradeCount(msg));
                    ctx.get().setPacketHandled(true);
                }
        );

        INSTANCE.registerMessage(id++,
                BoostsSyncPacket.class,
                BoostsSyncPacket::encode,
                BoostsSyncPacket::decode,
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> PacketHandler.handleBoostsSync(msg));
                    ctx.get().setPacketHandled(true);
                }
        );

        INSTANCE.registerMessage(id++,
                ModifyAttributePacket.class,
                ModifyAttributePacket::encode,
                ModifyAttributePacket::decode,
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> PacketHandler.handleModifyAttribute(msg, ctx.get().getSender()));
                    ctx.get().setPacketHandled(true);
                }
        );

        INSTANCE.registerMessage(id++,
                ActivateInfiniteIcePacket.class,
                ActivateInfiniteIcePacket::encode,
                ActivateInfiniteIcePacket::decode,
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> PacketHandler.handleActivateInfiniteIce(msg, ctx.get().getSender()));
                    ctx.get().setPacketHandled(true);
                }
        );

        INSTANCE.registerMessage(id++,
                SyncAbilityLevelPacket.class,
                SyncAbilityLevelPacket::encode,
                SyncAbilityLevelPacket::decode,
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> PacketHandler.handleSyncAbilityLevel(msg));
                    ctx.get().setPacketHandled(true);
                }
        );

        INSTANCE.registerMessage(id++,
                RequestEntityUpgradesPacket.class,
                RequestEntityUpgradesPacket::encode,
                RequestEntityUpgradesPacket::decode,
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> PacketHandler.handleRequestEntityUpgrades(msg, ctx.get().getSender()));
                    ctx.get().setPacketHandled(true);
                }
        );

        INSTANCE.registerMessage(id++,
                SyncEntityUpgradesPacket.class,
                SyncEntityUpgradesPacket::encode,
                SyncEntityUpgradesPacket::decode,
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> PacketHandler.handleSyncEntityUpgrades(msg));
                    ctx.get().setPacketHandled(true);
                }
        );
    }

    public static void sendToClient(ServerPlayer player, Object packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }
}
