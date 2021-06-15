package com.infamous.aptitude.client.renderer.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CarriedItemLayer<T extends MobEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
   public CarriedItemLayer(IEntityRenderer<T, M> entityRenderer) {
      super(entityRenderer);
   }

   public void render(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int p_225628_3_, T mob, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
      matrixStack.pushPose();
      float f = 1.0F;
      float f1 = -1.0F;
      float f2 = MathHelper.abs(mob.xRot) / 60.0F;
      if (mob.xRot < 0.0F) {
         matrixStack.translate(0.0D, (double)(1.0F - f2 * 0.5F), (double)(-1.0F + f2 * 0.5F));
      } else {
         matrixStack.translate(0.0D, (double)(1.0F + f2 * 0.8F), (double)(-1.0F + f2 * 0.2F));
      }

      ItemStack itemBySlot = mob.getItemBySlot(this.getCarriedItemSlot(mob));
      Minecraft.getInstance().getItemInHandRenderer().renderItem(mob, itemBySlot, ItemCameraTransforms.TransformType.GROUND, false, matrixStack, renderTypeBuffer, p_225628_3_);
      matrixStack.popPose();
   }

   protected EquipmentSlotType getCarriedItemSlot(T mob) {
      return EquipmentSlotType.MAINHAND;
   }
}