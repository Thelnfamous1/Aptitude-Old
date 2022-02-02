package com.infamous.aptitude.common.behavior.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.infamous.aptitude.common.behavior.BehaviorType;
import com.infamous.aptitude.common.behavior.BehaviorTypes;
import com.infamous.aptitude.common.behavior.consumer.ConsumerType;
import com.infamous.aptitude.common.behavior.consumer.ConsumerTypes;
import com.infamous.aptitude.common.behavior.functions.FunctionType;
import com.infamous.aptitude.common.behavior.functions.FunctionTypes;
import com.infamous.aptitude.common.behavior.predicates.PredicateType;
import com.infamous.aptitude.common.behavior.predicates.PredicateTypes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class BehaviorHelper {

    public static <U> MemoryModuleType<U> parseMemoryType(JsonObject jsonObject, String memberName) {
        String type = GsonHelper.getAsString(jsonObject, memberName, "");
        return (MemoryModuleType<U>) parseMemoryTypeString(type);
    }

    public static <U> MemoryModuleType<U> parseMemoryType(JsonElement jsonElement) {
        String type = jsonElement.getAsString();
        return (MemoryModuleType<U>) parseMemoryTypeString(type);
    }

    public static <U> MemoryModuleType<U> parseMemoryTypeString(String type) {
        ResourceLocation location = new ResourceLocation(type);
        MemoryModuleType<?> memoryType = ForgeRegistries.MEMORY_MODULE_TYPES.getValue(location);
        if (memoryType == null) throw new JsonParseException("Invalid memory module type: " + type);
        return (MemoryModuleType<U>) memoryType;
    }

    public static SensorType<?> parseSensorType(JsonElement jsonElement) {
        String type = jsonElement.getAsString();
        ResourceLocation location = new ResourceLocation(type);
        SensorType<?> sensorType = ForgeRegistries.SENSOR_TYPES.getValue(location);
        if(sensorType == null) throw new JsonParseException("Invalid sensor type: " + type);
        return sensorType;
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

    public static EntityType<?> parseEntityType(JsonElement memberElement) {
        String entityTypeString = memberElement.getAsString();
        return parseEntityTypeString(entityTypeString);
    }

    public static EntityType<?> parseEntityType(JsonObject jsonObject, String memberName) {
        String entityTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        return parseEntityTypeString(entityTypeString);
    }

    public static EntityType<?> parseEntityTypeString(String entityTypeString) {
        ResourceLocation etLocation = new ResourceLocation(entityTypeString);
        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(etLocation);
        if (entityType == null) throw new JsonParseException("Invalid entity type: " + entityTypeString);
        return entityType;
    }

    public static List<Pair<Behavior<?>, Integer>> parseWeightedBehaviors(JsonObject jsonObject, String memberName){
        List<Pair<Behavior<?>, Integer>> weightedBehaviors = new ArrayList<>();
        JsonArray behaviorArray = GsonHelper.getAsJsonArray(jsonObject, memberName);
        behaviorArray.forEach(je -> {
                    JsonObject elementObject = je.getAsJsonObject();
                    int weight = GsonHelper.getAsInt(elementObject, "weight", 0);
                    Behavior<?> behavior = parseBehavior(elementObject, "type");
                    Pair<Behavior<?>, Integer> pair = Pair.of(behavior, weight);
                    weightedBehaviors.add(pair);
                }
        );
        return weightedBehaviors;
    }

    public static Behavior<?> parseBehavior(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject behaviorObject = GsonHelper.getAsJsonObject(jsonObject, memberName);
        return parseBehavior(behaviorObject, typeMemberName);
    }

    public static Behavior<?> parseBehavior(JsonObject behaviorObject, String typeMemberName){
        BehaviorType<?> behaviorType = parseBehaviorType(behaviorObject, typeMemberName);

        return behaviorType.fromJson(behaviorObject);
    }

    public static BehaviorType<?> parseBehaviorType(JsonObject jsonObject, String memberName) {
        String behaviorTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation btLocation = new ResourceLocation(behaviorTypeString);
        BehaviorType<?> behaviorType = BehaviorTypes.getBehaviorType(btLocation);
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

    public static Map<MemoryModuleType<?>, MemoryStatus> parseMemoriesToStatus(JsonElement addContextElement) {
        Map<MemoryModuleType<?>, MemoryStatus> memoriesToStatus = new HashMap<>();

        if(addContextElement.isJsonArray()){
            JsonArray addContextArray = addContextElement.getAsJsonArray();
            addContextArray.forEach(jsonElement -> {
                JsonObject elementAsObj = jsonElement.getAsJsonObject();
                buildMemoriesToStatus(memoriesToStatus, elementAsObj);
            });
        } else if(addContextElement.isJsonObject()){
            buildMemoriesToStatus(memoriesToStatus, addContextElement.getAsJsonObject());
        }
        return memoriesToStatus;
    }

    public static void buildMemoriesToStatus(Map<MemoryModuleType<?>, MemoryStatus> memoriesToStatus, JsonObject elementAsObj) {
        MemoryModuleType<?> memoryType = parseMemoryType(elementAsObj, "type");
        MemoryStatus memoryStatus = parseMemoryStatus(elementAsObj, "status");
        memoriesToStatus.put(memoryType, memoryStatus);
    }

    public static <U> Predicate<U> parsePredicate(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject predicateObj = GsonHelper.getAsJsonObject(jsonObject, memberName);
        PredicateType<?> predicateType = parsePredicateType(predicateObj, typeMemberName);

        return (Predicate<U>) predicateType.fromJson(predicateObj);
    }

    public static PredicateType<?> parsePredicateType(JsonObject jsonObject, String memberName){
        String predicateTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation ptLocation = new ResourceLocation(predicateTypeString);
        PredicateType<?> predicateType = PredicateTypes.getPredicateType(ptLocation);
        if(predicateType == null) throw new JsonParseException("Invalid predicate type: " + predicateTypeString);
        return predicateType;
    }

    public static <T, R> Function<T, R> parseFunction(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject functionObj = GsonHelper.getAsJsonObject(jsonObject, memberName);
        FunctionType<?> predicateType = parseFunctionType(functionObj, typeMemberName);

        return (Function<T, R>) predicateType.fromJson(functionObj);
    }

    public static FunctionType<?> parseFunctionType(JsonObject jsonObject, String memberName){
        String functionTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation ftLocation = new ResourceLocation(functionTypeString);
        FunctionType<?> functionType = FunctionTypes.getFunctionType(ftLocation);
        if(functionType == null) throw new JsonParseException("Invalid function type: " + functionTypeString);
        return functionType;
    }

    public static Activity parseActivity(JsonElement je) {
        String activityString = je.getAsString();
        return parseActivityString(activityString);
    }

    public static Activity parseActivity(JsonObject jsonObject, String typeMemberName) {
        String activityString = GsonHelper.getAsString(jsonObject, typeMemberName);
        return parseActivityString(activityString);
    }

    public static Activity parseActivityString(String activityString) {
        ResourceLocation activityLocation = new ResourceLocation(activityString);
        Activity activity = ForgeRegistries.ACTIVITIES.getValue(activityLocation);
        if (activity == null) throw new JsonParseException("Invalid activity: " + activityString);
        return activity;
    }

    public static <U> Consumer<U> parseConsumer(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject consumerObj = GsonHelper.getAsJsonObject(jsonObject, memberName);
        return parseConsumer(consumerObj, typeMemberName);
    }

    public static <U> Consumer<U> parseConsumer(JsonObject jsonObject, String typeMemberName){
        ConsumerType<?> consumerType = parseConsumerType(jsonObject, typeMemberName);
        return (Consumer<U>) consumerType.fromJson(jsonObject);
    }

    public static ConsumerType<?> parseConsumerType(JsonObject jsonObject, String memberName){
        String consumerTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation ctLocation = new ResourceLocation(consumerTypeString);
        ConsumerType<?> consumerType = ConsumerTypes.getConsumerType(ctLocation);
        if(consumerType == null) throw new JsonParseException("Invalid consumer type: " + consumerTypeString);
        return consumerType;
    }

    public static SoundEvent parseSoundEvent(JsonObject jsonObject, String memberName, String typeMemberName) {
        JsonObject soundEventObj = GsonHelper.getAsJsonObject(jsonObject, memberName);
        return parseSoundEventString(soundEventObj, typeMemberName);
    }

    public static SoundEvent parseSoundEventString(JsonObject jsonObject, String memberName){
        String soundTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation seLocation = new ResourceLocation(soundTypeString);
        SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(seLocation);
        if(soundEvent == null) throw new JsonParseException("Invalid sound event: " + soundTypeString);
        return soundEvent;
    }

    public static List<EntityTypePredicate> getEntityTypePredicates(JsonObject jsonObject, String memberName) {
        List<EntityTypePredicate> ignoredTypes = new ArrayList<>();
        if(jsonObject.has(memberName)){
            JsonElement entityTypePredicateElem = jsonObject.get(memberName);
            if(entityTypePredicateElem.isJsonArray()){
                JsonArray ignoredTypesArr = GsonHelper.getAsJsonArray(jsonObject, memberName);
                ignoredTypesArr.forEach(jsonElement -> {
                    EntityTypePredicate entityTypePredicate = EntityTypePredicate.fromJson(jsonElement);
                    ignoredTypes.add(entityTypePredicate);
                });
            } else{
                EntityTypePredicate entityTypePredicate = EntityTypePredicate.fromJson(entityTypePredicateElem);
                ignoredTypes.add(entityTypePredicate);
            }
        }
        return ignoredTypes;
    }

    public static <U> List<Predicate<U>> getPredicates(JsonObject jsonObject, String predicatesMemberName, String typeMemberName) {
        JsonArray predicatesArray = GsonHelper.getAsJsonArray(jsonObject, predicatesMemberName);
        List<Predicate<U>> predicates = new ArrayList<>();
        predicatesArray.forEach(jsonElement -> {
            JsonObject elemObj = jsonElement.getAsJsonObject();
            PredicateType<?> predicateType = parsePredicateType(elemObj, typeMemberName);
            Predicate<U> predicate = (Predicate<U>) predicateType.fromJson(elemObj);
            predicates.add(predicate);
        });
        return predicates;
    }
}
