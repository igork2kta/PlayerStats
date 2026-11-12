package com.playerstats.fabric.platform;

import com.playerstats.network.*;
import com.playerstats.platform.IPersistentDataHolder;
import com.playerstats.platform.services.IPlatformHelper;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public CompoundTag getPersistentData(Entity entity) {
        return ((IPersistentDataHolder) entity).playerstats$getPersistentData();
    }

    @Override
    public void sendToServer(Object packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ResourceLocation id = null;

        if (packet instanceof ActivateInfiniteIcePacket p) {
            id = PacketHandler.ACTIVATE_INFINITE_ICE;
            ActivateInfiniteIcePacket.encode(p, buf);
        } else if (packet instanceof ModifyAttributePacket p) {
            id = PacketHandler.MODIFY_ATTRIBUTE;
            ModifyAttributePacket.encode(p, buf);
        } else if (packet instanceof RequestEntityUpgradesPacket p) {
            id = PacketHandler.REQUEST_ENTITY_UPGRADES;
            RequestEntityUpgradesPacket.encode(p, buf);
        } else if (packet instanceof ResetAttributesPacket p) {
            id = PacketHandler.RESET_ATTRIBUTES;
            ResetAttributesPacket.encode(p, buf);
        }

        if (id != null) {
            ClientPlayNetworking.send(id, buf);
        }
    }

    @Override
    public void sendToPlayer(ServerPlayer player, Object packet) {
        if (player == null) return;
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ResourceLocation id = null;

        if (packet instanceof UpdatePointsPacket p) {
            id = PacketHandler.UPDATE_POINTS;
            UpdatePointsPacket.encode(p, buf);
        } else if (packet instanceof UpdateUpgradeCountPacket p) {
            id = PacketHandler.UPDATE_UPGRADE_COUNT;
            UpdateUpgradeCountPacket.encode(p, buf);
        } else if (packet instanceof SyncAbilityLevelPacket p) {
            id = PacketHandler.SYNC_ABILITY_LEVEL;
            SyncAbilityLevelPacket.encode(p, buf);
        } else if (packet instanceof SyncEntityUpgradesPacket p) {
            id = PacketHandler.SYNC_ENTITY_UPGRADES;
            SyncEntityUpgradesPacket.encode(p, buf);
        } else if (packet instanceof BoostsSyncPacket p) {
            id = PacketHandler.BOOSTS_SYNC;
            BoostsSyncPacket.encode(p, buf);
        }

        if (id != null) {
            ServerPlayNetworking.send(player, id, buf);
        }
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isPhysicalClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public String getModName(String modId) {
        if ("minecraft".equals(modId)) return "Minecraft";
        return FabricLoader.getInstance().getModContainer(modId)
                .map(c -> c.getMetadata().getName())
                .orElse(org.apache.commons.lang3.StringUtils.capitalize(modId));
    }
}
