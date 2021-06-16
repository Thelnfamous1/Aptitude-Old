package com.infamous.aptitude.client.renderer.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.PolarBearModel;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PolarBearHeldItemLayer<T extends PolarBearEntity, M extends PolarBearModel<T>> extends AgeableHeldItemLayer<T,M>{

    public PolarBearHeldItemLayer(IEntityRenderer<T, M> entityRenderer) {
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
