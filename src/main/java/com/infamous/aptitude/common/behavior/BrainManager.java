package com.infamous.aptitude.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
