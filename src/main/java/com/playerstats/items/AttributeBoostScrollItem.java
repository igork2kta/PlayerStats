//package com.playerstats.items;
//
//import com.playerstats.Config;
//import com.playerstats.network.PacketHandler;
//import com.playerstats.util.AttributeUtils;
//import net.minecraft.ChatFormatting;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResultHolder;
//import net.minecraft.world.entity.ai.attributes.Attribute;
//import net.minecraft.world.entity.ai.attributes.AttributeInstance;
//import net.minecraft.world.entity.ai.attributes.AttributeModifier;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.TooltipFlag;
//import net.minecraft.world.level.Level;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
//import net.neoforged.neoforge.event.entity.player.PlayerEvent;
//import net.neoforged.neoforge.event.tick.PlayerTickEvent;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class AttributeBoostScrollItem extends Item {
//
//    private static final Map<UUID, List<BoostInstance>> activeBoosts = new HashMap<>();
//
//    public AttributeBoostScrollItem(Properties properties) {
//        super(properties);
//    }
//
//    public static class BoostInstance {
//        public final Attribute attribute;
//        public final UUID modifierId;
//        public int ticksRemaining;
//        public double amount;
//
//        public BoostInstance(Attribute attribute, UUID modifierId, int ticksRemaining, double amount) {
//            this.attribute = attribute;
//            this.modifierId = modifierId;
//            this.ticksRemaining = ticksRemaining;
//            this.amount = amount;
//        }
//    }
//
//    @Override
//    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
//        ItemStack stack = player.getItemInHand(hand);
//
//        if (!level.isClientSide) {
//            Random random = new Random();
//            var tag = stack.getTag();
//
//            if (!tag.contains("DefinedAttribute")) {
//                List<Attribute> filteredAttributes = AttributeUtils.getAttributes(player, "");
//                if (!filteredAttributes.isEmpty()) {
//                    Attribute selected = filteredAttributes.get(random.nextInt(filteredAttributes.size()));
//                    tag.putString("DefinedAttribute", BuiltInRegistries.ATTRIBUTE.getKey(selected).toString());
//
//                    int minMultiplier = Config.BOOST_AMOUNT_MIN_MULTIPLIER.get();
//                    int maxMultiplier = Config.BOOST_AMOUNT_MAX_MULTIPLIER.get();
//                    double baseIncrement = AttributeUtils.getIncrement(selected.getDescriptionId());
//                    int multiplier = minMultiplier + random.nextInt(maxMultiplier - minMultiplier + 1);
//                    double amount = baseIncrement * multiplier;
//
//                    int minMinutes = Config.BOOST_DURATION_MIN_MINUTES.get();
//                    int maxMinutes = Config.BOOST_DURATION_MAX_MINUTES.get();
//                    int minTicks = minMinutes * 60 * 20;
//                    int maxTicks = maxMinutes * 60 * 20;
//                    int duration = minTicks + random.nextInt(maxTicks - minTicks + 1);
//
//                    tag.putDouble("BoostAmount", amount);
//                    tag.putInt("BoostDuration", duration);
//                }
//            } else {
//                String attrKey = tag.getString("DefinedAttribute");
//                Attribute attribute = BuiltInRegistries.ATTRIBUTE
//                        .getOptional(ResourceLocation.tryParse(attrKey))
//                        .orElse(null);
//
//                if (attribute != null) {
//                    UUID modifierId = UUID.randomUUID();
//                    double amount = tag.getDouble("BoostAmount");
//                    int duration = tag.getInt("BoostDuration");
//                    applyBoost(player, attribute, modifierId, amount, duration);
//                }
//
//                if (!player.isCreative()) stack.shrink(1);
//            }
//        }
//
//        return InteractionResultHolder.success(stack);
//    }
//
//    private void applyBoost(Player player, Attribute attribute, UUID id, double amount, int duration) {
//        AttributeInstance instance = AttributeUtils.getAttributeInstance(player, attribute);
//        if (instance == null) return;
//
//        UUID playerId = player.getUUID();
//        List<BoostInstance> boosts = activeBoosts.computeIfAbsent(playerId, k -> new ArrayList<>());
//
//        for (BoostInstance existing : boosts) {
//            if (existing.attribute.equals(attribute)) {
//                existing.ticksRemaining = duration;
//                existing.amount = amount;
//                return;
//            }
//        }
//
//        // Cria um ResourceLocation único para esse atributo
//        ResourceLocation modifierId =  ResourceLocation.fromNamespaceAndPath("playerstats_scroll_boost", attribute.getDescriptionId());
//
//        //Remove o atributo se ja estiver ativo
//        instance.removeModifier(modifierId);
//
//        AttributeModifier modifier = new AttributeModifier(
//
//                modifierId,
//                amount,
//                AttributeModifier.Operation.ADD_VALUE
//        );
//
//        instance.addTransientModifier(modifier);
//        boosts.add(new BoostInstance(attribute, id, duration, amount));
//    }
//
//    @Override
//    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
//        CompoundTag tag = stack.getTags();
//        if (tag != null && tag.contains("DefinedAttribute")) {
//            Attribute attribute = BuiltInRegistries.ATTRIBUTE
//                    .getOptional(ResourceLocation.tryParse(tag.getString("DefinedAttribute")))
//                    .orElse(null);
//            if (attribute != null) {
//                double amount = tag.getDouble("BoostAmount");
//                int durationSec = tag.getInt("BoostDuration") / 20;
//
//                tooltip.add(Component.translatable(
//                        "item.playerstats.attribute_boost_scroll.hover_text_unveiled",
//                        AttributeUtils.getAttributeName(attribute)
//                ).withStyle(ChatFormatting.AQUA));
//
//                tooltip.add(Component.translatable(
//                        "item.playerstats.attribute_boost_scroll.hover_text_values",
//                        amount,
//                        durationSec
//                ).withStyle(ChatFormatting.GREEN));
//            }
//        } else {
//            tooltip.add(Component.translatable("item.playerstats.attribute_boost_scroll.hover_text")
//                    .withStyle(ChatFormatting.GRAY));
//        }
//    }
//
//    @SubscribeEvent
//    public static void onPlayerTick(PlayerTickEvent event) {
//        if (event.player.level().isClientSide) return;
//
//        UUID playerId = event.player.getUUID();
//        List<BoostInstance> boosts = activeBoosts.get(playerId);
//        if (boosts == null || boosts.isEmpty()) return;
//
//        Iterator<BoostInstance> iterator = boosts.iterator();
//        while (iterator.hasNext()) {
//            BoostInstance boost = iterator.next();
//            boost.ticksRemaining--;
//            if (boost.ticksRemaining <= 0) {
//                AttributeInstance instance = event.player.getAttribute(boost.attribute);
//                if (instance != null) instance.removeModifier(boost.modifierId);
//                iterator.remove();
//                sendBoostsToClient(event.player);
//            }
//        }
//
//        if (boosts.isEmpty()) activeBoosts.remove(playerId);
//    }
//
//    @SubscribeEvent
//    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
//        saveBoostsToPlayer(event.getEntity());
//    }
//
//    @SubscribeEvent
//    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
//        loadBoostsFromPlayer(event.getEntity());
//    }
//
//    @SubscribeEvent
//    public static void onPlayerDeath(LivingDeathEvent event) {
//        if (!(event.getEntity() instanceof ServerPlayer player)) return;
//        player.getPersistentData().remove("PlayerStatsBoosts");
//        activeBoosts.remove(player.getUUID());
//    }
//
//    public static void sendBoostsToClient(Player player) {
//        UUID playerId = player.getUUID();
//        List<BoostInstance> boosts = activeBoosts.get(playerId);
//        if (boosts == null || boosts.isEmpty()) {
//            PacketHandler.sendToClient(new com.playerstats.network.BoostsSyncPacket(Map.of()), (ServerPlayer) player);
//            return;
//        }
//
//        Map<ResourceLocation, com.playerstats.network.BoostsSyncPacket.BoostData> map = boosts.stream()
//                .collect(Collectors.toMap(
//                        boost -> BuiltInRegistries.ATTRIBUTE.getKey(boost.attribute),
//                        boost -> new com.playerstats.network.BoostsSyncPacket.BoostData(boost.amount, boost.ticksRemaining / 20)
//                ));
//
//        PacketHandler.sendToClient(new com.playerstats.network.BoostsSyncPacket(map), (ServerPlayer) player);
//    }
//
//    public static void saveBoostsToPlayer(Player player) {
//        UUID playerId = player.getUUID();
//        List<BoostInstance> boosts = activeBoosts.get(playerId);
//        if (boosts == null || boosts.isEmpty()) return;
//
//        var tag = new CompoundTag();
//        for (int i = 0; i < boosts.size(); i++) {
//            BoostInstance boost = boosts.get(i);
//            var boostTag = new CompoundTag();
//            boostTag.putString("Attribute", BuiltInRegistries.ATTRIBUTE.getKey(boost.attribute).toString());
//            boostTag.putUUID("ModifierId", boost.modifierId);
//            boostTag.putInt("TicksRemaining", boost.ticksRemaining);
//            boostTag.putDouble("Amount", boost.amount);
//            tag.put("Boost" + i, boostTag);
//        }
//
//        player.getPersistentData().put("PlayerStatsBoosts", tag);
//    }
//
//    public static void loadBoostsFromPlayer(Player player) {
//        var tag = player.getPersistentData().getCompound("PlayerStatsBoosts");
//        if (tag == null || tag.isEmpty()) return;
//
//        UUID playerId = player.getUUID();
//        List<BoostInstance> boosts = new ArrayList<>();
//
//        for (String key : tag.getAllKeys()) {
//            var boostTag = tag.getCompound(key);
//            String attrKey = boostTag.getString("Attribute");
//            UUID modId = boostTag.getUUID("ModifierId");
//            int ticks = boostTag.getInt("TicksRemaining");
//            double amount = boostTag.getDouble("Amount");
//
//            Attribute attribute = BuiltInRegistries.ATTRIBUTE.getOptional(new ResourceLocation(attrKey)).orElse(null);
//            if (attribute != null) {
//                AttributeInstance instance = player.getAttribute(attribute);
//                if (instance != null) {
//                    instance.addTransientModifier(new AttributeModifier(modId, "Scroll Boost", amount, AttributeModifier.Operation.ADDITION));
//                    boosts.add(new BoostInstance(attribute, modId, ticks, amount));
//                }
//            }
//        }
//
//        if (!boosts.isEmpty()) activeBoosts.put(playerId, boosts);
//    }
//}
