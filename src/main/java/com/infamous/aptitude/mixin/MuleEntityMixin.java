package com.infamous.aptitude.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.horse.DonkeyEntity;
import net.minecraft.entity.passive.horse.MuleEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MuleEntity.class)
public abstract class MuleEntityMixin extends AbstractHorseEntityMixin{
    protected MuleEntityMixin(EntityType<? extends AnimalEntity> p_i48568_1_, World p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Override
    public SoundEvent getAngrySoundRaw() {
        return SoundEvents.MULE_ANGRY;
    }
}
