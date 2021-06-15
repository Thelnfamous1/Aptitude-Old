package com.infamous.aptitude.mixin;

import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.renderer.entity.model.QuadrupedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(QuadrupedModel.class)
public abstract class QuadrupedModelMixin implements IHasHead {

    @Shadow protected ModelRenderer head;

    @Override
    public ModelRenderer getHead() {
        return this.head;
    }
}
