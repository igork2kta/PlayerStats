package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SyncEntityUpgradesPacket {
    private final int entityId;
    private final Map<String, Integer> upgrades;
    private final Set<String> disabledAttributes;

    public SyncEntityUpgradesPacket(int entityId, Map<String, Integer> upgrades, Set<String> disabledAttributes) {
        this.entityId = entityId;
        this.upgrades = upgrades;
        this.disabledAttributes = disabledAttributes;
    }

    public int getEntityId() { return entityId; }
    public Map<String, Integer> getUpgrades() { return upgrades; }
    public Set<String> getDisabledAttributes() { return disabledAttributes; }

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
}
