package com.playerstats.network;

import com.playerstats.PlayerStats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;


public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1.0";

    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(PlayerStats.MODID, "main"),
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
                UpdatePointsPacket::handle);

        INSTANCE.registerMessage(id++,
                ResetAttributesPacket.class,
                ResetAttributesPacket::encode,
                ResetAttributesPacket::decode,
                ResetAttributesPacket::handle);

        INSTANCE.registerMessage(id++,
                UpdateUpgradeCountPacket.class,
                UpdateUpgradeCountPacket::encode,
                UpdateUpgradeCountPacket::decode,
                UpdateUpgradeCountPacket::handle
        );

        INSTANCE.registerMessage(id++, BoostsSyncPacket.class,
                BoostsSyncPacket::encode,
                BoostsSyncPacket::decode,
                BoostsSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(id++, ModifyAttributePacket.class,
                ModifyAttributePacket::encode,
                ModifyAttributePacket::decode,
                ModifyAttributePacket::handle);

    }


    public static <MSG> void sendToClient(MSG message, net.minecraft.server.level.ServerPlayer player) {
        INSTANCE.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToClient(ServerPlayer player, Object packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}
