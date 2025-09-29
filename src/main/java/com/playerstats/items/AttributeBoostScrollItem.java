package com.playerstats.items;

import com.playerstats.Config;
import com.playerstats.network.BoostsSyncPacket;
import com.playerstats.network.PacketHandler;
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
import java.util.stream.Collectors;

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
        public double amount;  // novo campo

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

            // Primeiro uso: definir atributo, incremento e tempo
            if (!tag.contains("DefinedAttribute")) {
                List<Attribute> filteredAttributes = AttributeUtils.getAttributes(player, "");

                if (!filteredAttributes.isEmpty()) {
                    Attribute selected = filteredAttributes.get(random.nextInt(filteredAttributes.size()));
                    tag.putString("DefinedAttribute", BuiltInRegistries.ATTRIBUTE.getKey(selected).toString());

                    int minMultiplier = Config.BOOST_AMOUNT_MIN_MULTIPLIER.get();
                    int maxMultiplier = Config.BOOST_AMOUNT_MAX_MULTIPLIER.get();
                    double baseIncrement = AttributeUtils.getIncrement(selected.getDescriptionId());
                    int multiplier = minMultiplier + random.nextInt(maxMultiplier - minMultiplier + 1);
                    double amount = baseIncrement * multiplier;

                    int minMinutes = Config.BOOST_DURATION_MIN_MINUTES.get();
                    int maxMinutes = Config.BOOST_DURATION_MAX_MINUTES.get();
                    int minTicks = minMinutes * 60 * 20;
                    int maxTicks = maxMinutes * 60 * 20;
                    int duration = minTicks + random.nextInt(maxTicks - minTicks + 1);

                    //Limitar a 2 casas decimais
                    tag.putDouble("BoostAmount", Math.round(amount * 100.0) / 100.0);
                    tag.putInt("BoostDuration", duration);
                }
            }
            // Segundo uso: aplicar boost já salvo
            else {
                String attrKey = tag.getString("DefinedAttribute");
                Attribute attribute = BuiltInRegistries.ATTRIBUTE
                        .getOptional(ResourceLocation.tryParse(attrKey))
                        .orElse(null);

                if (attribute != null) {
                    UUID modifierId = UUID.randomUUID();
                    double amount = tag.getDouble("BoostAmount");
                    int duration = tag.getInt("BoostDuration");

                    applyBoost(player, attribute, modifierId, amount, duration);
                }

                // Consumir o item
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

        // Verifica se já existe boost para o mesmo atributo
        for (BoostInstance existing : boosts) {
            if (existing.attribute.equals(attribute)) {
                // Apenas renova o tempo
                existing.ticksRemaining = duration;
                existing.amount = amount;
                sendBoostsToClient(player);
                return;
            }
        }

        // Se não existir, cria um novo
        instance.removeModifier(id); // segurança
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
                sendBoostsToClient(event.player);
            }
        }

        // Remove do mapa se não tiver mais boosts
        if (boosts.isEmpty()) {
            activeBoosts.remove(playerId);
        }
        else{sendBoostsToClient(event.player);}
    }


    public static void saveBoostsToPlayer(Player player) {
        UUID playerId = player.getUUID();
        List<BoostInstance> boosts = activeBoosts.get(playerId);
        if (boosts == null || boosts.isEmpty()) return;

        var tag = new net.minecraft.nbt.CompoundTag();
        int index = 0;
        for (BoostInstance boost : boosts) {
            var boostTag = new CompoundTag();
            boostTag.putString("Attribute", BuiltInRegistries.ATTRIBUTE.getKey(boost.attribute).toString());
            boostTag.putUUID("ModifierId", boost.modifierId);
            boostTag.putInt("TicksRemaining", boost.ticksRemaining);
            boostTag.putDouble("Amount", boost.amount);  // Salva o amount
            tag.put("Boost" + index, boostTag);
            index++;
        }

        player.getPersistentData().put("PlayerStatsBoosts", tag);

        sendBoostsToClient(player);
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

            Attribute attribute = BuiltInRegistries.ATTRIBUTE.getOptional(ResourceLocation.tryParse(attrKey)).orElse(null);
            if (attribute != null) {
                // reaplica o boost
                double amount = boostTag.getDouble("Amount");
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance != null) {
                    instance.addTransientModifier(new AttributeModifier(modId, "Scroll Boost", amount, AttributeModifier.Operation.ADDITION));
                    boosts.add(new BoostInstance(attribute, modId, ticks, amount)); // passe amount aqui!
                }
            }
        }

        if (!boosts.isEmpty()) {
            activeBoosts.put(playerId, boosts);
        }

        sendBoostsToClient(player);
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
        sendBoostsToClient(player);
    }

    public static void sendBoostsToClient(Player player) {
        UUID playerId = player.getUUID();
        List<BoostInstance> boosts = activeBoosts.get(playerId);
        if (boosts == null || boosts.isEmpty()) {
            // Envia pacote vazio para limpar o cache no cliente
            PacketHandler.sendToClient(new BoostsSyncPacket(Map.of()),(ServerPlayer) player);
            return;
        }

        Map<ResourceLocation, BoostsSyncPacket.BoostData> map = boosts.stream()
                .collect(Collectors.toMap(
                        boost -> BuiltInRegistries.ATTRIBUTE.getKey(boost.attribute),
                        boost -> new BoostsSyncPacket.BoostData(
                                boost.amount,  // usa o amount do boost salvo
                                boost.ticksRemaining / 20
                        )
                ));

        PacketHandler.sendToClient(new BoostsSyncPacket(map), (ServerPlayer) player);
    }
}


