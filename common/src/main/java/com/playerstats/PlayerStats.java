package com.playerstats;

import com.playerstats.items.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class PlayerStats {
    public static final String MODID = "playerstats";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static void init() {
        makeAttributesSyncable();
    }

    public static void makeAttributesSyncable() {
        Attributes.ATTACK_DAMAGE.setSyncable(true);
        Attributes.ATTACK_KNOCKBACK.setSyncable(true);
        Attributes.KNOCKBACK_RESISTANCE.setSyncable(true);
    }

    public static void modifyLootTable(ResourceLocation name, Consumer<LootPool> poolConsumer) {
        if (name != null && name.getNamespace().equals("minecraft") && name.getPath().startsWith("chests/")) {
            LootPool pool1 = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.UPGRADE_RUNE.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.15f))
                            .setWeight(1)
                            .setQuality(1))
                    .add(LootItem.lootTableItem(ModItems.ABILITY_CRYSTAL.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.10f))
                            .setWeight(1)
                            .setQuality(1))
                    .build();

            LootPool pool2 = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.ATTRIBUTE_BOOST_SCROLL.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.12f))
                            .setWeight(1)
                            .setQuality(1))
                    .build();

            poolConsumer.accept(pool1);
            poolConsumer.accept(pool2);

            boolean isTargetChest =
                    name.getPath().equals("chests/end_city_treasure") ||
                            name.getPath().startsWith("chests/bastion_") ||
                            name.getPath().equals("chests/ancient_city") ||
                            name.getPath().equals("chests/ancient_city_ice_box");

            if (isTargetChest) {
                LootPool pool3 = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.SOUL_STONE.get())
                                .when(LootItemRandomChanceCondition.randomChance(0.08f))
                                .setWeight(1)
                                .setQuality(1))
                        .build();
                poolConsumer.accept(pool3);
            }
        }
    }
}
