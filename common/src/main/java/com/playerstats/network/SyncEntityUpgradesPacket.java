package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Sincroniza com o cliente a contagem de melhorias de atributos e quais estão desativados para uma entidade.
 */
public record SyncEntityUpgradesPacket(
        int entityId,
        Map<String, Integer> upgrades,
        Set<String> disabledAttributes
) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("playerstats", "sync_entity_upgrades");

    public static final Type<SyncEntityUpgradesPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SyncEntityUpgradesPacket> CODEC =
            StreamCodec.of(SyncEntityUpgradesPacket::encode, SyncEntityUpgradesPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(FriendlyByteBuf buf, SyncEntityUpgradesPacket msg) {
        buf.writeInt(msg.entityId);

        buf.writeInt(msg.upgrades.size());
        for (Map.Entry<String, Integer> entry : msg.upgrades.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeInt(entry.getValue());
        }

        buf.writeInt(msg.disabledAttributes.size());
        for (String attrId : msg.disabledAttributes) {
            buf.writeUtf(attrId);
        }
    }

    private static SyncEntityUpgradesPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();

        int upgradesSize = buf.readInt();
        Map<String, Integer> upgrades = new HashMap<>(upgradesSize);
        for (int i = 0; i < upgradesSize; i++) {
            upgrades.put(buf.readUtf(), buf.readInt());
        }

        int disabledSize = buf.readInt();
        Set<String> disabled = new HashSet<>(disabledSize);
        for (int i = 0; i < disabledSize; i++) {
            disabled.add(buf.readUtf());
        }

        return new SyncEntityUpgradesPacket(entityId, upgrades, disabled);
    }
}
