package com.playerstats.client.model;


import com.playerstats.entity.KickHorseEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KickHorseModel extends GeoModel<KickHorseEntity> {
    @Override
    public ResourceLocation getModelResource(KickHorseEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("playerstats", "geo/horse_kick.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KickHorseEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("playerstats", "textures/entity/horse_kick.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KickHorseEntity animatable) {
        return new ResourceLocation("playerstats", "animations/horse_kick.animation.json");
    }
}
