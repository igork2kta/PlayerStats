package com.playerstats;

import com.playerstats.client.KeyBindings;
import com.playerstats.command.PlayerStatsCommands;
import com.playerstats.event.PlayerAttributePersistence;
import com.playerstats.network.PacketHandler;
import com.playerstats.network.UpdatePointsPacket;
import com.playerstats.network.UpdateUpgradeCountPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod(PlayerStats.MODID)
@Mod.EventBusSubscriber(modid = "playerstats")
public class PlayerStats {
    public static final String MODID = "playerstats";

    public PlayerStats() {
        // Registrar pacotes de rede
        PacketHandler.register();

        // Registrar eventos apenas no lado do cliente
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () ->
                () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup));
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        KeyBindings.register();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        PlayerStatsCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {

    }
}


