package com.infamous.aptitude.mixin;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.LlamaModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LlamaModel.class)
public abstract class LlamaModelMixin<T extends AbstractChestedHorseEntity> extends EntityModel<T> {

    @Shadow @Final private ModelRenderer head;
    @Shadow @Final private ModelRenderer body;
    @Shadow @Final private ModelRenderer leg0;
    @Shadow @Final private ModelRenderer leg1;
    @Shadow @Final private ModelRenderer leg2;
    @Shadow @Final private ModelRenderer leg3;


    @Inject(at = @At("RETURN"), method = "setupAnim")
    private void handleAnimate(T llama, float animationPosition, float lerpAnimationSpeed, float bob, float headBodyDiff, float lerpXRot, CallbackInfo ci){
        float partialTicks = undoLerpXRotToGetPartialTicks(llama, lerpXRot);
        this.animate(llama, animationPosition, lerpAnimationSpeed, partialTicks);
    }

    private void animate(T llama, float animationPosition, float lerpAnimationSpeed, float partialTicks) {
        float lerpYBodyRot = MathHelper.rotlerp(llama.yBodyRotO, llama.yBodyRot, partialTicks);
        float lerpYHeadRot = MathHelper.rotlerp(llama.yHeadRotO, llama.yHeadRot, partialTicks);
        float lerpXRot = MathHelper.lerp(partialTicks, llama.xRotO, llama.xRot);

        float headBodyDiff = lerpYHeadRot - lerpYBodyRot;
        if (headBodyDiff > 20.0F) {
            headBodyDiff = 20.0F;
        }

        if (headBodyDiff < -20.0F) {
            headBodyDiff = -20.0F;
        }

        float xDegrees = lerpXRot * ((float)Math.PI / 180F);
        if (lerpAnimationSpeed > 0.2F) {
            xDegrees += MathHelper.cos(animationPosition * 0.4F) * 0.15F * lerpAnimationSpeed;
        }

        float eatAnim = llama.getEatAnim(partialTicks);
        float standAnim = llama.getStandAnim(partialTicks);
        float remainingStandAnim = 1.0F - standAnim;
        float mouthAnim = llama.getMouthAnim(partialTicks);
        float lerpTickCount = (float)llama.tickCount + partialTicks;

        this.head.y = 4.0F;
        this.head.z = -12.0F;
        this.head.xRot = ((float)Math.PI / 6F) + xDegrees;
        this.head.yRot = headBodyDiff * ((float)Math.PI / 180F);
        float f13 = (1.0F - Math.max(standAnim, eatAnim)) * (((float)Math.PI / 6F) + xDegrees + mouthAnim * MathHelper.sin(lerpTickCount) * 0.05F);
        this.head.xRot = standAnim * (0.2617994F + xDegrees) + eatAnim * (2.1816616F + MathHelper.sin(lerpTickCount) * 0.05F) + f13;
        this.head.yRot = standAnim * headBodyDiff * ((float)Math.PI / 180F) + (1.0F - Math.max(standAnim, eatAnim)) * this.head.yRot;
        this.head.y = standAnim * -4.0F + eatAnim * 11.0F + (1.0F - Math.max(standAnim, eatAnim)) * this.head.y;
        this.head.z = standAnim * -4.0F + eatAnim * -12.0F + (1.0F - Math.max(standAnim, eatAnim)) * this.head.z;

        this.body.xRot = 0.0F;
        this.body.xRot = standAnim * (-(float)Math.PI / 4F) + remainingStandAnim * this.body.xRot;

        this.leg2.y = 2.0F * standAnim + 14.0F * remainingStandAnim;
        this.leg2.z = -6.0F * standAnim - 10.0F * remainingStandAnim;
        this.leg3.y = this.leg2.y;
        this.leg3.z = this.leg2.z;

        float mobilityFactor = llama.isInWater() ? 0.2F : 1.0F;
        float f11 = MathHelper.cos(mobilityFactor * animationPosition * 0.6662F + (float)Math.PI);
        float f12 = f11 * 0.8F * lerpAnimationSpeed;
        float backLegStandAnim = 0.2617994F * standAnim;
        float f15 = MathHelper.cos(lerpTickCount * 0.6F + (float)Math.PI);
        float f16 = ((-(float)Math.PI / 3F) + f15) * standAnim + f12 * remainingStandAnim;
        float f17 = ((-(float)Math.PI / 3F) - f15) * standAnim - f12 * remainingStandAnim;
        this.leg0.xRot = backLegStandAnim - f11 * 0.5F * lerpAnimationSpeed * remainingStandAnim;
        this.leg1.xRot = backLegStandAnim + f11 * 0.5F * lerpAnimationSpeed * remainingStandAnim;
        this.leg2.xRot = f16;
        this.leg3.xRot = f17;
    }

    private static float undoLerpXRotToGetPartialTicks(AbstractChestedHorseEntity llama, float lerpXRot) {
        return (lerpXRot - llama.xRotO) / (llama.xRot - llama.xRotO);
    }
}
