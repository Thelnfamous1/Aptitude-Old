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

   public void render(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int p_225628_3_, T mob, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
      boolean sleeping = mob.isSleeping();
      boolean baby = mob.isBaby();
      matrixStack.pushPose();
      if (baby) {
         float babyScale = 0.75F;
         matrixStack.scale(babyScale, babyScale, babyScale);
         matrixStack.translate(0.0D, 0.5D, (double)0.209375F);
      }

      M parentModel = this.getParentModel();
      if(parentModel instanceof IHeadAccessor){
         IHeadAccessor headAccessor = (IHeadAccessor) parentModel;
         if(headAccessor.getHead() != null){
            matrixStack.translate((double)(headAccessor.getHead().x / 16.0F), (double)(headAccessor.getHead().y / 16.0F), (double)(headAccessor.getHead().z / 16.0F));
         }
      }
      //float headRollAngle = mob.getHeadRollAngle(p_225628_7_);
      //matrixStack.mulPose(Vector3f.ZP.rotation(headRollAngle));
      matrixStack.mulPose(Vector3f.YP.rotationDegrees(p_225628_9_));
      matrixStack.mulPose(Vector3f.XP.rotationDegrees(p_225628_10_));
      if (mob.isBaby()) {
         if (sleeping) {
            matrixStack.translate((double)0.4F, (double)0.26F, (double)0.15F);
         } else {
            matrixStack.translate((double)0.06F, (double)0.26F, -0.5D);
         }
      } else if (sleeping) {
         matrixStack.translate((double)0.46F, (double)0.26F, (double)0.22F);
      } else {
         matrixStack.translate((double)0.06F, (double)0.27F, -0.5D);
      }

      matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
      if (sleeping) {
         matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
      }

      EquipmentSlotType slotType = mob instanceof IDevourer ? ((IDevourer) mob).getSlotForFood() : EquipmentSlotType.MAINHAND;
      ItemStack itemBySlot = mob.getItemBySlot(slotType);
      Minecraft.getInstance().getItemInHandRenderer().renderItem(mob, itemBySlot, ItemCameraTransforms.TransformType.GROUND, false, matrixStack, renderTypeBuffer, p_225628_3_);
      matrixStack.popPose();
   }
}