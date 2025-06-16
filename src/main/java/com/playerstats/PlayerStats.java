package com.playerstats;

import com.playerstats.client.KeyBindings;
import com.playerstats.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.DistExecutor;

@Mod(PlayerStats.MODID)
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
}
