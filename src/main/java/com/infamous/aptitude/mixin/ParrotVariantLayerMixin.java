package com.infamous.aptitude.mixin;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.entity.IAgeable;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.layers.ParrotVariantLayer;
import net.minecraft.client.renderer.entity.model.ParrotModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParrotVariantLayer.class)
public abstract class ParrotVariantLayerMixin<T extends PlayerEntity> extends LayerRenderer<T, PlayerModel<T>> {
    private static boolean notifyRenderOverwrite;

    @Final
    @Shadow
    private ParrotModel model;

    public ParrotVariantLayerMixin(IEntityRenderer<T, PlayerModel<T>> entityRenderer) {
        super(entityRenderer);
    }

    @Inject(at = @At(value = "HEAD"),
            method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/entity/player/PlayerEntity;FFFFZ)V",
            cancellable = true)
    private void scaleForBaby(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int p_229136_3_, T player, float p_229136_5_, float p_229136_6_, float p_229136_7_, float p_229136_8_, boolean leftShoulder, CallbackInfo ci){
        CompoundNBT compoundnbt = leftShoulder ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
        EntityType.byString(compoundnbt.getString("id")).filter((entityType) -> entityType == EntityType.PARROT).ifPresent((p_229137_11_) -> {
            matrixStack.pushPose();
            boolean isBaby = compoundnbt.getInt("Age") < IAgeable.ADULT_AGE;
            if(isBaby){
                float babyScale = 0.5F;
                matrixStack.scale(babyScale, babyScale, babyScale);
            }
            float xShiftFactor = isBaby ? 2.0F : 1.0F;
            float additionalYShift = isBaby ? 0.2F : 0.0F;
            matrixStack.translate(leftShoulder ? (double)0.4F * xShiftFactor : (double)-0.4F * xShiftFactor, player.isCrouching() ? (double)-1.3F + additionalYShift : -1.5D, 0.0D);
            IVertexBuilder ivertexbuilder = renderTypeBuffer.getBuffer(this.model.renderType(ParrotRenderer.PARROT_LOCATIONS[compoundnbt.getInt("Variant")]));
            this.model.renderOnShoulder(matrixStack, ivertexbuilder, p_229136_3_, OverlayTexture.NO_OVERLAY, p_229136_5_, p_229136_6_, p_229136_7_, p_229136_8_, player.tickCount);
            matrixStack.popPose();
        });

        if(!notifyRenderOverwrite){
            Aptitude.LOGGER.debug("Notice: Silently overwrote ParrotVariantLayerMixin#render in order to scale baby parrots on shoulders");
            notifyRenderOverwrite = true;
        }

        ci.cancel();
    }
}
