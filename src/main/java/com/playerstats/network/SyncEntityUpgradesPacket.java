package com.playerstats.network;

import com.playerstats.client.ClientAttributeCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Sincroniza com o cliente a contagem de melhorias de atributos e quais estão desativados para uma entidade.
 */
public class SyncEntityUpgradesPacket {
    private final int entityId;
    private final Map<String, Integer> upgrades;
    private final Set<String> disabledAttributes;

    public SyncEntityUpgradesPacket(int entityId, Map<String, Integer> upgrades, Set<String> disabledAttributes) {
        this.entityId = entityId;
        this.upgrades = upgrades;
        this.disabledAttributes = disabledAttributes;
    }

    public static void encode(SyncEntityUpgradesPacket msg, FriendlyByteBuf buf) {
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

    public static SyncEntityUpgradesPacket decode(FriendlyByteBuf buf) {
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

    public static void handle(SyncEntityUpgradesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientAttributeCache.setEntityUpgrades(msg.entityId, msg.upgrades, msg.disabledAttributes);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
