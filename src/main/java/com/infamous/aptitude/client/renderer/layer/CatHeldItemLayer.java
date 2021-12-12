package com.infamous.aptitude.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.CatModel;
import net.minecraft.world.entity.animal.Cat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatHeldItemLayer<T extends Cat, M extends CatModel<T>> extends AgeableHeldItemLayer<T,M>{

    public CatHeldItemLayer(RenderLayerParent<T, M> entityRenderer) {
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
