package com.infamous.aptitude.common.behavior;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

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

    static Predicate<Mob> predicateFromJson(JsonObject canAttackPredicateObj) {
        Map<MemoryModuleType<?>, MemoryStatus> typeToStatus = new HashMap<>();
        JsonArray checkMemoriesArray = GsonHelper.getAsJsonArray(canAttackPredicateObj, "check_memories");
        checkMemoriesArray.forEach(je -> {
            JsonObject elementObject = je.getAsJsonObject();
            MemoryModuleType<?> memoryType = BehaviorHelper.parseMemoryType(elementObject, "type");
            MemoryStatus memoryStatus = BehaviorHelper.parseMemoryStatus(elementObject, "value");
            typeToStatus.put(memoryType, memoryStatus);
        });

        return mob -> {
            boolean memoryCheck = true;
            for(Map.Entry<MemoryModuleType<?>, MemoryStatus> entry : typeToStatus.entrySet()){
                if(!mob.getBrain().checkMemory(entry.getKey(), entry.getValue())){
                    memoryCheck = false;
                    break;
                }
            }
            return memoryCheck;
        };
    }

}
