package com.infamous.aptitude.common.manager.base;

import com.infamous.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class BaseAIHelper {

    public static void firstSpawn(LivingEntity mob) {
        ResourceLocation etLocation = mob.getType().getRegistryName();
        Consumer<LivingEntity> firstSpawn = Aptitude.baseAIManager.firstSpawn(etLocation);
        firstSpawn.accept(mob);
    }

    public static boolean hasBaseAIFile(LivingEntity mob) {
        ResourceLocation etLocation = mob.getType().getRegistryName();
        return Aptitude.baseAIManager.hasBaseAIEntry(etLocation);
    }

    public static boolean wantsToPickUp(Mob mob, ItemStack item) {
        ResourceLocation etLocation = mob.getType().getRegistryName();
        BiPredicate<LivingEntity, ItemStack> wantsToPickUp = Aptitude.baseAIManager.wantsToPickUp(etLocation);
        return wantsToPickUp.test(mob, item);
    }

    public static void pickUpItem(Mob mob, ItemEntity itemEntity) {
        ResourceLocation etLocation = mob.getType().getRegistryName();
        BiConsumer<LivingEntity, ItemEntity> pickUpItem = Aptitude.baseAIManager.pickUpItem(etLocation);
        pickUpItem.accept(mob, itemEntity);
    }

    public static void addedToWorld(LivingEntity mob) {
        ResourceLocation etLocation = mob.getType().getRegistryName();
        Consumer<LivingEntity> addedToWorld = Aptitude.baseAIManager.addedToWorld(etLocation);
        addedToWorld.accept(mob);
    }

    public static void attackedBy(LivingEntity victim, LivingEntity attacker) {
        ResourceLocation etLocation = victim.getType().getRegistryName();
        BiConsumer<LivingEntity, LivingEntity> attackedBy = Aptitude.baseAIManager.attackedBy(etLocation);
        attackedBy.accept(victim, attacker);
    }

    public static void attacked(LivingEntity victim, LivingEntity attacker) {
        ResourceLocation etLocation = victim.getType().getRegistryName();
        BiConsumer<LivingEntity, LivingEntity> attacked = Aptitude.baseAIManager.attacked(etLocation);
        attacked.accept(victim, attacker);
    }
}
