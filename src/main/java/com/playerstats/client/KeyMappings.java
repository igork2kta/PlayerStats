package com.playerstats.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;


public class KeyMappings {

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath("playerstats", "playerstats")
    );

    public static final Lazy<KeyMapping> OPEN_STATS_KEY = Lazy.of(() -> new KeyMapping(
            "key.playerstats.open_stats_screen", // Will be localized using this translation key
            InputConstants.Type.KEYSYM, // Default mapping is on the keyboard
            GLFW.GLFW_KEY_R, // Default key is P
            CATEGORY
    ));

    public static final Lazy<KeyMapping> OPEN_ENTITY_STATS_KEY = Lazy.of(() -> new KeyMapping(
            "key.playerstats.open_entity_stats", // Will be localized using this translation key
            InputConstants.Type.KEYSYM, // Default mapping is on the keyboard
            GLFW.GLFW_KEY_H, // Default key is H
            CATEGORY
    ));

    public static final Lazy<KeyMapping> ACTIVATE_ICE_KEY = Lazy.of(() -> new KeyMapping(
            "key.playerstats.activate_ice",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V, // Padrão tecla V
            CATEGORY
    ));

    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(OPEN_STATS_KEY.get());
        event.register(OPEN_ENTITY_STATS_KEY.get());
        event.register(ACTIVATE_ICE_KEY.get());
    }


}
