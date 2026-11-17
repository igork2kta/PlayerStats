package com.playerstats.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyMappings {

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath("playerstats", "playerstats")
    );

    public static final KeyMapping OPEN_STATS_KEY = new KeyMapping(
            "key.playerstats.open_stats_screen",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );

    public static final KeyMapping OPEN_ENTITY_STATS_KEY = new KeyMapping(
            "key.playerstats.open_entity_stats",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
    );

    public static final KeyMapping ACTIVATE_ICE_KEY = new KeyMapping(
            "key.playerstats.activate_ice",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
}
