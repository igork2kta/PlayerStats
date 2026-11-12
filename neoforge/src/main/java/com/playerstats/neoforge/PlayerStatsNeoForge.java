package com.playerstats.neoforge;

import com.playerstats.Config;
import com.playerstats.ModAttributes;
import com.playerstats.PlayerStats;
import com.playerstats.client.KeyMappings;
import com.playerstats.command.PlayerStatsCommands;
import com.playerstats.items.ModItems;
import com.playerstats.neoforge.client.ModClientConfigScreen;
import com.playerstats.neoforge.config.NeoForgeConfig;
import com.playerstats.neoforge.event.NeoForgeEvents;
import com.playerstats.neoforge.platform.NeoForgeRegistryHelper;
import com.playerstats.network.*;
import com.playerstats.util.ModDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(PlayerStats.MODID)
public class PlayerStatsNeoForge {

    public PlayerStatsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Inicializa a ponte de configuração
        Config.setInstance(new NeoForgeConfig());

        // Registra registries DeferredRegister
        NeoForgeRegistryHelper.register(modEventBus);

        // Força carregamento das classes estáticas comuns
        ModItems.init();
        ModDataComponents.init();
        ModAttributes.init();
        PlayerStats.init();

        // Listeners do modEventBus
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::onEntityAttributeModification);
        modEventBus.addListener(this::onRegisterPayloads);
        modEventBus.addListener(this::onConfigReload);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerBindings);
            ModClientConfigScreen.register(modContainer);
        }

        // Registra configs FML
        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeConfig.COMMON);
        modContainer.registerConfig(ModConfig.Type.SERVER, NeoForgeConfig.SERVER);

        // Registra eventos do jogo
        NeoForge.EVENT_BUS.register(NeoForgeEvents.class);
        NeoForge.EVENT_BUS.register(this);
    }

    private void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            ModAttributes.applyEntityAttributes(type, event::add);
        }
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1.0");

        // Cliente -> Servidor
        registrar.playToServer(
                ModifyAttributePacket.TYPE,
                ModifyAttributePacket.CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer player) {
                        PacketHandler.handleModifyAttribute(pkt, player);
                    }
                })
        );

        registrar.playToServer(
                RequestEntityUpgradesPacket.TYPE,
                RequestEntityUpgradesPacket.CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer player) {
                        PacketHandler.handleRequestEntityUpgrades(pkt, player);
                    }
                })
        );

        registrar.playToServer(
                ResetAttributesPacket.TYPE,
                ResetAttributesPacket.CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer player) {
                        PacketHandler.handleResetAttributes(pkt, player);
                    }
                })
        );

        registrar.playToServer(
                ActivateInfiniteIcePacket.TYPE,
                ActivateInfiniteIcePacket.CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer player) {
                        PacketHandler.handleActivateInfiniteIce(pkt, player);
                    }
                })
        );

        // Servidor -> Cliente
        registrar.playToClient(
                UpdatePointsPacket.TYPE,
                UpdatePointsPacket.CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> PacketHandler.handleUpdatePoints(pkt))
        );

        registrar.playToClient(
                UpdateUpgradeCountPacket.TYPE,
                UpdateUpgradeCountPacket.CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> PacketHandler.handleUpdateUpgradeCount(pkt))
        );

        registrar.playToClient(
                SyncAbilityLevelPacket.TYPE,
                SyncAbilityLevelPacket.CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> PacketHandler.handleSyncAbilityLevel(pkt))
        );

        registrar.playToClient(
                SyncEntityUpgradesPacket.TYPE,
                SyncEntityUpgradesPacket.CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> PacketHandler.handleSyncEntityUpgrades(pkt))
        );

        registrar.playToClient(
                BoostsSyncPacket.TYPE,
                BoostsSyncPacket.CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> PacketHandler.handleBoostsSync(pkt))
        );
    }

    private void onConfigReload(ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Loading || event instanceof ModConfigEvent.Reloading) {
            if (event.getConfig().getSpec() == NeoForgeConfig.SERVER) {
                NeoForgeConfig.reload();
            }
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

    private void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(KeyMappings.OPEN_STATS_KEY);
        event.register(KeyMappings.OPEN_ENTITY_STATS_KEY);
        event.register(KeyMappings.ACTIVATE_ICE_KEY);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        PlayerStatsCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        PlayerStats.modifyLootTable(event.getName(), poolBuilder -> event.getTable().addPool(poolBuilder.build()));
    }
}
