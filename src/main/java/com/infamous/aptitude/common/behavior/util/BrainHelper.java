package com.infamous.aptitude.common.behavior.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.mixin.BrainAccessor;
import com.infamous.aptitude.mixin.LivingEntityAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BrainHelper {
    public static <E extends LivingEntity> void remakeBrain(E mob, ServerLevel serverLevel){
        ResourceLocation etLocation = ForgeRegistries.ENTITIES.getKey(mob.getType());

        Set<MemoryModuleType<?>> memoryTypes = Aptitude.brainManager.getMemoryTypes(etLocation);
        Set<SensorType<? extends Sensor<? super E>>> sensorTypes = Aptitude.brainManager.getSensorTypesUnchecked(etLocation);
        Map<Activity, List<Pair<Integer, JsonObject>>> prioritizedBehaviorsByActivity = Aptitude.brainManager.getPrioritizedBehaviorsByActivity(etLocation);
        Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = Aptitude.brainManager.getActivityRequirements(etLocation);
        Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = Aptitude.brainManager.getActivityMemoriesToEraseWhenStopped(etLocation);
        Set<Activity> coreActivities = Aptitude.brainManager.getCoreActivities(etLocation);
        Pair<Activity, Boolean> defaultActivity = Aptitude.brainManager.getDefaultActivity(etLocation);

        Brain<E> originalBrain = (Brain<E>) mob.getBrain();
        originalBrain.stopAll(serverLevel, mob);
        ((LivingEntityAccessor)mob).setBrain(originalBrain.copyWithoutBehaviors());
        Brain<E> newBrain = (Brain<E>)mob.getBrain();
        // Add custom memory module types and sensor types
        remakeWithCustomMemoriesAndSensors(memoryTypes, sensorTypes, newBrain);
        // Add custom behaviors
        remakeWithCustomBehaviors(prioritizedBehaviorsByActivity, activityRequirements, activityMemoriesToEraseWhenStopped, newBrain);
        newBrain.setCoreActivities(coreActivities);
        newBrain.setDefaultActivity(defaultActivity.getFirst());
        if(defaultActivity.getSecond()){
            newBrain.useDefaultActivity();
        }
    }

    private static <E extends LivingEntity> void remakeWithCustomBehaviors(Map<Activity, List<Pair<Integer, JsonObject>>> prioritizedBehaviorsByActivity, Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements, Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped, Brain<E> newBrain) {
        for(Map.Entry<Activity, List<Pair<Integer, JsonObject>>> entry : prioritizedBehaviorsByActivity.entrySet()){
            Activity activity = entry.getKey();
            List<Pair<Integer, JsonObject>> prioritizedBehaviorJsons = entry.getValue();
            ImmutableList.Builder<Pair<Integer, Behavior<? super E>>> prioritizedBehaviorsBuilder = ImmutableList.builder();
            prioritizedBehaviorJsons.forEach(p -> {
                Integer priority = p.getFirst();
                Behavior<?> behavior = BehaviorHelper.parseBehavior(p.getSecond(), "type");
                Behavior<? super E> behaviorCast = (Behavior<? super E>)behavior;
                prioritizedBehaviorsBuilder.add(Pair.of(priority, behaviorCast));
            });

            ImmutableList<Pair<Integer, Behavior<? super E>>> prioritzedBehaviors = prioritizedBehaviorsBuilder.build();
            newBrain.addActivityAndRemoveMemoriesWhenStopped(activity, prioritzedBehaviors, activityRequirements.getOrDefault(activity, Sets.newHashSet()), activityMemoriesToEraseWhenStopped.getOrDefault(activity, Sets.newHashSet()));
        }
    }

    private static <E extends LivingEntity> void remakeWithCustomMemoriesAndSensors(Set<MemoryModuleType<?>> memoryTypes, Set<SensorType<? extends Sensor<? super E>>> sensorTypes, Brain<E> brain) {
        Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> originalMemories = brain.getMemories();
        for(MemoryModuleType<?> memoryType : memoryTypes){
            if(!originalMemories.containsKey(memoryType)) originalMemories.put(memoryType, Optional.empty());
        }
        BrainAccessor<E> brainAccessor = (BrainAccessor<E>) brain;
        Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> originalSensors = brainAccessor.getSensors();
        for(SensorType<? extends Sensor<? super E>> sensorType : sensorTypes){
            if(!originalSensors.containsKey(sensorType)) originalSensors.put(sensorType, sensorType.create());
        }
        for(Sensor<? super E> sensor : originalSensors.values()) {
            for(MemoryModuleType<?> requiredMemoryType : sensor.requires()) {
                if(!originalMemories.containsKey(requiredMemoryType)) originalMemories.put(requiredMemoryType, Optional.empty());
            }
        }
    }
}
