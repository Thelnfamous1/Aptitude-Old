package com.infamous.aptitude.client.renderer.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.renderer.entity.model.WolfModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

/**
 * @author baguchan
 */
public class WolfHeldItemLayer extends LayerRenderer<WolfEntity, WolfModel<WolfEntity>> {
    public WolfHeldItemLayer(IEntityRenderer<WolfEntity, WolfModel<WolfEntity>> wolfRenderer) {
        super(wolfRenderer);
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, WolfEntity wolf, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.getParentModel() instanceof IHasHead) {
            boolean flag = wolf.isSleeping();
            boolean baby = wolf.isBaby();
            matrixStackIn.pushPose();
            if (baby) {
                float babyScale = 0.75F;
                matrixStackIn.scale(babyScale, babyScale, babyScale);
                matrixStackIn.translate(0.0D, 0.65D, 0.0D);
            }

            float headRollAngle = wolf.getHeadRollAngle(partialTicks);
            matrixStackIn.translate((double)(((IHasHead)this.getParentModel()).getHead().x / 16.0F), (double)(((IHasHead)this.getParentModel()).getHead().y / 16.0F), (double)(((IHasHead)this.getParentModel()).getHead().z / 16.0F));
            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(headRollAngle));
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(netHeadYaw));
            matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(headPitch));
            matrixStackIn.translate(0.06D, 0.15D, -0.42D);
            matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            ItemStack itemstack = wolf.getItemBySlot(EquipmentSlotType.MAINHAND);
            Minecraft.getInstance().getItemInHandRenderer().renderItem(wolf, itemstack, ItemCameraTransforms.TransformType.GROUND, false, matrixStackIn, bufferIn, packedLightIn);
            matrixStackIn.popPose();
        }

    }
}