package com.playerstats;

import com.playerstats.client.KeyBindings;
import com.playerstats.command.PlayerStatsCommands;
import com.playerstats.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(PlayerStats.MODID)
@Mod.EventBusSubscriber(modid = "playerstats")
public class PlayerStats {
    public static final String MODID = "playerstats";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public PlayerStats() {

        // Registrar config no construtor (ESSENCIAL)
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER);

        // Registrar pacotes de rede
        PacketHandler.register();

        // Registrar eventos apenas no lado do cliente
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () ->
                () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup));

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReload);
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

    private void onConfigReload(ModConfigEvent event) {
        if (event.getConfig().getSpec() == Config.SERVER) {
            Config.reloadCustomMobChances();
        }
    }


}


