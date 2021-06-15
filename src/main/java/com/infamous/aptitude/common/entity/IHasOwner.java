package com.infamous.aptitude.common.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nullable;

public interface IHasOwner
{
    @Nullable
    LivingEntity getOwner();
}
