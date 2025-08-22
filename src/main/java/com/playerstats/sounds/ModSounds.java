//package com.playerstats.sounds;
//
//import com.playerstats.PlayerStats;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.sounds.SoundEvent;
//import net.minecraftforge.eventbus.api.IEventBus;
//import net.minecraftforge.registries.DeferredRegister;
//import net.minecraftforge.registries.ForgeRegistries;
//import net.minecraftforge.registries.RegistryObject;
//
//public class ModSounds {
//    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
//            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PlayerStats.MODID);
//
//    //Sons do mod em si
//    public static final RegistryObject<SoundEvent> UPGRADE_RUNE_SOUND = registerSoundEvent("upgrade_rune_sound");
//
//    private static RegistryObject<SoundEvent> registerSoundEvent(String name){
//        ResourceLocation id = new ResourceLocation(PlayerStats.MODID, name);
//        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PlayerStats.MODID, name)));
//    }
//    public static void register(IEventBus eventBus){
//        SOUND_EVENTS.register(eventBus);
//    }
//}