package com.infamous.aptitude.common.behavior;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class BrainManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<ResourceLocation, Set<MemoryModuleType<?>>> memoryTypes = ImmutableMap.of();
    private Map<ResourceLocation, Set<SensorType<?>>> sensorTypes = ImmutableMap.of();
    private Map<ResourceLocation, Map<Activity, List<Pair<Integer, JsonObject>>>> prioritizedBehaviorsByActivity = ImmutableMap.of();
    private Map<ResourceLocation, Map<Activity, Set<MemoryModuleType<?>>>> activityMemoriesToEraseWhenStopped = ImmutableMap.of();
    private Map<ResourceLocation, Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>>> activityRequirements = ImmutableMap.of();
    private Map<ResourceLocation, Set<Activity>> coreActivities = ImmutableMap.of();
    private Map<ResourceLocation, Pair<Activity, Boolean>> defaultActivities = ImmutableMap.of();

    public BrainManager() {
        super(GSON, "brain");
    }

    public Set<MemoryModuleType<?>> getMemoryTypes(ResourceLocation location) {
        return this.memoryTypes.getOrDefault(location, ImmutableSet.of());
    }

    public <E extends LivingEntity> Set<SensorType<? extends Sensor<? super E>>> getSensorTypesUnchecked(ResourceLocation location) {
        return (Set<SensorType<? extends Sensor<? super E>>>) this.sensorTypes.getOrDefault(location, ImmutableSet.of());
    }

    public Map<Activity, List<Pair<Integer, JsonObject>>> getPrioritizedBehaviorsByActivity(ResourceLocation location) {
        return this.prioritizedBehaviorsByActivity.getOrDefault(location, ImmutableMap.of());
    }

    public Set<Activity> getCoreActivities(ResourceLocation location) {
        return this.coreActivities.getOrDefault(location, ImmutableSet.of());
    }

    public Pair<Activity, Boolean> getDefaultActivity(ResourceLocation location) {
        return this.defaultActivities.getOrDefault(location, Pair.of(Activity.IDLE, true));
    }

    public Map<Activity, Set<MemoryModuleType<?>>> getActivityMemoriesToEraseWhenStopped(ResourceLocation location){
        return this.activityMemoriesToEraseWhenStopped.getOrDefault(location, ImmutableMap.of());
    }

    public Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> getActivityRequirements(ResourceLocation location) {
        return this.activityRequirements.getOrDefault(location, ImmutableMap.of());
    }

    protected void apply(Map<ResourceLocation, JsonElement> locationElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder<ResourceLocation, Set<MemoryModuleType<?>>> memoryTypesBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, Set<SensorType<?>>> sensorTypesBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, Map<Activity, List<Pair<Integer, JsonObject>>>> prioritizedBehaviorsByActivityBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>>> activityRequirementsBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, Map<Activity, Set<MemoryModuleType<?>>>> activityMemoriesToEraseWhenStoppedBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, Set<Activity>> coreActivitiesBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, Pair<Activity, Boolean>> defaultActivitiesBuilder = ImmutableMap.builder();

        locationElementMap.forEach((location, jsonElement) -> {
            try {
                JsonObject topElement = GsonHelper.convertToJsonObject(jsonElement, "top element");

                JsonArray coreActivitiesArr = GsonHelper.getAsJsonArray(topElement, "core_activities");
                JsonObject defaultActivityObj = GsonHelper.getAsJsonObject(topElement, "default_activity");
                JsonArray activitiesByPriorityArr = GsonHelper.getAsJsonArray(topElement, "activities_by_priority");

                this.buildMemoryTypes(memoryTypesBuilder, location, topElement);
                this.buildSensorTypes(sensorTypesBuilder, location, topElement);
                this.buildCoreActivities(coreActivitiesBuilder, location, coreActivitiesArr);
                this.buildDefaultActivity(defaultActivitiesBuilder, location, defaultActivityObj);

                activitiesByPriorityArr.forEach(je -> {
                    JsonObject elementObj = je.getAsJsonObject();
                    Activity activity = BehaviorHelper.parseActivity(elementObj, "activity");

                    this.buildActivityMemoriesToEraseWhenStopped(activityMemoriesToEraseWhenStoppedBuilder, location, elementObj, activity);
                    this.buildActivityRequirements(activityRequirementsBuilder, location, elementObj, activity);
                    this.buildPrioritizedBehaviorsByActivity(prioritizedBehaviorsByActivityBuilder, location, elementObj, activity);

                });

            } catch (Exception exception) {
                LOGGER.error("Couldn't parse brain for {}", location, exception);
            }

        });
        this.memoryTypes = memoryTypesBuilder.build();
        this.sensorTypes = sensorTypesBuilder.build();
        this.prioritizedBehaviorsByActivity = prioritizedBehaviorsByActivityBuilder.build();
        this.activityRequirements = activityRequirementsBuilder.build();
        this.activityMemoriesToEraseWhenStopped = activityMemoriesToEraseWhenStoppedBuilder.build();
        this.coreActivities = coreActivitiesBuilder.build();
        this.defaultActivities = defaultActivitiesBuilder.build();
    }

    private void buildPrioritizedBehaviorsByActivity(ImmutableMap.Builder<ResourceLocation, Map<Activity, List<Pair<Integer, JsonObject>>>> prioritizedBehaviorsByActivityBuilder, ResourceLocation location, JsonObject elementObj, Activity activity) {
        List<Pair<Integer, JsonObject>> prioritizedBehaviors;
        JsonArray behaviorArr = GsonHelper.getAsJsonArray(elementObj, "behaviors");
        if(elementObj.has("priority_start")){
            int priorityStart = GsonHelper.getAsInt(elementObj, "priority_start", 0);
            prioritizedBehaviors = this.createPriorityPairs(priorityStart, behaviorArr);
        } else{
            prioritizedBehaviors = new ArrayList<>();
            behaviorArr.forEach(je1 -> {
                JsonObject elementObj1 = je1.getAsJsonObject();
                int priority = GsonHelper.getAsInt(elementObj1, "priority", 0);
                prioritizedBehaviors.add(Pair.of(priority, elementObj1));
            });
        }
        Map<Activity, List<Pair<Integer, JsonObject>>> prioritizedBehaviorsForActivity = new HashMap<>();
        prioritizedBehaviorsForActivity.put(activity, prioritizedBehaviors);
        prioritizedBehaviorsByActivityBuilder.put(location, prioritizedBehaviorsForActivity);
    }

    private List<Pair<Integer, JsonObject>> createPriorityPairs(int priorityStart, JsonArray behaviorJsons) {
        int i = priorityStart;
        List<Pair<Integer, JsonObject>> priorityPairs = new ArrayList<>();

        for(JsonElement behaviorJson : behaviorJsons) {
            priorityPairs.add(Pair.of(i++, behaviorJson.getAsJsonObject()));
        }

        return priorityPairs;
    }

    private void buildActivityRequirements(ImmutableMap.Builder<ResourceLocation, Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>>> activityRequirementsBuilder, ResourceLocation location, JsonObject elementObj, Activity activity) {
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
        Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> requirementsForActivity = new HashMap<>();
        requirementsForActivity.put(activity, requirements);
        activityRequirementsBuilder.put(location, requirementsForActivity);
    }

    private void buildActivityMemoriesToEraseWhenStopped(ImmutableMap.Builder<ResourceLocation, Map<Activity, Set<MemoryModuleType<?>>>> activityMemoriesToEraseWhenStoppedBuilder, ResourceLocation location, JsonObject elementObj, Activity activity) {
        Set<MemoryModuleType<?>> removeWhenStopped = new HashSet<>();
        if(elementObj.has("remove_when_stopped")){
            JsonArray memoryArray = GsonHelper.getAsJsonArray(elementObj, "remove_when_stopped");
            memoryArray.forEach(je1 -> {
                MemoryModuleType<?> memoryToRemove = BehaviorHelper.parseMemoryType(je1);
                removeWhenStopped.add(memoryToRemove);
            });
        }
        Map<Activity, Set<MemoryModuleType<?>>> removeForActivity = new HashMap<>();
        removeForActivity.put(activity, removeWhenStopped);
        activityMemoriesToEraseWhenStoppedBuilder.put(location, removeForActivity);
    }

    private void buildDefaultActivity(ImmutableMap.Builder<ResourceLocation, Pair<Activity, Boolean>> defaultActivitiesBuilder, ResourceLocation location, JsonObject defaultActivityObj) {
        Activity activity = BehaviorHelper.parseActivity(defaultActivityObj, "type");
        defaultActivitiesBuilder.put(location, Pair.of(activity, GsonHelper.getAsBoolean(defaultActivityObj, "set_active", true)));
    }

    private void buildCoreActivities(ImmutableMap.Builder<ResourceLocation, Set<Activity>> coreActivitiesBuilder, ResourceLocation location, JsonArray coreActivitiesArr) {
        Set<Activity> coreActivitySet = new HashSet<>();
        coreActivitiesArr.forEach(je -> {
            Activity activity = BehaviorHelper.parseActivity(je);
            coreActivitySet.add(activity);
        });
        coreActivitiesBuilder.put(location, coreActivitySet);
    }

    private void buildMemoryTypes(ImmutableMap.Builder<ResourceLocation, Set<MemoryModuleType<?>>> memoryTypesBuilder, ResourceLocation location, JsonObject topElement) {
        JsonArray memoryTypesArr = GsonHelper.getAsJsonArray(topElement, "memory_types");
        Set<MemoryModuleType<?>> memoryTypeSet = new HashSet<>();
        memoryTypesArr.forEach(je -> {
            MemoryModuleType<?> memoryType = BehaviorHelper.parseMemoryType(je);
            memoryTypeSet.add(memoryType);
        });
        memoryTypesBuilder.put(location, memoryTypeSet);
    }

    private void buildSensorTypes(ImmutableMap.Builder<ResourceLocation, Set<SensorType<?>>> sensorTypesBuilder, ResourceLocation location, JsonObject topElement) {
        JsonArray sensorTypesArr = GsonHelper.getAsJsonArray(topElement, "sensor_types");
        Set<SensorType<?>> sensorTypeSet = new HashSet<>();
        sensorTypesArr.forEach(je -> {
            SensorType<?> sensorType = BehaviorHelper.parseSensorType(je);
            sensorTypeSet.add(sensorType);
        });
        sensorTypesBuilder.put(location, sensorTypeSet);
    }
}
