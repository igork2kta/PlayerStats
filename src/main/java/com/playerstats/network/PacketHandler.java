package com.playerstats.network;

import com.playerstats.PlayerStats;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;


public class PacketHandler {
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
                ModifyAttributePacket.class,
                ModifyAttributePacket::encode,
                ModifyAttributePacket::decode,
                ModifyAttributePacket::handle);

        INSTANCE.registerMessage(id++,
                UpdatePointsPacket.class,
                UpdatePointsPacket::encode,
                UpdatePointsPacket::decode,
                UpdatePointsPacket::handle);

        INSTANCE.registerMessage( id++,
                ResetAttributesPacket.class,
                ResetAttributesPacket::toBytes,
                ResetAttributesPacket::new,
                ResetAttributesPacket::handle
        );

        INSTANCE.registerMessage(id++,
                UpdateUpgradeCountPacket.class,
                UpdateUpgradeCountPacket::encode,
                UpdateUpgradeCountPacket::decode,
                UpdateUpgradeCountPacket::handle
        );
    }


    public static <MSG> void sendToClient(MSG message, net.minecraft.server.level.ServerPlayer player) {
        INSTANCE.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}
