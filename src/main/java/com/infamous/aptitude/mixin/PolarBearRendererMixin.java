package com.infamous.aptitude.mixin;

import com.infamous.aptitude.client.renderer.layer.QuadrupedHeldItemLayer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.PolarBearRenderer;
import net.minecraft.client.renderer.entity.model.PolarBearModel;
import net.minecraft.entity.passive.PolarBearEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PolarBearRenderer.class)
public abstract class PolarBearRendererMixin extends MobRenderer<PolarBearEntity, PolarBearModel<PolarBearEntity>> {

    public PolarBearRendererMixin(EntityRendererManager p_i50961_1_, PolarBearModel<PolarBearEntity> p_i50961_2_, float p_i50961_3_) {
        super(p_i50961_1_, p_i50961_2_, p_i50961_3_);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void onConstructed(EntityRendererManager entityRendererManager, CallbackInfo ci){
        this.addLayer(new QuadrupedHeldItemLayer<>(this));
    }
}
