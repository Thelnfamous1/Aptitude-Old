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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AgeableHeldItemLayer<T extends LivingEntity, M extends AgeableModel<T>> extends LayerRenderer<T, M> {

   public AgeableHeldItemLayer(IEntityRenderer<T, M> entityRenderer) {
      super(entityRenderer);
   }

   public void render(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int p_225628_3_, T living, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
      matrixStack.pushPose();
      if (living.isBaby()) {
         this.scaleForBaby(matrixStack);
         this.translateForBaby(matrixStack);
      }

      this.translateToHead(matrixStack);
      this.poseItem(matrixStack, living, netHeadYaw, headPitch, partialTicks);

      EquipmentSlotType slotType = this.getSlotType(living);
      ItemStack itemBySlot = living.getItemBySlot(slotType);
      Minecraft.getInstance().getItemInHandRenderer().renderItem(living, itemBySlot, ItemCameraTransforms.TransformType.GROUND, false, matrixStack, renderTypeBuffer, p_225628_3_);
      matrixStack.popPose();
   }

   protected EquipmentSlotType getSlotType(T living) {
      return living instanceof IDevourer ? ((IDevourer) living).getSlotForFood() : EquipmentSlotType.MAINHAND;
   }

   protected void scaleForBaby(MatrixStack matrixStack) {
      float babyScale = 0.75F;
      matrixStack.scale(babyScale, babyScale, babyScale);
   }

   protected void translateForBaby(MatrixStack matrixStack) {
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

   protected void poseItem(MatrixStack matrixStack, T living, float netHeadYaw, float headPitch, float partialTicks) {
      //float headRollAngle = living.getHeadRollAngle(partialTicks);
      //matrixStack.mulPose(Vector3f.ZP.rotation(headRollAngle));
      matrixStack.mulPose(Vector3f.YP.rotationDegrees(netHeadYaw));
      matrixStack.mulPose(Vector3f.XP.rotationDegrees(headPitch));

      this.translateItem(matrixStack, living);

      matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
      if (living.isSleeping()) {
         matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
      }
   }

   protected void translateItem(MatrixStack matrixStack, T living) {
      boolean baby = living.isBaby();
      boolean sleeping = living.isSleeping();

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