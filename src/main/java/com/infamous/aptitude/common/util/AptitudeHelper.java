package com.infamous.aptitude.common.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

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

    public static Hand getWeaponHoldingHand(LivingEntity living, Predicate<Item> itemPredicate) {
        return itemPredicate.test(living.getMainHandItem().getItem()) ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    public static void addEatEffect(ItemStack stack, World world, LivingEntity living) {
        Item item = stack.getItem();
        if (item.isEdible()) {
            for(Pair<EffectInstance, Float> pair : item.getFoodProperties().getEffects()) {
                if (!world.isClientSide && pair.getFirst() != null && world.random.nextFloat() < pair.getSecond()) {
                    living.addEffect(new EffectInstance(pair.getFirst()));
                }
            }
        }

    }
}
