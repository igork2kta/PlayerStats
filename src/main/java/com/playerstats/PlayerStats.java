package com.playerstats;

import com.playerstats.client.KeyBindings;
import com.playerstats.client.KeyMappings;
import com.playerstats.command.PlayerStatsCommands;
import com.playerstats.event.PlayerAttributePersistence;
import com.playerstats.items.AttributeBoostScrollItem;
import com.playerstats.items.ModItems;
import com.playerstats.network.PacketHandler;
import com.playerstats.util.ModDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(PlayerStats.MODID)

public class PlayerStats {
    public static final String MODID = "playerstats";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    /*
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.examplemod")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());
*/
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public PlayerStats(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        // Registrar pacotes de rede
        //PacketHandler.register(modEventBus);
        // Registrar os itens
        ModItems.register(modEventBus);

        ModDataComponents.register(modEventBus);

        //Registrar pacores
        PacketHandler.register(modEventBus);

        //PlayerAttributePersistence.register();

        NeoForge.EVENT_BUS.register(AttributeBoostScrollItem.class);

        NeoForge.EVENT_BUS.register(this);

        // 🔹 Registra os atributos
        ModAttributes.ATTRIBUTES.register(modEventBus);
        // 🔹 Registra o listener do evento
        modEventBus.addListener(ModAttributes::onEntityAttributeModification);

        //Atalhos de teclado
        //modEventBus.addListener(KeyMappings::registerBindings);
        //modEventBus.addListener(KeyMappings::onClientTick);

        modEventBus.addListener(this::onConfigReload);
        //NeoForge.EVENT_BUS.addListener(KeyBindings::onClientTick); pode ser feito assim ou da forma abaixo. Porque preciso ter keymappings e keybindings?
        //KeyBindings.register();

        // Registrar eventos apenas no lado do cliente
        // No construtor do seu ModMain ou em algum registrador
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::onClientSetup);
        }

        //ModEvents.register();
        //NeoForge.EVENT_BUS.register(ModEvents.class);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER);

    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        PlayerStatsCommands.register(event.getDispatcher());
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        KeyBindings.register();
    }

    /*
    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES){
            event.accept(ModItems.UPGRADE_RUNE.get());
            event.accept(ModItems.ATTRIBUTE_BOOST_SCROLL.get());
        }
    }
*/
    private void onConfigReload(ModConfigEvent event) {
        if (event.getConfig().getSpec() == Config.SERVER) {
            Config.reloadCustomMobChances();
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();

        // Verifica se o loot é de estrutura (todos estão no namespace "minecraft:chests/")
        if (name != null && name.getNamespace().equals("minecraft") && name.getPath().startsWith("chests/")) {
            // Pool 1: concorrem entre si
            LootPool pool1 = LootPool.lootPool()
                    .name("upgrade_or_crystal_pool")
                    .setRolls(ConstantValue.exactly(1)) // rola apenas 1 vez
                    .add(LootItem.lootTableItem(ModItems.UPGRADE_RUNE.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.10f)) // 7%
                            .setWeight(1)
                            .setQuality(1))
                    .add(LootItem.lootTableItem(ModItems.ABILITY_CRYSTAL.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.7f)) // 5%
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
            event.accept(ModItems.UPGRADE_RUNE.get());
            event.accept(ModItems.ATTRIBUTE_BOOST_SCROLL.get());
            event.accept(ModItems.ABILITY_CRYSTAL.get());
            event.accept(ModItems.SOUL_FRAGMENT.get());
            event.accept(ModItems.SOUL_STONE.get());
        }
    }


}


