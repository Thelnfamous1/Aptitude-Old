package com.infamous.aptitude.client.renderer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public interface IHeadAccessor {

    @Nullable
    ModelPart getHead();
}
