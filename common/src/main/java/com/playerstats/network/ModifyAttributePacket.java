package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;

public class ModifyAttributePacket {
    private final int entityId;
    private final String attributeId;
    private final boolean isToggle;

    public ModifyAttributePacket(int entityId, String attributeId) {
        this(entityId, attributeId, false);
    }

    public ModifyAttributePacket(int entityId, String attributeId, boolean isToggle) {
        this.entityId = entityId;
        this.attributeId = attributeId;
        this.isToggle = isToggle;
    }

    public int getEntityId() { return entityId; }
    public String getAttributeId() { return attributeId; }
    public boolean isToggle() { return isToggle; }

    public static void encode(ModifyAttributePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeUtf(msg.attributeId);
        buf.writeBoolean(msg.isToggle);
    }

    public static ModifyAttributePacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        String attrId = buf.readUtf();
        boolean isToggle = buf.isReadable() && buf.readBoolean();
        return new ModifyAttributePacket(entityId, attrId, isToggle);
    }
}
