package com.infamous.aptitude.common.manager.brain;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.infamous.aptitude.common.behavior.custom.sensor.CustomSensorType;
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

import java.util.*;
import java.util.function.Consumer;

public class BrainManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<ResourceLocation, BrainContainer> brainContainers = ImmutableMap.of();

    public BrainManager() {
        super(GSON, "aptitude/brain");
    }

    public boolean hasBrainEntry(ResourceLocation location){
        return this.brainContainers.containsKey(location);
    }

    public boolean replaceBrain(ResourceLocation location){
        return this.getBrainContainer(location).replaceBrain();
    }

    public Set<MemoryModuleType<?>> getMemoryTypes(ResourceLocation location) {
        return this.getBrainContainer(location).getMemoryTypes();
    }

    public Set<SensorType<? extends Sensor<?>>> getSensorTypes(ResourceLocation location) {
        return this.getBrainContainer(location).getSensorTypes();
    }

    public Map<CustomSensorType<? extends Sensor<?>>, JsonObject> getCustomSensorTypes(ResourceLocation location) {
        return this.getBrainContainer(location).getCustomSensorTypes();
    }

    public Map<Activity, List<Pair<Integer, JsonObject>>> getPrioritizedBehaviorsByActivity(ResourceLocation location) {
        return this.getBrainContainer(location).getPrioritizedBehaviorsByActivity();
    }

    public Set<Activity> getCoreActivities(ResourceLocation location) {
        return this.getBrainContainer(location).getCoreActivities();
    }

    public Activity getDefaultActivity(ResourceLocation location) {
        return this.getBrainContainer(location).getDefaultActivity();
    }

    public Consumer<LivingEntity> getBrainCreationCallback(ResourceLocation location){
        return this.getBrainContainer(location).getBrainCreationCallback();
    }

    public Map<Activity, Set<MemoryModuleType<?>>> getActivityMemoriesToEraseWhenStopped(ResourceLocation location){
        return this.getBrainContainer(location).getActivityMemoriesToEraseWhenStopped();
    }

    public Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> getActivityRequirements(ResourceLocation location) {
        return this.getBrainContainer(location).getActivityRequirements();
    }

    public List<Activity> getRotatingActivities(ResourceLocation location) {
        return this.getBrainContainer(location).getRotatingActivities();
    }

    public Consumer<LivingEntity> getUpdateActivityCallback(ResourceLocation location) {
        return this.getBrainContainer(location).getUpdateActivityCallback();
    }

    private BrainContainer getBrainContainer(ResourceLocation resourceLocation){
        return this.brainContainers.getOrDefault(resourceLocation, BrainContainer.EMPTY);
    }

    protected void apply(Map<ResourceLocation, JsonElement> locationElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder<ResourceLocation, BrainContainer> brainContainerBuilder = ImmutableMap.builder();

        locationElementMap.forEach((location, jsonElement) -> {
            try{
                JsonObject topElement = GsonHelper.convertToJsonObject(jsonElement, "top element");
                BrainContainer brainContainer = BrainContainer.of(topElement);
                brainContainerBuilder.put(location, brainContainer);
            } catch (Exception exception) {
                LOGGER.error("Couldn't parse brain for {}", location, exception);
            }
        });

        this.brainContainers = brainContainerBuilder.build();
    }
}
