package com.infamous.aptitude.common.behavior.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.infamous.aptitude.common.behavior.AptitudeRegistries;
import com.infamous.aptitude.common.behavior.BehaviorType;
import com.infamous.aptitude.common.behavior.functions.FunctionType;
import com.infamous.aptitude.common.behavior.predicates.PredicateType;
import com.infamous.aptitude.mixin.LivingEntityAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class BehaviorHelper {

    public static boolean hasRequiredMemories(LivingEntity livingEntity, Map<MemoryModuleType<?>, MemoryStatus> entryCondition) {
        for(Map.Entry<MemoryModuleType<?>, MemoryStatus> entry : entryCondition.entrySet()) {
            MemoryModuleType<?> memorymoduletype = entry.getKey();
            MemoryStatus memorystatus = entry.getValue();
            if (!livingEntity.getBrain().checkMemory(memorymoduletype, memorystatus)) {
                return false;
            }
        }
        return true;
    }

    public static MemoryModuleType<?> parseMemoryType(JsonObject jsonObject, String memberName) {
        String type = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation location = new ResourceLocation(type);
        MemoryModuleType<?> memoryType = ForgeRegistries.MEMORY_MODULE_TYPES.getValue(location);
        if(memoryType == null) throw new JsonParseException("Invalid memory module type: " + type);
        return memoryType;
    }

    public static MemoryStatus parseMemoryStatus(JsonObject elementObject, String memberName) {
        String value = GsonHelper.getAsString(elementObject, memberName, "");
        MemoryStatus memoryStatus;
        try{
            memoryStatus = MemoryStatus.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e){
            throw new JsonParseException("Invalid memory status value: " + value);
        }
        return memoryStatus;
    }

    public static EntityType<?> parseEntityTypeFromElement(JsonElement memberElement) {
        String entityTypeString = memberElement.getAsString();
        ResourceLocation etLocation = new ResourceLocation(entityTypeString);
        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(etLocation);
        if(entityType == null) throw new JsonParseException("Invalid entity type: " + entityTypeString);
        return entityType;
    }

    public static EntityType<?> parseEntityType(JsonObject jsonObject, String memberName) {
        String entityTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation etLocation = new ResourceLocation(entityTypeString);
        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(etLocation);
        if(entityType == null) throw new JsonParseException("Invalid entity type: " + entityTypeString);
        return entityType;
    }

    public static List<Pair<Behavior<?>, Integer>> parsePrioritizedBehaviors(JsonObject jsonObject, String memberName){
        List<Pair<Behavior<?>, Integer>> prioritizedBehaviors = new ArrayList<>();
        JsonArray behaviorArray = GsonHelper.getAsJsonArray(jsonObject, memberName);
        behaviorArray.forEach(je -> {
                    JsonObject elementObject = je.getAsJsonObject();
                    int priority = GsonHelper.getAsInt(elementObject, "priority", 0);
                    Behavior<?> behavior = parseBehavior(elementObject, "behavior", "type");
                    Pair<Behavior<?>, Integer> pair = Pair.of(behavior, priority);
                    prioritizedBehaviors.add(pair);
                }
        );
        return prioritizedBehaviors;
    }

    public static Behavior<?> parseBehavior(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject behaviorObject = GsonHelper.getAsJsonObject(jsonObject, memberName);
        BehaviorType<?> behaviorType = parseBehaviorType(behaviorObject, typeMemberName);

        return behaviorType.fromJson(behaviorObject);
    }

    public static BehaviorType<?> parseBehaviorType(JsonObject jsonObject, String memberName) {
        String behaviorTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation btLocation = new ResourceLocation(behaviorTypeString);
        BehaviorType<?> behaviorType = AptitudeRegistries.BEHAVIOR_TYPES.getValue(btLocation);
        if(behaviorType == null) throw new JsonParseException("Invalid behavior type: " + behaviorTypeString);
        return behaviorType;
    }

    public static UniformInt parseUniformInt(JsonObject jsonObject, String memberName) {
        JsonObject interval = GsonHelper.getAsJsonObject(jsonObject, memberName);
        int minInclusive = GsonHelper.getAsInt(interval, "minInclusive", 0);
        int maxInclusive = GsonHelper.getAsInt(interval, "maxInclusive", 0);
        return UniformInt.of(minInclusive, maxInclusive);
    }

    public static float parseSpeedModifier(JsonObject jsonObject) {
        return GsonHelper.getAsFloat(jsonObject, "speedModifier", 1.0F);
    }

    public static Pair<Integer, Integer> parseBaseBehaviorDuration(JsonObject jsonObject){
        int minDuration = GsonHelper.getAsInt(jsonObject, "minDuration", 150);
        int maxDuration = GsonHelper.getAsInt(jsonObject, "maxDuration", 250);
        return Pair.of(minDuration, maxDuration);
    }

    public static Map<MemoryModuleType<?>, MemoryStatus> parseMemoriesToStatus(JsonObject addContextObj) {
        Map<MemoryModuleType<?>, MemoryStatus> memoriesToStatus = new HashMap<>();

        if(addContextObj.isJsonArray()){
            JsonArray addContextArray = addContextObj.getAsJsonArray();
            addContextArray.forEach(jsonElement -> {
                JsonObject elementAsObj = jsonElement.getAsJsonObject();
                buildMemoriesToStatus(memoriesToStatus, elementAsObj);
            });
        } else{
            buildMemoriesToStatus(memoriesToStatus, addContextObj);
        }
        return memoriesToStatus;
    }

    private static void buildMemoriesToStatus(Map<MemoryModuleType<?>, MemoryStatus> memoriesToStatus, JsonObject elementAsObj) {
        MemoryModuleType<?> memoryType = parseMemoryType(elementAsObj, "type");
        MemoryStatus memoryStatus = parseMemoryStatus(elementAsObj, "status");
        memoriesToStatus.put(memoryType, memoryStatus);
    }

    public static Predicate<?> parsePredicate(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject predicateObj = GsonHelper.getAsJsonObject(jsonObject, memberName);
        PredicateType<?> predicateType = parsePredicateType(predicateObj, typeMemberName);

        return predicateType.fromJson(predicateObj);
    }

    public static PredicateType<?> parsePredicateType(JsonObject jsonObject, String memberName){
        String predicateTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation ptLocation = new ResourceLocation(predicateTypeString);
        PredicateType<?> predicateType = AptitudeRegistries.PREDICATE_TYPES.getValue(ptLocation);
        if(predicateType == null) throw new JsonParseException("Invalid predicate type: " + predicateTypeString);
        return predicateType;
    }

    public static Function<?, ?> parseFunction(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject functionObj = GsonHelper.getAsJsonObject(jsonObject, memberName);
        FunctionType<?> predicateType = parseFunctionType(functionObj, typeMemberName);

        return predicateType.fromJson(functionObj);
    }

    public static FunctionType<?> parseFunctionType(JsonObject jsonObject, String memberName){
        String functionTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation ftLocation = new ResourceLocation(functionTypeString);
        FunctionType<?> functionType = AptitudeRegistries.FUNCTION_TYPES.getValue(ftLocation);
        if(functionType == null) throw new JsonParseException("Invalid function type: " + functionTypeString);
        return functionType;
    }

    public static <E extends LivingEntity> void refreshBrain(E mob, ServerLevel serverLevel, Set<MemoryModuleType<?>> memoryTypes, Set<SensorType<? extends Sensor<? super E>>> sensorTypes, Map<Integer, Map<Activity, Set<BehaviorType<?>>>> activitiesByPriority){
        Brain<E> brain = (Brain<E>) mob.getBrain();
        brain.stopAll(serverLevel, mob);
        ((LivingEntityAccessor)mob).setBrain(brain.copyWithoutBehaviors());
        // Add custom memory module types
        // Add custom sensors
        // Add custom behaviors
    }
}
