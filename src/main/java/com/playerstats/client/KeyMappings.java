package com.playerstats.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.jarjar.nio.util.Lazy;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;


@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = "playerstats", value = Dist.CLIENT)
public class KeyMappings {

    public static final Lazy<KeyMapping> OPEN_STATS_KEY = Lazy.of(() -> new KeyMapping(
            "key.playerstats.open_stats_screen", // Will be localized using this translation key
            InputConstants.Type.KEYSYM, // Default mapping is on the keyboard
            GLFW.GLFW_KEY_R, // Default key is P
            "key.categories.playerstats"
    ));

    public static final Lazy<KeyMapping> OPEN_ENTITY_STATS_KEY = Lazy.of(() -> new KeyMapping(
            "key.playerstats.open_entity_stats", // Will be localized using this translation key
            InputConstants.Type.KEYSYM, // Default mapping is on the keyboard
            GLFW.GLFW_KEY_H, // Default key is H
            "key.categories.playerstats"
    ));

    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_STATS_KEY.get());
        event.register(OPEN_ENTITY_STATS_KEY.get());
    }


}
