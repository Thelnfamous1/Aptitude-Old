package com.infamous.aptitude.common.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Predicate;

public class AptitudeHelper {

    public static boolean hasSameOwner(LivingEntity living1, LivingEntity living2){
        UUID ownerUUID1 = getOwnerForTameable(living1);
        UUID ownerUUID2 = getOwnerForTameable(living2);

        return ownerUUID1 != null && ownerUUID1 == ownerUUID2;
    }

    @Nullable
    private static UUID getOwnerForTameable(LivingEntity living) {
        TamableAnimal asTameable = living instanceof TamableAnimal ? (TamableAnimal) living : null;
        AbstractHorse asHorse = living instanceof AbstractHorse ? (AbstractHorse) living : null;

        UUID ownerUUID = null;
        if(asTameable != null && asTameable.getOwnerUUID() != null){
            ownerUUID = asTameable.getOwnerUUID();
        } else if(asHorse != null && asHorse.getOwnerUUID() != null){
            ownerUUID = asHorse.getOwnerUUID();
        }
        return ownerUUID;
    }

    public static boolean isTamedAnimal(LivingEntity living) {
        TamableAnimal asTameable = living instanceof TamableAnimal ? (TamableAnimal) living : null;
        AbstractHorse asHorse = living instanceof AbstractHorse ? (AbstractHorse) living : null;

        return (asTameable != null && asTameable.isTame())
                || (asHorse != null && asHorse.isTamed());
    }

    public static void addEatEffect(ItemStack stack, Level world, LivingEntity living) {
        Item item = stack.getItem();
        if (item.isEdible()) {
            for(Pair<MobEffectInstance, Float> pair : item.getFoodProperties().getEffects()) {
                if (!world.isClientSide && pair.getFirst() != null && world.random.nextFloat() < pair.getSecond()) {
                    living.addEffect(new MobEffectInstance(pair.getFirst()));
                }
            }
        }

    }
}
