package com.infamous.aptitude.common.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Predicate;

public class AptitudePredicates {
    public static final Predicate<LivingEntity> OCELOT_PREY_PREDICATE = living -> living.getType().is(AptitudeResources.OCELOTS_HUNT);
    public static final Predicate<ItemStack> OCELOT_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.OCELOTS_EAT);

    public static final Predicate<LivingEntity> CAT_PREY_PREDICATE = living -> living.getType().is(AptitudeResources.CATS_HUNT) && !AptitudeHelper.isTamedAnimal(living);
    public static final Predicate<ItemStack> CAT_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.CATS_EAT);

    public static final Predicate<LivingEntity> DOLPHIN_PREY_PREDICATE = living -> living.getType().is(AptitudeResources.DOLPHINS_HUNT);
    public static final Predicate<ItemStack> DOLPHIN_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.DOLPHINS_EAT);

    public static final Predicate<LivingEntity> POLAR_BEAR_PREY_PREDICATE = living -> living.getType().is(AptitudeResources.POLAR_BEARS_HUNT);
    public static final Predicate<ItemStack> POLAR_BEAR_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.POLAR_BEARS_EAT);

    public static final Predicate<LivingEntity> WOLF_PREY_PREDICATE = living -> living.getType().is(AptitudeResources.WOLVES_HUNT);

    public static final Predicate<LivingEntity> FOXES_HUNT_ON_LAND = entity -> entity.getType().is(AptitudeResources.FOXES_HUNT_ON_LAND);
    public static final Predicate<LivingEntity> FOXES_HUNT_IN_WATER = entity -> entity.getType().is(AptitudeResources.FOXES_HUNT_IN_WATER);
    public static final Predicate<Entity> FOXES_CAN_STALK = entity -> entity instanceof LivingEntity && FOXES_HUNT_ON_LAND.test((LivingEntity) entity);

    public static final Predicate<ItemStack> PARROT_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.PARROTS_EAT);
    public static final Predicate<ItemStack> COW_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.COWS_EAT);
    public static final Predicate<ItemStack> PIG_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.PIGS_EAT);
    public static final Predicate<ItemStack> CHICKEN_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.CHICKENS_EAT);
    public static final Predicate<ItemStack> RABBIT_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.RABBITS_EAT);
    public static final Predicate<ItemStack> SHEEP_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.SHEEP_EAT);
    public static final Predicate<ItemStack> FOX_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.FOXES_EAT);
    public static final Predicate<ItemStack> PANDA_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.PANDAS_EAT);
    public static final Predicate<ItemStack> TURTLE_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.TURTLES_EAT);
    public static final Predicate<ItemStack> STRIDER_FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.STRIDERS_EAT);
    public static final Predicate<ItemStack> STRIDER_TEMPT_PREDICATE = stack -> STRIDER_FOOD_PREDICATE.test(stack) || stack.getItem() == Items.WARPED_FUNGUS_ON_A_STICK;
    public static final Predicate<ItemStack> CAKE_PREDICATE = stack -> stack.getItem().is(AptitudeResources.CAKES);
    public static final Predicate<ItemStack> PANDA_FOOD_OR_CAKE_PREDICATE = stack -> PANDA_FOOD_PREDICATE.test(stack) || CAKE_PREDICATE.test(stack);

    public static final Predicate<LivingEntity> CAT_DEFEND_PREDICATE = living -> living.getType().is(AptitudeResources.CATS_REPEL) && !AptitudeHelper.isTamedAnimal(living);
    public static final Predicate<LivingEntity> OCELOT_DEFEND_PREDICATE = living -> living.getType().is(AptitudeResources.OCELOTS_REPEL) && !AptitudeHelper.isTamedAnimal(living);
    public static final Predicate<LivingEntity> LLAMA_DEFEND_PREDICATE = living -> living.getType().is(AptitudeResources.LLAMAS_REPEL) && !AptitudeHelper.isTamedAnimal(living);
    public static final Predicate<LivingEntity> DONKEY_DEFEND_PREDICATE = living -> living.getType().is(AptitudeResources.DONKEYS_REPEL) && !AptitudeHelper.isTamedAnimal(living);
    public static final Predicate<LivingEntity> MULE_DEFEND_PREDICATE = living -> living.getType().is(AptitudeResources.MULES_REPEL) && !AptitudeHelper.isTamedAnimal(living);
    public static final Predicate<LivingEntity> WOLF_DEFEND_PREDICATE = living -> living.getType().is(AptitudeResources.WOLVES_REPEL) && !AptitudeHelper.isTamedAnimal(living);

    public static final Predicate<LivingEntity> FOXES_AVOID_PREDICATE = living -> living.getType().is(AptitudeResources.FOXES_AVOID) && !AptitudeHelper.isTamedAnimal(living);
    public static final Predicate<LivingEntity> CREEPERS_AVOID_PREDICATE = living -> living.getType().is(AptitudeResources.CREEPERS_AVOID);
    public static final Predicate<LivingEntity> SKELETONS_AVOID_PREDICATE = living -> living.getType().is(AptitudeResources.SKELETONS_AVOID);
    public static final Predicate<LivingEntity> WOLVES_AVOID_PREDICATE = living -> living.getType().is(AptitudeResources.WOLVES_AVOID) && !AptitudeHelper.isTamedAnimal(living);
    public static final Predicate<LivingEntity> RABBITS_AVOID_PREDICATE = living -> living.getType().is(AptitudeResources.RABBITS_AVOID) && !AptitudeHelper.isTamedAnimal(living);

    public static final Predicate<ItemEntity> ALLOWED_ITEMS = (itemEntity) -> !itemEntity.hasPickUpDelay() && itemEntity.isAlive();
    public static final Predicate<ItemEntity> PANDA_ITEMS =
            (itemEntity) -> PANDA_FOOD_OR_CAKE_PREDICATE.test(itemEntity.getItem()) && !itemEntity.hasPickUpDelay() && itemEntity.isAlive();

}
