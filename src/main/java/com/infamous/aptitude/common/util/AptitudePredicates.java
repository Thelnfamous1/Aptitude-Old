package com.infamous.aptitude.common.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class AptitudePredicates {
    public static final Predicate<LivingEntity> OCELOT_PREY_PREDICATE = living -> living.getType().is(AptitudeResources.OCELOTS_HUNT);
    public static final Predicate<ItemStack> OCELOT_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.OCELOTS_EAT);
    public static final Predicate<ItemEntity> OCELOT_ALLOWED_ITEMS = (itemEntity) -> !itemEntity.hasPickUpDelay() && itemEntity.isAlive();

    public static final Predicate<LivingEntity> DOLPHIN_PREY_PREDICATE = living -> living.getType().is(AptitudeResources.DOLPHINS_HUNT);
    public static final Predicate<ItemStack> DOLPHIN_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.DOLPHINS_EAT);

    public static final Predicate<LivingEntity> POLAR_BEAR_PREY_PREDICATE = living -> living.getType().is(AptitudeResources.POLAR_BEARS_HUNT);
    public static final Predicate<ItemStack> POLAR_BEAR_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.POLAR_BEARS_EAT);
    public static final Predicate<ItemEntity> POLAR_BEAR_ALLOWED_ITEMS = (itemEntity) -> !itemEntity.hasPickUpDelay() && itemEntity.isAlive();
}
