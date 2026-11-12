package com.playerstats.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyMappings {

    public static final KeyMapping OPEN_STATS_KEY = new KeyMapping(
            "key.playerstats.open_stats",
            GLFW.GLFW_KEY_R,
            "key.categories.playerstats"
    );

    public static final KeyMapping OPEN_ENTITY_STATS_KEY = new KeyMapping(
            "key.playerstats.open_entity_stats",
            GLFW.GLFW_KEY_H,
            "key.categories.playerstats"
    );

    public static final KeyMapping ACTIVATE_ICE_KEY = new KeyMapping(
            "key.playerstats.activate_ice",
            GLFW.GLFW_KEY_V,
            "key.categories.playerstats"
    );
}
