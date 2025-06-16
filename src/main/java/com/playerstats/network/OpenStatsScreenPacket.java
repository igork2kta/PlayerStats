package com.playerstats.network;

import com.playerstats.client.StatsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenStatsScreenPacket {
    public OpenStatsScreenPacket() {
        // Sem dados, só sinaliza que deve abrir a tela
    }

    public static void encode(OpenStatsScreenPacket msg, FriendlyByteBuf buf) {
        // Nada a codificar
    }

    public static OpenStatsScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenStatsScreenPacket();
    }

//    public static void handle(OpenStatsScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
//        ctx.get().enqueueWork(() -> openScreen());
//        ctx.get().setPacketHandled(true);
//    }


    public static void handle(OpenStatsScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                Minecraft.getInstance().setScreen(new StatsScreen());
            } else {
                // Aqui é o servidor, que provavelmente não deve abrir tela, pode ignorar ou enviar resposta ao cliente.
            }
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void openScreen() {
        Minecraft.getInstance().setScreen(new StatsScreen());
    }
}
