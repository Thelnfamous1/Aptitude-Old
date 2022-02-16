package com.infamous.aptitude.common.manager.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.custom.sensor.CustomSensorType;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import com.infamous.aptitude.common.behavior.util.ConsumerHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.*;
import java.util.function.Consumer;

public class BrainContainer {

    public static final BrainContainer EMPTY = new BrainContainer();

    private boolean replaceBrain = false;
    private Set<MemoryModuleType<?>> memoryTypes = ImmutableSet.of();
    private Set<SensorType<?>> sensorTypes = ImmutableSet.of();
    private Map<CustomSensorType<?>, JsonObject> customSensorTypes = ImmutableMap.of();
    private Map<Activity, List<Pair<Integer, JsonObject>>> prioritizedBehaviorsByActivity = ImmutableMap.of();
    private Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = ImmutableMap.of();
    private Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = ImmutableMap.of();
    private Set<Activity> coreActivities = ImmutableSet.of();
    private List<Activity> rotatingActivities = ImmutableList.of();
    private Activity defaultActivity = Activity.IDLE;
    private Consumer<LivingEntity> brainCreationCallback = livingEntity -> {};
    private Consumer<LivingEntity> updateActivityCallback = livingEntity -> {};

    private BrainContainer(){
    }

    public boolean replaceBrain() {
        return this.replaceBrain;
    }

    public Set<MemoryModuleType<?>> getMemoryTypes() {
        return this.memoryTypes;
    }

    public Set<SensorType<?>> getSensorTypes() {
        return this.sensorTypes;
    }

    public Map<CustomSensorType<?>, JsonObject> getCustomSensorTypes() {
        return this.customSensorTypes;
    }

    public Map<Activity, List<Pair<Integer, JsonObject>>> getPrioritizedBehaviorsByActivity() {
        return this.prioritizedBehaviorsByActivity;
    }

    public Map<Activity, Set<MemoryModuleType<?>>> getActivityMemoriesToEraseWhenStopped() {
        return this.activityMemoriesToEraseWhenStopped;
    }

    public Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> getActivityRequirements() {
        return this.activityRequirements;
    }

    public Set<Activity> getCoreActivities() {
        return this.coreActivities;
    }

    public List<Activity> getRotatingActivities() {
        return this.rotatingActivities;
    }

    public Activity getDefaultActivity() {
        return this.defaultActivity;
    }

    public Consumer<LivingEntity> getBrainCreationCallback() {
        return this.brainCreationCallback;
    }

    public Consumer<LivingEntity> getUpdateActivityCallback() {
        return this.updateActivityCallback;
    }

    public static BrainContainer of(JsonObject topElement){
        BrainContainer brainContainer = new BrainContainer();

        brainContainer.replaceBrain = GsonHelper.getAsBoolean(topElement, "replace_brain", false);

        ImmutableSet.Builder<MemoryModuleType<?>> memoryTypesBuilder = ImmutableSet.builder();
        buildMemoryTypes(memoryTypesBuilder, topElement);
        brainContainer.memoryTypes = memoryTypesBuilder.build();

        ImmutableSet.Builder<SensorType<?>> sensorTypesBuilder = ImmutableSet.builder();
        ImmutableMap.Builder<CustomSensorType<?>, JsonObject> customSensorTypesBuilder = ImmutableMap.builder();

        buildSensorTypes(sensorTypesBuilder, customSensorTypesBuilder, topElement);
        brainContainer.sensorTypes = sensorTypesBuilder.build();
        brainContainer.customSensorTypes = customSensorTypesBuilder.build();

        ImmutableSet.Builder<Activity> coreActivitiesBuilder = ImmutableSet.builder();
        JsonArray coreActivitiesArr = GsonHelper.getAsJsonArray(topElement, "core_activities");
        buildCoreActivities(coreActivitiesBuilder, coreActivitiesArr);
        brainContainer.coreActivities = coreActivitiesBuilder.build();

        brainContainer.defaultActivity = BehaviorHelper.parseActivityOrDefault(topElement, "default_activity", Activity.IDLE);
        brainContainer.brainCreationCallback = ConsumerHelper.parseConsumerOrDefault(topElement, "brain_creation_callback", "type", livingEntity -> {});

        ImmutableList.Builder<Activity> rotatingActivitiesBuilder = ImmutableList.builder();
        JsonArray rotatingActivitiesObj = GsonHelper.getAsJsonArray(topElement, "rotating_activities");
        buildRotatingActivities(rotatingActivitiesBuilder, rotatingActivitiesObj);
        brainContainer.rotatingActivities = rotatingActivitiesBuilder.build();

        brainContainer.updateActivityCallback = ConsumerHelper.parseConsumerOrDefault(topElement, "update_activity_callback", "type", livingEntity -> {});

        ImmutableMap.Builder<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirementsBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStoppedBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<Activity, List<Pair<Integer, JsonObject>>> prioritizedBehaviorsByActivityBuilder = ImmutableMap.builder();

        JsonArray activitiesWithBehaviorsArr = GsonHelper.getAsJsonArray(topElement, "activities_with_behaviors");
        activitiesWithBehaviorsArr.forEach(element -> {
            JsonObject elementObj = element.getAsJsonObject();
            Activity activity = BehaviorHelper.parseActivity(elementObj, "activity");

            buildActivityRequirements(activityRequirementsBuilder, elementObj, activity);
            buildActivityMemoriesToEraseWhenStopped(activityMemoriesToEraseWhenStoppedBuilder, elementObj, activity);
            buildPrioritizedBehaviorsByActivity(prioritizedBehaviorsByActivityBuilder, elementObj, activity);
        });
        brainContainer.activityRequirements = activityRequirementsBuilder.build();
        brainContainer.activityMemoriesToEraseWhenStopped = activityMemoriesToEraseWhenStoppedBuilder.build();
        brainContainer.prioritizedBehaviorsByActivity = prioritizedBehaviorsByActivityBuilder.build();

        return brainContainer;
    }

    private static void buildRotatingActivities(ImmutableList.Builder<Activity> rotatingActivitiesBuilder, JsonArray rotatingActivitiesObj) {
        rotatingActivitiesObj.forEach(je1 -> {
            Activity activity = BehaviorHelper.parseActivity(je1);
            rotatingActivitiesBuilder.add(activity);
        });

    }

    private static void buildPrioritizedBehaviorsByActivity(ImmutableMap.Builder<Activity, List<Pair<Integer, JsonObject>>> prioritizedBehaviorsByActivityBuilder, JsonObject elementObj, Activity activity) {
        List<Pair<Integer, JsonObject>> prioritizedBehaviors;
        JsonArray behaviorArr = GsonHelper.getAsJsonArray(elementObj, "behaviors");
        if(elementObj.has("priority_start")){
            int priorityStart = GsonHelper.getAsInt(elementObj, "priority_start", 0);
            prioritizedBehaviors = createPriorityPairs(priorityStart, behaviorArr);
        } else{
            prioritizedBehaviors = new ArrayList<>();
            behaviorArr.forEach(je1 -> {
                JsonObject elementObj1 = je1.getAsJsonObject();
                int priority = GsonHelper.getAsInt(elementObj1, "priority", 0);
                prioritizedBehaviors.add(Pair.of(priority, elementObj1));
            });
        }
        prioritizedBehaviorsByActivityBuilder.put(activity, prioritizedBehaviors);
    }

    private static List<Pair<Integer, JsonObject>> createPriorityPairs(int priorityStart, JsonArray behaviorJsons) {
        int i = priorityStart;
        List<Pair<Integer, JsonObject>> priorityPairs = new ArrayList<>();

        for(JsonElement behaviorJson : behaviorJsons) {
            //Aptitude.LOGGER.info("Reading in behavior object from data: {}", behaviorJson);
            priorityPairs.add(Pair.of(i++, behaviorJson.getAsJsonObject()));
        }

        return priorityPairs;
    }

    private static void buildActivityRequirements(ImmutableMap.Builder<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirementsBuilder, JsonObject elementObj, Activity activity) {
        Set<Pair<MemoryModuleType<?>, MemoryStatus>> requirements = new HashSet<>();
        if(elementObj.has("requirements")){
            JsonArray requirementArr = GsonHelper.getAsJsonArray(elementObj, "requirements");
            requirementArr.forEach(je1 -> {
                JsonObject elementObj1 = je1.getAsJsonObject();
                MemoryModuleType<?> memoryRequirement = BehaviorHelper.parseMemoryType(elementObj1, "type");
                MemoryStatus statusRequirement = BehaviorHelper.parseMemoryStatus(elementObj1, "status");
                requirements.add(Pair.of(memoryRequirement, statusRequirement));
            });
        }
        activityRequirementsBuilder.put(activity, requirements);
    }

    private static void buildActivityMemoriesToEraseWhenStopped(ImmutableMap.Builder<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStoppedBuilder, JsonObject elementObj, Activity activity) {
        Set<MemoryModuleType<?>> removeWhenStopped = new HashSet<>();
        if(elementObj.has("remove_when_stopped")){
            JsonArray memoryArray = GsonHelper.getAsJsonArray(elementObj, "remove_when_stopped");
            memoryArray.forEach(je1 -> {
                MemoryModuleType<?> memoryToRemove = BehaviorHelper.parseMemoryType(je1);
                removeWhenStopped.add(memoryToRemove);
            });
        }
        activityMemoriesToEraseWhenStoppedBuilder.put(activity, removeWhenStopped);
    }

    private static void buildCoreActivities(ImmutableSet.Builder<Activity> coreActivitiesBuilder, JsonArray coreActivitiesArr) {
        coreActivitiesArr.forEach(je -> {
            Activity activity = BehaviorHelper.parseActivity(je);
            coreActivitiesBuilder.add(activity);
        });
    }

    private static void buildMemoryTypes(ImmutableSet.Builder<MemoryModuleType<?>> memoryTypesBuilder, JsonObject topElement) {
        JsonArray memoryTypesArr = GsonHelper.getAsJsonArray(topElement, "memory_types");
        memoryTypesArr.forEach(je -> {
            MemoryModuleType<?> memoryType = BehaviorHelper.parseMemoryType(je);
            memoryTypesBuilder.add(memoryType);
        });
    }

    private static void buildSensorTypes(ImmutableSet.Builder<SensorType<?>> sensorTypesBuilder, ImmutableMap.Builder<CustomSensorType<?>, JsonObject> customSensorTypesBuilder, JsonObject topElement) {
        JsonArray sensorTypesArr = GsonHelper.getAsJsonArray(topElement, "sensor_types");
        sensorTypesArr.forEach(element -> {
            if(element.isJsonObject()){
                JsonObject customSensorObj = element.getAsJsonObject();
                SensorType<?> sensorType = BehaviorHelper.parseSensorType(customSensorObj, "type");
                if(sensorType instanceof CustomSensorType<?> customSensorType){
                    customSensorTypesBuilder.put(customSensorType, customSensorObj);
                } else{
                    Aptitude.LOGGER.error("Tried to use {} as a CustomSensorType, it will be used as a regular SensorType instead.", GsonHelper.getAsString(customSensorObj, "type"));
                    sensorTypesBuilder.add(sensorType);
                }
            } else{
                SensorType<?> sensorType = BehaviorHelper.parseSensorType(element);
                sensorTypesBuilder.add(sensorType);
            }
        });
    }
}
