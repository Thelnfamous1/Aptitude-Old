package com.infamous.aptitude.mixin;

import com.infamous.aptitude.client.renderer.IHeadAccessor;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(AgeableModel.class)
public abstract class AgeableModelMixin implements IHeadAccessor {

    @Nullable
    @Override
    public ModelRenderer getHead() {
        if(this.headParts().iterator().hasNext()){
            return this.headParts().iterator().next();
        }
        return null;
    }

    @Shadow
    protected abstract Iterable<ModelRenderer> headParts();
}
