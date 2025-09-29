package com.playerstats;

import com.playerstats.client.KeyBindings;
import com.playerstats.command.PlayerStatsCommands;
import com.playerstats.items.ModItems;
import com.playerstats.network.PacketHandler;
//import com.playerstats.sounds.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.IEventBus;
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

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Registrar config no construtor (ESSENCIAL)
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER);

        // Registrar pacotes de rede
        PacketHandler.register();
        // Registrar os itens
        ModItems.register(modEventBus);
        //ModSounds.register(modEventBus);
        ModAttributes.ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());

        modEventBus.addListener(this::addCreative);

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


    private void onConfigReload(ModConfigEvent event) {
        if (event.getConfig().getSpec() == Config.SERVER) {
            Config.reloadCustomMobChances();
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();

        // Verifica se o loot é de estrutura (todos estão no namespace "minecraft:chests/")
        if (name != null && name.getNamespace().equals("minecraft") && name.getPath().startsWith("chests/")) {
            // Pool 1: concorrem entre si
            LootPool pool1 = LootPool.lootPool()
                    .name("upgrade_or_crystal_pool")
                    .setRolls(ConstantValue.exactly(1)) // rola apenas 1 vez
                    .add(LootItem.lootTableItem(ModItems.UPGRADE_RUNE.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.15f)) // 7%
                            .setWeight(1)
                            .setQuality(1))
                    .add(LootItem.lootTableItem(ModItems.ABILITY_CRYSTAL.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.10f)) // 5%
                            .setWeight(1)
                            .setQuality(1))
                    .build();

            // Pool 2: ATTRIBUTE_BOOST_SCROLL independente
            LootPool pool2 = LootPool.lootPool()
                    .name("attribute_boost_scroll_pool")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.ATTRIBUTE_BOOST_SCROLL.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.12f)) // 12%
                            .setWeight(1)
                            .setQuality(1))
                    .build();
            event.getTable().addPool(pool1);
            event.getTable().addPool(pool2);

            // Pool 3: SOUL_STONE apenas em The End, Bastions e Ancient City
            boolean isTargetChest =
                    name.getPath().equals("chests/end_city_treasure") ||
                            name.getPath().startsWith("chests/bastion_") ||
                            name.getPath().equals("chests/ancient_city") ||
                            name.getPath().equals("chests/ancient_city_ice_box");

            if (isTargetChest) {
                LootPool pool3 = LootPool.lootPool()
                        .name("soul_stone_pool")
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.SOUL_STONE.get())
                                .when(LootItemRandomChanceCondition.randomChance(0.08f)) // 8%
                                .setWeight(1)
                                .setQuality(1))
                        .build();
                event.getTable().addPool(pool3);
            }

        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event){
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES){
            event.accept(ModItems.UPGRADE_RUNE);
            event.accept(ModItems.ATTRIBUTE_BOOST_SCROLL);
            event.accept(ModItems.ABILITY_CRYSTAL);
            event.accept(ModItems.SOUL_FRAGMENT);
            event.accept(ModItems.SOUL_STONE);
        }
    }


}


