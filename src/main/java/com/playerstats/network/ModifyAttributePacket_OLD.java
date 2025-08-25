//package com.playerstats.network;
//
//import com.playerstats.event.PlayerAttributePersistence;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraftforge.network.NetworkEvent;
//
//import java.util.function.Supplier;
//
//public class ModifyAttributePacket_OLD {
//    private final String attributeId;
//
//    public ModifyAttributePacket_OLD(String attributeId) {
//        this.attributeId = attributeId;
//
//    }
//
//    public static void encode(ModifyAttributePacket_OLD msg, FriendlyByteBuf buf) {
//        buf.writeUtf(msg.attributeId);
//    }
//
//    public static ModifyAttributePacket_OLD decode(FriendlyByteBuf buf) {
//        return new ModifyAttributePacket_OLD(buf.readUtf());
//    }
//
//
//
//    public static void handle(ModifyAttributePacket_OLD msg, Supplier<NetworkEvent.Context> ctx) {
//        ctx.get().enqueueWork(() -> {
//
//            ServerPlayer player = ctx.get().getSender();
//            if (player != null) {
//                PlayerAttributePersistence.upgradeAttribute(player, msg.attributeId);
//            }
//
//        });
//        ctx.get().setPacketHandled(true);
//    }
//}
