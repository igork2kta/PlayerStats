package com.playerstats.network;

import com.playerstats.client.ClientBoostCache;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BoostsSyncPacket {

    private final Map<ResourceLocation, BoostData> boosts;

    public BoostsSyncPacket(Map<ResourceLocation, BoostData> boosts) {
        this.boosts = boosts;
    }

    public static class BoostData {
        public final double amount;
        public final int secondsRemaining;

        public BoostData(double amount, int secondsRemaining) {
            this.amount = amount;
            this.secondsRemaining = secondsRemaining;
        }
    }

    // Serializa os dados para o buffer (envio)
    public static void encode(BoostsSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.boosts.size());
        for (Map.Entry<ResourceLocation, BoostData> entry : pkt.boosts.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeDouble(entry.getValue().amount);
            buf.writeInt(entry.getValue().secondsRemaining);
        }
    }

    // Desserializa o pacote (recebimento)
    public static BoostsSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<ResourceLocation, BoostData> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation attrId = buf.readResourceLocation();
            double amount = buf.readDouble();
            int seconds = buf.readInt();
            map.put(attrId, new BoostData(amount, seconds));
        }
        return new BoostsSyncPacket(map);
    }

    // Lida com o pacote no lado do cliente
    public static void handle(BoostsSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Limpa cache atual
            ClientBoostCache.activeBoosts.clear();

            for (Map.Entry<ResourceLocation, BoostData> entry : pkt.boosts.entrySet()) {
                Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(entry.getKey());
                if (attribute != null) {
                    BoostData bd = entry.getValue();
                    ClientBoostCache.activeBoosts.put(attribute,
                            new ClientBoostCache.BoostInfo(bd.amount, bd.secondsRemaining));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
