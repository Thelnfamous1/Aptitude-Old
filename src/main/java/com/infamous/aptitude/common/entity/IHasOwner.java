package com.infamous.aptitude.common.entity;

import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public interface IHasOwner
{
    default boolean isOwnedBy(LivingEntity living) {
        return living == this.getOwner();
    }

    @Nullable
    LivingEntity getOwner();
}
