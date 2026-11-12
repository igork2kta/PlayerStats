package com.playerstats;

import com.playerstats.platform.Services;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ModAttributes {

    public static final Supplier<Attribute> FOLLOW_OWNER =
            Services.REGISTRY.registerAttribute("follow_owner",
                    () -> new RangedAttribute("attribute.playerstats.follow_owner", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final Supplier<Attribute> TELEPORT_TO_OWNER =
            Services.REGISTRY.registerAttribute("teleport_to_owner",
                    () -> new RangedAttribute("attribute.playerstats.teleport_to_owner", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final Supplier<Attribute> LIFE_REGEN =
            Services.REGISTRY.registerAttribute("life_regen",
                    () -> new RangedAttribute("attribute.playerstats.life_regen", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final Supplier<Attribute> FROST_WALKER =
            Services.REGISTRY.registerAttribute("frost_walker",
                    () -> new RangedAttribute("attribute.playerstats.frost_walker", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final Supplier<Attribute> DEFEND_OWNER =
            Services.REGISTRY.registerAttribute("defend_owner",
                    () -> new RangedAttribute("attribute.playerstats.defend_owner", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final Supplier<Attribute> REBIRTH =
            Services.REGISTRY.registerAttribute("rebirth",
                    () -> new RangedAttribute("attribute.playerstats.rebirth", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final Supplier<Attribute> TAUNT =
            Services.REGISTRY.registerAttribute("taunt",
                    () -> new RangedAttribute("attribute.playerstats.taunt", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final Supplier<Attribute> SCOUT_VISION =
            Services.REGISTRY.registerAttribute("scout_vision",
                    () -> new RangedAttribute("attribute.playerstats.scout_vision", -1.0D, -1.0D, 3.0D)
                            .setSyncable(true));

    public static final Supplier<Attribute> HOWL_BUFF =
            Services.REGISTRY.registerAttribute("howl_buff",
                    () -> new RangedAttribute("attribute.playerstats.howl_buff", -1.0D, -1.0D, 3.0D)
                            .setSyncable(true));

    public static final Supplier<Attribute> PATROL =
            Services.REGISTRY.registerAttribute("patrol",
                    () -> new RangedAttribute("attribute.playerstats.patrol", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final Supplier<Attribute> MILKY_AURA =
            Services.REGISTRY.registerAttribute("milky_aura",
                    () -> new RangedAttribute("attribute.playerstats.milky_aura", -1.0D, -1.0D, 3.0D)
                            .setSyncable(true));

    public static final Supplier<Attribute> INFINITE_ICE =
            Services.REGISTRY.registerAttribute("infinite_ice",
                    () -> new RangedAttribute("attribute.playerstats.infinite_ice", -1.0D, -1.0D, 1.0D)
                            .setSyncable(true));

    public static final Supplier<Attribute> VAMPIRISM =
            Services.REGISTRY.registerAttribute("vampirism",
                    () -> new RangedAttribute("attribute.playerstats.vampirism", -1.0D, -1.0D, 5.0D)
                            .setSyncable(true));

    public static void applyEntityAttributes(EntityType<? extends LivingEntity> type, BiConsumer<EntityType<? extends LivingEntity>, Attribute> consumer) {
        if (type.getCategory() == MobCategory.CREATURE || type == EntityType.PLAYER) {
            consumer.accept(type, REBIRTH.get());
        }

        if (type == EntityType.PLAYER) {
            consumer.accept(type, INFINITE_ICE.get());
            consumer.accept(type, VAMPIRISM.get());
        }

        if (type != EntityType.PLAYER) {
            consumer.accept(type, LIFE_REGEN.get());
        }

        if (type == EntityType.HORSE || type == EntityType.DONKEY || type == EntityType.MULE || type == EntityType.CAMEL) {
            consumer.accept(type, FOLLOW_OWNER.get());
            consumer.accept(type, TELEPORT_TO_OWNER.get());
            consumer.accept(type, FROST_WALKER.get());
        }

        if (type == EntityType.IRON_GOLEM) {
            consumer.accept(type, FOLLOW_OWNER.get());
            consumer.accept(type, TELEPORT_TO_OWNER.get());
            consumer.accept(type, DEFEND_OWNER.get());
            consumer.accept(type, TAUNT.get());
            consumer.accept(type, PATROL.get());
        }

        if (type == EntityType.WOLF) {
            consumer.accept(type, TAUNT.get());
            consumer.accept(type, HOWL_BUFF.get());
            consumer.accept(type, PATROL.get());
        }

        if (type == EntityType.PARROT) {
            consumer.accept(type, SCOUT_VISION.get());
        }

        if (type == EntityType.COW || type == EntityType.MOOSHROOM) {
            consumer.accept(type, MILKY_AURA.get());
        }
    }

    public static void init() {
    }
}
