package com.infamous.aptitude.client.renderer;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public interface IHeadAccessor {

    @Nullable
    ModelRenderer getHead();
}
