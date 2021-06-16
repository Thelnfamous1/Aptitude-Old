package com.infamous.aptitude.common.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public class AptitudeHelper {

    public static boolean hasSameOwner(LivingEntity living1, LivingEntity living2){
        UUID ownerUUID1 = getOwnerForTameable(living1);
        UUID ownerUUID2 = getOwnerForTameable(living2);

        return ownerUUID1 != null && ownerUUID1 == ownerUUID2;
    }

    @Nullable
    private static UUID getOwnerForTameable(LivingEntity living) {
        TameableEntity asTameable = living instanceof TameableEntity ? (TameableEntity) living : null;
        AbstractHorseEntity asHorse = living instanceof AbstractHorseEntity ? (AbstractHorseEntity) living : null;

        UUID ownerUUID = null;
        if(asTameable != null && asTameable.getOwnerUUID() != null){
            ownerUUID = asTameable.getOwnerUUID();
        } else if(asHorse != null && asHorse.getOwnerUUID() != null){
            ownerUUID = asHorse.getOwnerUUID();
        }
        return ownerUUID;
    }

    public static boolean isTamedAnimal(LivingEntity living) {
        TameableEntity asTameable = living instanceof TameableEntity ? (TameableEntity) living : null;
        AbstractHorseEntity asHorse = living instanceof AbstractHorseEntity ? (AbstractHorseEntity) living : null;

        return (asTameable != null && asTameable.isTame())
                || (asHorse != null && asHorse.isTamed());
    }
}
