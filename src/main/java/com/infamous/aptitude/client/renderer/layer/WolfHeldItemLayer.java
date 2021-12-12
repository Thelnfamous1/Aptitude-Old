package com.infamous.aptitude.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.WolfModel;
import net.minecraft.world.entity.animal.Wolf;
import com.mojang.math.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class WolfHeldItemLayer<T extends Wolf, M extends WolfModel<T>> extends AgeableHeldItemLayer<T, M> {

    public WolfHeldItemLayer(RenderLayerParent<T, M> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    protected void translateForBaby(PoseStack matrixStack) {
        matrixStack.translate(0.0D, 0.65D, 0.0D);
    }

    @Override
    protected void poseItem(PoseStack matrixStack, T living, float netHeadYaw, float headPitch, float partialTicks) {
        float headRollAngle = living.getHeadRollAngle(partialTicks);
        matrixStack.mulPose(Vector3f.ZP.rotation(headRollAngle));
        super.poseItem(matrixStack, living, netHeadYaw, headPitch, partialTicks);
    }

    @Override
    protected void translateItem(PoseStack matrixStack, T living) {
        matrixStack.translate(0.06D, 0.15D, -0.42D);
    }
}