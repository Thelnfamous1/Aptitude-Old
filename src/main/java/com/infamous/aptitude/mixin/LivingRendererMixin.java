package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.entity.IAgeable;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingRenderer.class)
public abstract class LivingRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements IEntityRenderer<T, M> {

    private float adultShadowRadius;
    private float babyShadowRadius;

    protected LivingRendererMixin(EntityRendererManager p_i46179_1_) {
        super(p_i46179_1_);
    }
    @Inject(at = @At("RETURN"), method = "<init>")
    private void onConstructed(EntityRendererManager p_i50965_1_, M p_i50965_2_, float p_i50965_3_, CallbackInfo ci){
        this.adultShadowRadius = this.shadowRadius;
        this.babyShadowRadius = this.shadowRadius / 2;
    }

    /*
    For some silly reason, I wasn't able to get this to work by overriding scale in a DolphinRenderer mixin
     */
    @Inject(at = @At("RETURN"), method = "scale")
    private void handleScale(T living, MatrixStack matrixStack, float p_225620_3_, CallbackInfo ci){
        if(living instanceof IAgeable){
            if(living.isBaby()){
                float babyScale = 0.5F;
                matrixStack.scale(babyScale, babyScale, babyScale);
                this.shadowRadius  = this.babyShadowRadius;
            } else {
                this.shadowRadius = this.adultShadowRadius;
            }
        }
    }
}
