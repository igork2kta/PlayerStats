package com.playerstats.items;

import com.playerstats.Config;
import com.playerstats.network.BoostsSyncPacket;
import com.playerstats.network.PacketHandler;
import com.playerstats.platform.Services;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class AttributeBoostScrollItem extends Item {

    private static final Map<UUID, List<BoostInstance>> activeBoosts = new HashMap<>();

    public AttributeBoostScrollItem(Properties properties) {
        super(properties);
    }

    public static class BoostInstance {
        public final Attribute attribute;
        public final UUID modifierId;
        public int ticksRemaining;
        public double amount;

        public BoostInstance(Attribute attribute, UUID modifierId, int ticksRemaining, double amount) {
            this.attribute = attribute;
            this.modifierId = modifierId;
            this.ticksRemaining = ticksRemaining;
            this.amount = amount;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            Random random = new Random();
            CompoundTag tag = stack.getOrCreateTag();

            if (!tag.contains("DefinedAttribute")) {
                List<Attribute> filteredAttributes = AttributeUtils.getAttributes(player, "");

                if (!filteredAttributes.isEmpty()) {
                    Attribute selected = filteredAttributes.get(random.nextInt(filteredAttributes.size()));
                    tag.putString("DefinedAttribute", BuiltInRegistries.ATTRIBUTE.getKey(selected).toString());

                    int minMultiplier = Config.BOOST_AMOUNT_MIN_MULTIPLIER.get();
                    int maxMultiplier = Config.BOOST_AMOUNT_MAX_MULTIPLIER.get();
                    double baseIncrement = AttributeUtils.getIncrement(selected);
                    int multiplier = minMultiplier + random.nextInt(maxMultiplier - minMultiplier + 1);
                    double amount = baseIncrement * multiplier;

                    int minMinutes = Config.BOOST_DURATION_MIN_MINUTES.get();
                    int maxMinutes = Config.BOOST_DURATION_MAX_MINUTES.get();
                    int minTicks = minMinutes * 60 * 20;
                    int maxTicks = maxMinutes * 60 * 20;
                    int duration = minTicks + random.nextInt(maxTicks - minTicks + 1);

                    tag.putDouble("BoostAmount", Math.round(amount * 100.0) / 100.0);
                    tag.putInt("BoostDuration", duration);
                }
            } else {
                String attrKey = tag.getString("DefinedAttribute");
                Attribute attribute = BuiltInRegistries.ATTRIBUTE
                        .getOptional(ResourceLocation.tryParse(attrKey))
                        .orElse(null);

                if (attribute != null) {
                    UUID modifierId = UUID.nameUUIDFromBytes(("playerstats_boost:" + attribute.getDescriptionId()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    double amount = tag.getDouble("BoostAmount");
                    int duration = tag.getInt("BoostDuration");

                    applyBoost(player, attribute, modifierId, amount, duration);
                }

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

        UUID playerId = player.getUUID();
        List<BoostInstance> boosts = activeBoosts.computeIfAbsent(playerId, k -> new ArrayList<>());

        for (BoostInstance existing : boosts) {
            if (existing.attribute.equals(attribute)) {
                existing.ticksRemaining = duration;
                existing.amount = amount;
                sendBoostsToClient(player);
                return;
            }
        }

        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, "Scroll Boost", amount, AttributeModifier.Operation.ADDITION));
        boosts.add(new BoostInstance(attribute, id, duration, amount));

        sendBoostsToClient(player);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("DefinedAttribute")) {
            Attribute attribute = BuiltInRegistries.ATTRIBUTE
                    .getOptional(ResourceLocation.tryParse(tag.getString("DefinedAttribute")))
                    .orElse(null);
            if (attribute != null) {
                double amount = tag.getDouble("BoostAmount");
                int durationSec = tag.getInt("BoostDuration") / 20;

                tooltip.add(Component.translatable(
                        "item.playerstats.attribute_boost_scroll.hover_text_unveiled",
                        AttributeUtils.getAttributeName(attribute)
                ).withStyle(ChatFormatting.AQUA));

                tooltip.add(Component.translatable(
                        "item.playerstats.attribute_boost_scroll.hover_text_values",
                        amount,
                        durationSec
                ).withStyle(ChatFormatting.GREEN));
            }
        } else {
            tooltip.add(Component.translatable("item.playerstats.attribute_boost_scroll.hover_text")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public static void handlePlayerTick(Player player) {
        if (player.level().isClientSide) return;

        UUID playerId = player.getUUID();
        List<BoostInstance> boosts = activeBoosts.get(playerId);
        if (boosts == null || boosts.isEmpty()) return;

        boolean syncNeeded = false;
        Iterator<BoostInstance> iterator = boosts.iterator();
        while (iterator.hasNext()) {
            BoostInstance boost = iterator.next();
            boost.ticksRemaining--;

            if (boost.ticksRemaining <= 0) {
                AttributeInstance instance = player.getAttribute(boost.attribute);
                if (instance != null) {
                    instance.removeModifier(boost.modifierId);
                }
                iterator.remove();
                syncNeeded = true;
            } else if (boost.ticksRemaining % 20 == 0) {
                syncNeeded = true;
            }
        }

        if (boosts.isEmpty()) {
            activeBoosts.remove(playerId);
            sendBoostsToClient(player);
        } else if (syncNeeded) {
            sendBoostsToClient(player);
        }
    }

    public static void saveBoostsToPlayer(Player player) {
        UUID playerId = player.getUUID();
        List<BoostInstance> boosts = activeBoosts.get(playerId);
        if (boosts == null || boosts.isEmpty()) {
            Services.PLATFORM.getPersistentData(player).remove("PlayerStatsBoosts");
            return;
        }

        CompoundTag tag = new CompoundTag();
        int index = 0;
        for (BoostInstance boost : boosts) {
            CompoundTag boostTag = new CompoundTag();
            boostTag.putString("Attribute", BuiltInRegistries.ATTRIBUTE.getKey(boost.attribute).toString());
            boostTag.putUUID("ModifierId", boost.modifierId);
            boostTag.putInt("TicksRemaining", boost.ticksRemaining);
            boostTag.putDouble("Amount", boost.amount);
            tag.put("Boost" + index, boostTag);
            index++;
        }

        Services.PLATFORM.getPersistentData(player).put("PlayerStatsBoosts", tag);
    }

    public static void loadBoostsFromPlayer(Player player) {
        CompoundTag tag = Services.PLATFORM.getPersistentData(player).getCompound("PlayerStatsBoosts");
        if (tag == null || tag.isEmpty()) return;

        UUID playerId = player.getUUID();
        List<BoostInstance> boosts = new ArrayList<>();

        for (String key : tag.getAllKeys()) {
            CompoundTag boostTag = tag.getCompound(key);
            String attrKey = boostTag.getString("Attribute");
            UUID modId = boostTag.hasUUID("ModifierId") ? boostTag.getUUID("ModifierId") : null;
            int ticks = boostTag.getInt("TicksRemaining");

            Attribute attribute = BuiltInRegistries.ATTRIBUTE.getOptional(ResourceLocation.tryParse(attrKey)).orElse(null);
            if (attribute != null) {
                if (modId == null) {
                    modId = UUID.nameUUIDFromBytes(("playerstats_boost:" + attribute.getDescriptionId()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
                double amount = boostTag.getDouble("Amount");
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance != null) {
                    instance.removeModifier(modId);
                    instance.addTransientModifier(new AttributeModifier(modId, "Scroll Boost", amount, AttributeModifier.Operation.ADDITION));
                    boosts.add(new BoostInstance(attribute, modId, ticks, amount));
                }
            }
        }

        if (!boosts.isEmpty()) {
            activeBoosts.put(playerId, boosts);
        }

        sendBoostsToClient(player);
    }

    public static void handlePlayerLoggedOut(Player player) {
        saveBoostsToPlayer(player);
        activeBoosts.remove(player.getUUID());
    }

    public static void handlePlayerLoggedIn(Player player) {
        loadBoostsFromPlayer(player);
    }

    public static void handlePlayerDeath(Player player) {
        if (!(player instanceof ServerPlayer)) return;

        CompoundTag data = Services.PLATFORM.getPersistentData(player);
        data.remove("PlayerStatsBoosts");

        activeBoosts.remove(player.getUUID());
        sendBoostsToClient(player);
    }

    public static void sendBoostsToClient(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        UUID playerId = serverPlayer.getUUID();
        List<BoostInstance> boosts = activeBoosts.get(playerId);
        if (boosts == null || boosts.isEmpty()) {
            PacketHandler.sendToClient(serverPlayer, new BoostsSyncPacket(Map.of()));
            return;
        }

        Map<ResourceLocation, BoostsSyncPacket.BoostData> map = boosts.stream()
                .collect(Collectors.toMap(
                        boost -> BuiltInRegistries.ATTRIBUTE.getKey(boost.attribute),
                        boost -> new BoostsSyncPacket.BoostData(
                                boost.amount,
                                Math.max(0, boost.ticksRemaining / 20)
                        )
                ));

        PacketHandler.sendToClient(serverPlayer, new BoostsSyncPacket(map));
    }
}
