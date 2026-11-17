package com.playerstats;

import com.playerstats.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.function.BiConsumer;

public class ModAttributes {

    public static final Holder<Attribute> FOLLOW_OWNER =
            Services.REGISTRY.registerAttribute("follow_owner",
                    () -> new RangedAttribute("attribute.playerstats.follow_owner", -1.0D, -1.0D, 1.0D).setSyncable(true));

    public static final Holder<Attribute> TELEPORT_TO_OWNER =
            Services.REGISTRY.registerAttribute("teleport_to_owner",
                    () -> new RangedAttribute("attribute.playerstats.teleport_to_owner", -1.0D, -1.0D, 1.0D).setSyncable(true));

    public static final Holder<Attribute> LIFE_REGEN =
            Services.REGISTRY.registerAttribute("life_regen",
                    () -> new RangedAttribute("attribute.playerstats.life_regen", -1.0D, -1.0D, 1.0D).setSyncable(true));

    public static final Holder<Attribute> FROST_WALKER =
            Services.REGISTRY.registerAttribute("frost_walker",
                    () -> new RangedAttribute("attribute.playerstats.frost_walker", -1.0D, -1.0D, 1.0D).setSyncable(true));

    public static final Holder<Attribute> DEFEND_OWNER =
            Services.REGISTRY.registerAttribute("defend_owner",
                    () -> new RangedAttribute("attribute.playerstats.defend_owner", -1.0D, -1.0D, 1.0D).setSyncable(true));

    public static final Holder<Attribute> REBIRTH =
            Services.REGISTRY.registerAttribute("rebirth",
                    () -> new RangedAttribute("attribute.playerstats.rebirth", -1.0D, -1.0D, 1.0D).setSyncable(true));

    public static final Holder<Attribute> TAUNT =
            Services.REGISTRY.registerAttribute("taunt",
                    () -> new RangedAttribute("attribute.playerstats.taunt", -1.0D, -1.0D, 1.0D).setSyncable(true));

    public static final Holder<Attribute> SCOUT_VISION =
            Services.REGISTRY.registerAttribute("scout_vision",
                    () -> new RangedAttribute("attribute.playerstats.scout_vision", -1.0D, -1.0D, 3.0D).setSyncable(true));

    public static final Holder<Attribute> HOWL_BUFF =
            Services.REGISTRY.registerAttribute("howl_buff",
                    () -> new RangedAttribute("attribute.playerstats.howl_buff", -1.0D, -1.0D, 3.0D).setSyncable(true));

    public static final Holder<Attribute> PATROL =
            Services.REGISTRY.registerAttribute("patrol",
                    () -> new RangedAttribute("attribute.playerstats.patrol", -1.0D, -1.0D, 1.0D).setSyncable(true));

    public static final Holder<Attribute> MILKY_AURA =
            Services.REGISTRY.registerAttribute("milky_aura",
                    () -> new RangedAttribute("attribute.playerstats.milky_aura", -1.0D, -1.0D, 3.0D).setSyncable(true));

    public static final Holder<Attribute> INFINITE_ICE =
            Services.REGISTRY.registerAttribute("infinite_ice",
                    () -> new RangedAttribute("attribute.playerstats.infinite_ice", -1.0D, -1.0D, 1.0D).setSyncable(true));

    public static final Holder<Attribute> VAMPIRISM =
            Services.REGISTRY.registerAttribute("vampirism",
                    () -> new RangedAttribute("attribute.playerstats.vampirism", -1.0D, -1.0D, 5.0D).setSyncable(true));

    public static void init() {
    }

    public static void applyEntityAttributes(EntityType<? extends LivingEntity> type, BiConsumer<EntityType<? extends LivingEntity>, Holder<Attribute>> consumer) {
        if (type.getCategory() == MobCategory.CREATURE || type == EntityType.PLAYER) {
            consumer.accept(type, ModAttributes.REBIRTH);
        }

        if (type == EntityType.PLAYER) {
            consumer.accept(type, ModAttributes.INFINITE_ICE);
            consumer.accept(type, ModAttributes.VAMPIRISM);
        }

        if (type != EntityType.PLAYER) {
            consumer.accept(type, ModAttributes.LIFE_REGEN);
        }

        if (type == EntityType.HORSE || type == EntityType.DONKEY || type == EntityType.MULE || type == EntityType.CAMEL) {
            consumer.accept(type, ModAttributes.FOLLOW_OWNER);
            consumer.accept(type, ModAttributes.TELEPORT_TO_OWNER);
            consumer.accept(type, ModAttributes.FROST_WALKER);
        }

        if (type == EntityType.IRON_GOLEM) {
            consumer.accept(type, ModAttributes.FOLLOW_OWNER);
            consumer.accept(type, ModAttributes.TELEPORT_TO_OWNER);
            consumer.accept(type, ModAttributes.DEFEND_OWNER);
            consumer.accept(type, ModAttributes.TAUNT);
            consumer.accept(type, ModAttributes.PATROL);
        }

        if (type == EntityType.WOLF) {
            consumer.accept(type, ModAttributes.TAUNT);
            consumer.accept(type, ModAttributes.HOWL_BUFF);
            consumer.accept(type, ModAttributes.PATROL);
        }

        if (type == EntityType.PARROT) {
            consumer.accept(type, ModAttributes.SCOUT_VISION);
        }

        if (type == EntityType.COW) {
            consumer.accept(type, ModAttributes.MILKY_AURA);
        }
    }
}
