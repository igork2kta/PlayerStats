package com.playerstats.forge;

import com.playerstats.Config;
import com.playerstats.ModAttributes;
import com.playerstats.PlayerStats;
import com.playerstats.client.KeyBindings;
import com.playerstats.client.KeyMappings;
import com.playerstats.forge.config.ForgeConfig;
import com.playerstats.forge.event.ForgeEvents;
import com.playerstats.forge.network.ForgePacketHandler;
import com.playerstats.forge.platform.ForgeRegistryHelper;
import com.playerstats.items.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PlayerStats.MODID)
public class PlayerStatsForge {

    public PlayerStatsForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Config.setInstance(new ForgeConfig());

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgeConfig.COMMON);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ForgeConfig.SERVER);

        ForgeRegistryHelper.register(modEventBus);
        ForgePacketHandler.register();

        ModItems.init();
        ModAttributes.init();
        PlayerStats.init();

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::onEntityAttributeModification);
        modEventBus.addListener(this::onConfigReload);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(this::onClientSetup);
            modEventBus.addListener(this::registerBindings);
            MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
        });

        MinecraftForge.EVENT_BUS.register(ForgeEvents.class);
    }

    private void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            ModAttributes.applyEntityAttributes(type, event::add);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.UPGRADE_RUNE.get());
            event.accept(ModItems.ATTRIBUTE_BOOST_SCROLL.get());
            event.accept(ModItems.ABILITY_CRYSTAL.get());
            event.accept(ModItems.SOUL_FRAGMENT.get());
            event.accept(ModItems.SOUL_STONE.get());
        }
    }

    private void onConfigReload(ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Loading || event instanceof ModConfigEvent.Reloading) {
            if (event.getConfig().getSpec() == ForgeConfig.SERVER) {
                ForgeConfig.reloadCache();
            }
        }
    }

    private void onClientSetup(FMLClientSetupEvent event) {
    }

    private void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(KeyMappings.OPEN_STATS_KEY);
        event.register(KeyMappings.OPEN_ENTITY_STATS_KEY);
        event.register(KeyMappings.ACTIVATE_ICE_KEY);
    }

    private void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            KeyBindings.handleClientTick();
        }
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        PlayerStats.modifyLootTable(event.getName(), event.getTable()::addPool);
    }
}
