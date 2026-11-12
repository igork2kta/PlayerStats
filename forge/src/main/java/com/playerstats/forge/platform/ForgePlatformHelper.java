package com.playerstats.forge.platform;

import com.playerstats.forge.network.ForgePacketHandler;
import com.playerstats.platform.services.IPlatformHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }

    @Override
    public CompoundTag getPersistentData(Entity entity) {
        return entity.getPersistentData();
    }

    @Override
    public void sendToServer(Object packet) {
        ForgePacketHandler.sendToServer(packet);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, Object packet) {
        ForgePacketHandler.sendToClient(player, packet);
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isPhysicalClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public String getModName(String modId) {
        if ("minecraft".equals(modId)) return "Minecraft";
        return ModList.get().getModContainerById(modId)
                .map(c -> c.getModInfo().getDisplayName())
                .orElse(org.apache.commons.lang3.StringUtils.capitalize(modId));
    }
}
