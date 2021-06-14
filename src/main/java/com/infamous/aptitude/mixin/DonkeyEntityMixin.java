package com.infamous.aptitude.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.horse.DonkeyEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DonkeyEntity.class)
public abstract class DonkeyEntityMixin extends AbstractHorseEntityMixin{
    protected DonkeyEntityMixin(EntityType<? extends AnimalEntity> p_i48568_1_, World p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Override
    public SoundEvent getAngrySoundRaw() {
        return SoundEvents.DONKEY_ANGRY;
    }
}
