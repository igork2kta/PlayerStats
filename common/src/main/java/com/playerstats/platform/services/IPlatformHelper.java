package com.playerstats.platform.services;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.nio.file.Path;

public interface IPlatformHelper {

    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    CompoundTag getPersistentData(Entity entity);

    void sendToServer(CustomPacketPayload packet);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload packet);

    Path getConfigDir();

    boolean isPhysicalClient();

    String getModName(String modId);
}
