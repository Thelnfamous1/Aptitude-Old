package com.infamous.aptitude.common.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nullable;

public interface IHasOwner
{
    default boolean isOwnedBy(LivingEntity living) {
        return living == this.getOwner();
    }

    @Nullable
    LivingEntity getOwner();
}
