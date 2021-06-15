package com.infamous.aptitude.client.renderer.layer;

import com.infamous.aptitude.client.renderer.IHeadAccessor;
import com.infamous.aptitude.common.entity.IDevourer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AgeableHeldItemLayer<T extends MobEntity, M extends AgeableModel<T>> extends LayerRenderer<T, M> {

   public AgeableHeldItemLayer(IEntityRenderer<T, M> entityRenderer) {
      super(entityRenderer);
   }

   public void render(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int p_225628_3_, T mob, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
      boolean baby = mob.isBaby();
      matrixStack.pushPose();
      if (baby) {
         this.scaleAndTranslateForBaby(matrixStack);
      }

      this.translateToHead(matrixStack);
      this.poseItem(matrixStack, mob, netHeadYaw, headPitch);

      EquipmentSlotType slotType = mob instanceof IDevourer ? ((IDevourer) mob).getSlotForFood() : EquipmentSlotType.MAINHAND;
      ItemStack itemBySlot = mob.getItemBySlot(slotType);
      Minecraft.getInstance().getItemInHandRenderer().renderItem(mob, itemBySlot, ItemCameraTransforms.TransformType.GROUND, false, matrixStack, renderTypeBuffer, p_225628_3_);
      matrixStack.popPose();
   }

   protected void scaleAndTranslateForBaby(MatrixStack matrixStack) {
      float babyScale = 0.75F;
      matrixStack.scale(babyScale, babyScale, babyScale);
      double babyTranslateX = 0.0D;
      double babyTranslateY = 0.5D;
      double babyTranslateZ = 0.209375F;
      matrixStack.translate(babyTranslateX, babyTranslateY, babyTranslateZ);
   }

   protected void translateToHead(MatrixStack matrixStack) {
      M parentModel = this.getParentModel();
      if(parentModel instanceof IHeadAccessor){
         IHeadAccessor headAccessor = (IHeadAccessor) parentModel;
         if(headAccessor.getHead() != null){
            matrixStack.translate((double)(headAccessor.getHead().x / 16.0F), (double)(headAccessor.getHead().y / 16.0F), (double)(headAccessor.getHead().z / 16.0F));
         }
      }
   }

   protected void poseItem(MatrixStack matrixStack, T mob, float netHeadYaw, float headPitch) {
      boolean sleeping = mob.isSleeping();
      //float headRollAngle = mob.getHeadRollAngle(partialTicks);
      //matrixStack.mulPose(Vector3f.ZP.rotation(headRollAngle));
      matrixStack.mulPose(Vector3f.YP.rotationDegrees(netHeadYaw));
      matrixStack.mulPose(Vector3f.XP.rotationDegrees(headPitch));

      this.translateItem(matrixStack, mob);

      matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
      if (sleeping) {
         matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
      }
   }

   protected void translateItem(MatrixStack matrixStack, T mob) {
      boolean baby = mob.isBaby();
      boolean sleeping = mob.isSleeping();

      double normalX = 0.06F;
      double anyY = 0.26F;
      double normalZ = -0.5D;
      if (baby) {
         if (sleeping) {
            double babySleepX = 0.4F;
            double babySleepZ = 0.15F;
            matrixStack.translate(babySleepX, anyY, babySleepZ);
         } else {
            matrixStack.translate(normalX, anyY, normalZ);
         }
      } else if (sleeping) {
         double adultSleepX = 0.46F;
         double adultSleepZ = 0.22F;
         matrixStack.translate(adultSleepX, anyY, adultSleepZ);
      } else {
         double adultNormalY = 0.27F;
         matrixStack.translate(normalX, adultNormalY, normalZ);
      }
   }
}