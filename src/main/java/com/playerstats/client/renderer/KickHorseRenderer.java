package com.playerstats.client.renderer;

import com.playerstats.client.model.KickHorseModel;
import com.playerstats.entity.KickHorseEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KickHorseRenderer extends GeoEntityRenderer<KickHorseEntity> {
    public KickHorseRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KickHorseModel());
        this.shadowRadius = 0.8f; // sombra do cavalo (ajuste se quiser maior/menor)
    }
}