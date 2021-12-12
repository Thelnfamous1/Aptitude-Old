package com.infamous.aptitude.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.PolarBearModel;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PolarBearHeldItemLayer<T extends PolarBear, M extends PolarBearModel<T>> extends AgeableHeldItemLayer<T,M>{

    public PolarBearHeldItemLayer(RenderLayerParent<T, M> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    protected void translateForBaby(PoseStack matrixStack) {
        super.translateForBaby(matrixStack);
    }

    @Override
    protected void translateItem(PoseStack matrixStack, T living) {
        super.translateItem(matrixStack, living);
    }
}
