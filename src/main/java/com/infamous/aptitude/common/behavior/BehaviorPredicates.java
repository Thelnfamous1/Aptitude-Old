package com.infamous.aptitude.common.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BehaviorPredicates {

    public static boolean isAdult(LivingEntity livingEntity){
        return !isBaby(livingEntity);
    }

    public static boolean isBaby(LivingEntity livingEntity){
        return livingEntity.isBaby();
    }

    public static boolean isBreeding(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }

    protected static boolean isPacified(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.PACIFIED);
    }
}
