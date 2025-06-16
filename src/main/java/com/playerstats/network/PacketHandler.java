package com.playerstats.network;

import com.playerstats.PlayerStats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1.0";
    private static int packetId = 0;

    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PlayerStats.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
//        INSTANCE.registerMessage(
//                nextId(),
//                OpenStatsScreenPacket.class,
//                OpenStatsScreenPacket::encode,
//                OpenStatsScreenPacket::decode,
//                OpenStatsScreenPacket::handle,
//                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
//        );

        int id = 0;
        INSTANCE.registerMessage(id++, OpenStatsScreenPacket.class,
                OpenStatsScreenPacket::encode,
                OpenStatsScreenPacket::decode,
                OpenStatsScreenPacket::handle);

        INSTANCE.registerMessage(id++, ModifyAttributePacket.class,
                ModifyAttributePacket::encode,
                ModifyAttributePacket::decode,
                ModifyAttributePacket::handle);

        INSTANCE.registerMessage(id++,
                UpdatePointsPacket.class,
                UpdatePointsPacket::encode,
                UpdatePointsPacket::decode,
                UpdatePointsPacket::handle);
    }

    private static int nextId() {
        return packetId++;
    }

    public static <MSG> void sendToClient(MSG message, net.minecraft.server.level.ServerPlayer player) {
        INSTANCE.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}
