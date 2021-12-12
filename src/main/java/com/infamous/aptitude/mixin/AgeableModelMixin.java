package com.infamous.aptitude.mixin;

import com.infamous.aptitude.client.renderer.IHeadAccessor;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(AgeableListModel.class)
public abstract class AgeableModelMixin implements IHeadAccessor {

    @Nullable
    @Override
    public ModelPart getHead() {
        if(this.headParts().iterator().hasNext()){
            return this.headParts().iterator().next();
        }
        return null;
    }

    @Shadow
    protected abstract Iterable<ModelPart> headParts();
}
