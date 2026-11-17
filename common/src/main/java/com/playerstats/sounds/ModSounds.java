package com.playerstats.sounds;

import com.playerstats.PlayerStats;
import com.playerstats.platform.Services;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public class ModSounds {
    public static final Supplier<SoundEvent> WOLF_HOWL = Services.REGISTRY.registerSoundEvent("wolf_howl",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(PlayerStats.MODID, "wolf_howl")));

    public static void init() {
    }
}
