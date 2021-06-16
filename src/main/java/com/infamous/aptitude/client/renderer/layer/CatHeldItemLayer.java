package com.infamous.aptitude.client.renderer.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.CatModel;
import net.minecraft.entity.passive.CatEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatHeldItemLayer<T extends CatEntity, M extends CatModel<T>> extends AgeableHeldItemLayer<T,M>{

    public CatHeldItemLayer(IEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    protected void translateForBaby(MatrixStack matrixStack) {
        super.translateForBaby(matrixStack);
    }

    @Override
    protected void translateItem(MatrixStack matrixStack, T living) {
        super.translateItem(matrixStack, living);
    }
}
