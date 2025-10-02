package com.playerstats.network;

import com.playerstats.client.ClientBoostCache;
import com.playerstats.event.PlayerAttributePersistence;
import com.playerstats.util.AttributeUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public class PacketHandler {

    public static void register(IEventBus modEventBus) {
        // Escuta o evento de registro de pacotes
        modEventBus.addListener(PacketHandler::onRegisterPayloads);
    }


    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1.0"); // versão do protocolo (pode ser "1.0")

        // Cliente → Servidor
        registrar.playToServer(
                ModifyAttributePacket.TYPE,
                ModifyAttributePacket.CODEC,
                PacketHandler::handleModifyAttribute
        );

        // Cliente <- Servidor
        registrar.playToClient(
                UpdatePointsPacket.TYPE,
                UpdatePointsPacket.CODEC,
                PacketHandler::handleUpdatePoints

        );

        // Servidor → Cliente
        registrar.playToClient(
                UpdateUpgradeCountPacket.TYPE,
                UpdateUpgradeCountPacket.CODEC,
                PacketHandler::handleUpdateUpgradeCount
        );

        // Cliente → Servidor
        registrar.playToServer(
                ResetAttributesPacket.TYPE,
                ResetAttributesPacket.CODEC,
                PacketHandler::handleResetAttributes
        );

        // Servidor → Cliente
        registrar.playToClient(
                BoostsSyncPacket.TYPE,
                BoostsSyncPacket.CODEC,
                PacketHandler::sendBoostsToClient
        );
        // Exemplo: se UpdatePointsPacket também for C->S:
        // registrar.playToServer(UpdatePointsPacket.TYPE, UpdatePointsPacket.CODEC, PacketHandler::handleUpdatePoints);


        // Servidor → Cliente
        // registrar.playToClient(BoostsSyncPacket.TYPE, BoostsSyncPacket.CODEC, PacketHandler::handleBoostsSync);
    }



    // Helpers de envio
    public static void sendToClient(CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);

    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    private static void handleModifyAttribute(ModifyAttributePacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                var level = player.level();
                var entity = level.getEntity(msg.entityId());

                if (entity instanceof LivingEntity living) {
                    var attribute = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.tryParse(msg.attributeId()));
                    if (attribute != null) {
                        var instance = AttributeUtils.getAttributeInstance(living, attribute);
                        if (instance != null) {

                            PlayerAttributePersistence.upgradeAttribute(living, player, msg.attributeId());

                        }
                    }
                }
            }
        });
    }

    private static void handleUpdatePoints(UpdatePointsPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            // estamos no lado cliente aqui
            if(msg.pointsType().equals("attribute"))
                com.playerstats.client.ClientAttributeCache.setPoints(msg.points());
            else if(msg.pointsType().equals("ability"))
                com.playerstats.client.ClientAttributeCache.setAbilityPoints(msg.points());
        });
    }

    private static void handleUpdateUpgradeCount(UpdateUpgradeCountPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Estamos no lado cliente
            com.playerstats.client.ClientAttributeCache.setUpgradeCount(packet.upgradeCount());
        });
    }

    private static void handleResetAttributes(ResetAttributesPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                var level = player.level();
                var entity = level.getEntity(msg.entityId());
                if (entity instanceof LivingEntity living) {
                    PlayerAttributePersistence.resetAttributes(living, player, false);
                }
            }
        });
    }

    // Lida com o pacote no lado do cliente
    public static void sendBoostsToClient(BoostsSyncPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Limpa cache atual
            ClientBoostCache.activeBoosts.clear();

            for (Map.Entry<ResourceLocation, BoostsSyncPacket.BoostData> entry : pkt.boosts.entrySet()) {
                Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(entry.getKey());
                if (attribute != null) {
                    BoostsSyncPacket.BoostData bd = entry.getValue();
                    ClientBoostCache.activeBoosts.put(attribute,
                            new ClientBoostCache.BoostInfo(bd.amount, bd.secondsRemaining));
                }
            }
        });
    }
}
