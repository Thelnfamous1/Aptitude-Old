package com.infamous.aptitude.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CarriedItemLayer<T extends Mob, M extends EntityModel<T>> extends RenderLayer<T, M> {
   public CarriedItemLayer(RenderLayerParent<T, M> entityRenderer) {
      super(entityRenderer);
   }

   public void render(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int p_225628_3_, T mob, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
      matrixStack.pushPose();
      float f = 1.0F;
      float f1 = -1.0F;
      float f2 = Mth.abs(mob.getXRot()) / 60.0F;
      if (mob.getXRot() < 0.0F) {
         matrixStack.translate(0.0D, (double)(1.0F - f2 * 0.5F), (double)(-1.0F + f2 * 0.5F));
      } else {
         matrixStack.translate(0.0D, (double)(1.0F + f2 * 0.8F), (double)(-1.0F + f2 * 0.2F));
      }

      ItemStack itemBySlot = mob.getItemBySlot(this.getCarriedItemSlot(mob));
      Minecraft.getInstance().getItemInHandRenderer().renderItem(mob, itemBySlot, ItemTransforms.TransformType.GROUND, false, matrixStack, renderTypeBuffer, p_225628_3_);
      matrixStack.popPose();
   }

   protected EquipmentSlot getCarriedItemSlot(T mob) {
      return EquipmentSlot.MAINHAND;
   }
}