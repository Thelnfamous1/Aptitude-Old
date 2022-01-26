package com.infamous.aptitude.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;

public class BrainManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<ResourceLocation, Set<MemoryModuleType<?>>> memoryTypes = ImmutableMap.of();
    private Map<ResourceLocation, Set<SensorType<?>>> sensorTypes = ImmutableMap.of();
    private Map<ResourceLocation, Map<Integer, Map<Activity, Set<BehaviorType<?>>>>> activitiesByPriority = ImmutableMap.of();

    public BrainManager() {
        super(GSON, "brain");
    }

    public Set<MemoryModuleType<?>> getMemoryTypes(ResourceLocation location) {
        return this.memoryTypes.getOrDefault(location, ImmutableSet.of());
    }

    public <E extends LivingEntity> Set<SensorType<? extends Sensor<? super E>>> getSensorTypesUnchecked(ResourceLocation location) {
        return (Set<SensorType<? extends Sensor<? super E>>>) this.sensorTypes.getOrDefault(location, ImmutableSet.of());
    }

    public Map<Integer, Map<Activity, Set<BehaviorType<?>>>> getActivitiesByPriority(ResourceLocation location) {
        return this.activitiesByPriority.getOrDefault(location, ImmutableMap.of());
    }

    protected void apply(Map<ResourceLocation, JsonElement> locationElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder<ResourceLocation, Set<MemoryModuleType<?>>> memoryTypesBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, Set<SensorType<? extends Sensor<?>>>> sensorTypesBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, Map<Integer, Map<Activity, Set<BehaviorType<?>>>>> activitiesByPriorityBuilder = ImmutableMap.builder();

        locationElementMap.forEach((location, jsonElement) -> {
            try (Resource res = resourceManager.getResource(getPreparedPath(location))){
                //BehaviorType<?> loottable = net.minecraftforge.common.ForgeHooks.loadLootTable(GSON, location, jsonElement, res == null || !res.getSourceName().equals("Default"), this);
                //builder.put(location, loottable);
            } catch (Exception exception) {
                LOGGER.error("Couldn't parse brain for {}", location, exception);
            }

        });
        this.memoryTypes = memoryTypesBuilder.build();
        this.sensorTypes = sensorTypesBuilder.build();
        this.activitiesByPriority = activitiesByPriorityBuilder.build();
    }
}
