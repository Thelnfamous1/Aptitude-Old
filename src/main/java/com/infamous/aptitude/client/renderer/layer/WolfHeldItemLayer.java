package com.infamous.aptitude.client.renderer.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.WolfModel;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class WolfHeldItemLayer<T extends WolfEntity, M extends WolfModel<T>> extends AgeableHeldItemLayer<T, M> {

    public WolfHeldItemLayer(IEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    protected void translateForBaby(MatrixStack matrixStack) {
        matrixStack.translate(0.0D, 0.65D, 0.0D);
    }

    @Override
    protected void poseItem(MatrixStack matrixStack, T living, float netHeadYaw, float headPitch, float partialTicks) {
        float headRollAngle = living.getHeadRollAngle(partialTicks);
        matrixStack.mulPose(Vector3f.ZP.rotation(headRollAngle));
        super.poseItem(matrixStack, living, netHeadYaw, headPitch, partialTicks);
    }

    @Override
    protected void translateItem(MatrixStack matrixStack, T living) {
        matrixStack.translate(0.06D, 0.15D, -0.42D);
    }
}