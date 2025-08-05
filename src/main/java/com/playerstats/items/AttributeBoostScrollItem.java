package com.playerstats.items;

import com.playerstats.Config;
import com.playerstats.util.AttributeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.*;
@Mod.EventBusSubscriber(modid = "playerstats", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AttributeBoostScrollItem extends Item {


    // Mapeia jogador -> lista de boosts ativos
    private static final Map<UUID, List<BoostInstance>> activeBoosts = new HashMap<>();

    public AttributeBoostScrollItem(Properties properties) {
        super(properties);
    }

    // Classe interna para representar um boost
    public static class BoostInstance {
        public final Attribute attribute;
        public final UUID modifierId;
        public int ticksRemaining;

        public BoostInstance(Attribute attribute, UUID modifierId, int ticks) {
            this.attribute = attribute;
            this.modifierId = modifierId;
            this.ticksRemaining = ticks;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {


            // Define atributo se ainda não estiver salvo
            if (!stack.hasTag() || !stack.getTag().contains("DefinedAttribute")) {
                List<Attribute> filteredAttributes = BuiltInRegistries.ATTRIBUTE.stream()
                        .filter(attr -> {
                            AttributeInstance attribute = player.getAttributes().getInstance(attr);
                            if (attribute == null || !attribute.getAttribute().isClientSyncable()) return false;
                            if (Config.cachedIgnoredAttributes.contains(attr.getDescriptionId())) return false;
                            return true;
                        }).toList();

                if (!filteredAttributes.isEmpty()) {
                    Random random = new Random();
                    Attribute selected = filteredAttributes.get(random.nextInt(filteredAttributes.size()));
                    stack.getOrCreateTag().putString("DefinedAttribute", BuiltInRegistries.ATTRIBUTE.getKey(selected).toString());
                }


            } else {

                // Já tem atributo salvo
                String attrKey = stack.getTag().getString("DefinedAttribute");
                Attribute attribute = BuiltInRegistries.ATTRIBUTE.getOptional(new ResourceLocation(attrKey)).orElse(null);

                if (attribute != null) {
                    UUID modifierId = UUID.randomUUID();
                    double amount = AttributeUtils.getIncrement(attribute.getDescriptionId());
                    int duration = 20 * 60 * 3; // 3 minutos = 3600 ticks

                    applyBoost(player, attribute, modifierId, amount, duration);
                }

                // Consumir o item (1 unidade)
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }

        }

        return InteractionResultHolder.success(stack);
    }

    private void applyBoost(Player player, Attribute attribute, UUID id, double amount, int duration) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        instance.removeModifier(id); // segurança
        instance.addTransientModifier(new AttributeModifier(id, "Scroll Boost", amount, AttributeModifier.Operation.ADDITION));

        UUID playerId = player.getUUID();
        activeBoosts.computeIfAbsent(playerId, k -> new ArrayList<>())
                .add(new BoostInstance(attribute, id, duration));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.hasTag() && stack.getTag().contains("DefinedAttribute")) {
            String attrKey = stack.getTag().getString("DefinedAttribute");
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.getOptional(new ResourceLocation(attrKey)).orElse(null);
            if (attribute != null) {
                tooltip.add(Component.translatable("item.playerstats.attribute_boost_scroll.hover_text_unveiled",
                        AttributeUtils.getAttributeName(attribute)).withStyle(ChatFormatting.AQUA));
            }
        } else {
            tooltip.add(Component.translatable("item.playerstats.attribute_boost_scroll.hover_text")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        UUID playerId = event.player.getUUID();
        List<BoostInstance> boosts = activeBoosts.get(playerId);
        if (boosts == null || boosts.isEmpty()) return;

        Iterator<BoostInstance> iterator = boosts.iterator();
        while (iterator.hasNext()) {
            BoostInstance boost = iterator.next();
            boost.ticksRemaining--;

            if (boost.ticksRemaining <= 0) {
                AttributeInstance instance = event.player.getAttribute(boost.attribute);
                if (instance != null) {
                    instance.removeModifier(boost.modifierId);
                }
                iterator.remove();
            }
        }

        // Remove do mapa se não tiver mais boosts
        if (boosts.isEmpty()) {
            activeBoosts.remove(playerId);
        }
    }


    public static void saveBoostsToPlayer(Player player) {
        UUID playerId = player.getUUID();
        List<BoostInstance> boosts = activeBoosts.get(playerId);
        if (boosts == null || boosts.isEmpty()) return;

        var tag = new net.minecraft.nbt.CompoundTag();
        int index = 0;
        for (BoostInstance boost : boosts) {
            var boostTag = new net.minecraft.nbt.CompoundTag();
            boostTag.putString("Attribute", BuiltInRegistries.ATTRIBUTE.getKey(boost.attribute).toString());
            boostTag.putUUID("ModifierId", boost.modifierId);
            boostTag.putInt("TicksRemaining", boost.ticksRemaining);
            tag.put("Boost" + index, boostTag);
            index++;
        }

        player.getPersistentData().put("PlayerStatsBoosts", tag);
    }

    public static void loadBoostsFromPlayer(Player player) {
        var tag = player.getPersistentData().getCompound("PlayerStatsBoosts");
        if (tag == null || tag.isEmpty()) return;

        UUID playerId = player.getUUID();
        List<BoostInstance> boosts = new ArrayList<>();

        for (String key : tag.getAllKeys()) {
            var boostTag = tag.getCompound(key);
            String attrKey = boostTag.getString("Attribute");
            UUID modId = boostTag.getUUID("ModifierId");
            int ticks = boostTag.getInt("TicksRemaining");

            Attribute attribute = BuiltInRegistries.ATTRIBUTE.getOptional(new ResourceLocation(attrKey)).orElse(null);
            if (attribute != null) {
                // reaplica o boost
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance != null) {
                    double amount = AttributeUtils.getIncrement(attribute.getDescriptionId()); // usa mesmo cálculo
                    instance.addTransientModifier(new AttributeModifier(modId, "Scroll Boost", amount, AttributeModifier.Operation.ADDITION));
                    boosts.add(new BoostInstance(attribute, modId, ticks));
                }
            }
        }

        if (!boosts.isEmpty()) {
            activeBoosts.put(playerId, boosts);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
       saveBoostsToPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
       loadBoostsFromPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Remover o NBT persistente com os boosts
        CompoundTag data = player.getPersistentData();
        data.remove("PlayerStatsBoosts");

        // Remover da memória (HashMap) os boosts ativos
        activeBoosts.remove(player.getUUID());
    }

}


