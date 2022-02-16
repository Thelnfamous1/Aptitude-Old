package com.infamous.aptitude.common.behavior.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.custom.JsonFriendly;
import com.infamous.aptitude.common.behavior.custom.sensor.CustomSensorType;
import com.infamous.aptitude.common.util.AptitudeResources;
import com.infamous.aptitude.mixin.BrainAccessor;
import com.infamous.aptitude.mixin.LivingEntityAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class BrainHelper {

    public static <E extends LivingEntity> void remakeBrain(E mob, ServerLevel serverLevel){
        ResourceLocation etLocation = mob.getType().getRegistryName();

        Set<MemoryModuleType<?>> memoryTypes = Aptitude.brainManager.getMemoryTypes(etLocation);
        Set<SensorType<? extends Sensor<?>>> sensorTypes = Aptitude.brainManager.getSensorTypes(etLocation);
        Map<CustomSensorType<? extends Sensor<?>>, JsonObject> customSensorTypes = Aptitude.brainManager.getCustomSensorTypes(etLocation);
        Map<Activity, List<Pair<Integer, JsonObject>>> prioritizedBehaviorsByActivity = Aptitude.brainManager.getPrioritizedBehaviorsByActivity(etLocation);
        Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = Aptitude.brainManager.getActivityRequirements(etLocation);
        Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = Aptitude.brainManager.getActivityMemoriesToEraseWhenStopped(etLocation);
        Set<Activity> coreActivities = Aptitude.brainManager.getCoreActivities(etLocation);
        Activity defaultActivity = Aptitude.brainManager.getDefaultActivity(etLocation);

        Consumer<LivingEntity> brainCreationCallback = Aptitude.brainManager.getBrainCreationCallback(etLocation);

        Brain<E> brain = Aptitude.brainManager.replaceBrain(etLocation) ? replaceBrain(mob, serverLevel) : getBrainCast(mob);
        // Add custom memory module types and sensor types
        remakeWithCustomMemoriesAndSensors(memoryTypes, sensorTypes, customSensorTypes, brain);
        // Add custom behaviors
        remakeWithCustomBehaviors(prioritizedBehaviorsByActivity, activityRequirements, activityMemoriesToEraseWhenStopped, brain);
        brain.setCoreActivities(coreActivities);
        brain.setDefaultActivity(defaultActivity);
        brainCreationCallback.accept(mob); //brain.useDefaultActivity();

    }

    public static <E extends LivingEntity> void debugNewBrainCreation(E mob, Brain<E> newBrain) {
        Aptitude.LOGGER.info("New brain created for {}!", mob);
        Aptitude.LOGGER.info("Printing memory types!");
        newBrain.getMemories().keySet().forEach(Aptitude.LOGGER::info);
        Aptitude.LOGGER.info("Printing sensor types!");
        Map<SensorType<? extends Sensor<?>>, Sensor<?>> sensors = ((BrainAccessor<E>) newBrain).getSensors();
        sensors.keySet().forEach(st -> Aptitude.LOGGER.info(st.getRegistryName().toString()));
    }

    public static <E extends LivingEntity> Brain<E> replaceBrain(E mob, ServerLevel serverLevel) {
        Brain<E> originalBrain = (Brain<E>) mob.getBrain();
        originalBrain.stopAll(serverLevel, mob);
        ((LivingEntityAccessor) mob).setBrain(originalBrain.copyWithoutBehaviors());
        Brain<E> newBrain = (Brain<E>) mob.getBrain();
        return newBrain;
    }

    public static <E extends LivingEntity> void remakeWithCustomBehaviors(Map<Activity, List<Pair<Integer, JsonObject>>> prioritizedBehaviorsByActivity, Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements, Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped, Brain<E> newBrain) {
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

    public static <E extends LivingEntity> void debugBehaviorsForActivity(BrainAccessor<E> newBrain, Activity activity) {
        Map<Integer, Map<Activity, Set<Behavior<?>>>> availableBehaviorsByPriority = newBrain.getAvailableBehaviorsByPriority();
        for(Map.Entry<Integer, Map<Activity, Set<Behavior<?>>>> abbpEntry : availableBehaviorsByPriority.entrySet()){
            Aptitude.LOGGER.info("Checking available behaviors for priority: {}", abbpEntry.getKey());
            Map<Activity, Set<Behavior<?>>> activitiesToBehaviors = abbpEntry.getValue();
            if(activitiesToBehaviors.containsKey(activity)){
                Set<Behavior<?>> behaviors = activitiesToBehaviors.get(activity);
                behaviors.forEach(b -> Aptitude.LOGGER.info("Activity {} has behavior: {}", activity, b));
            }
        }
    }

    public static <E extends LivingEntity> void remakeWithCustomMemoriesAndSensors(Set<MemoryModuleType<?>> memoryTypes, Set<SensorType<? extends Sensor<?>>> sensorTypes, Map<CustomSensorType<? extends Sensor<?>>, JsonObject> customSensorTypes, Brain<E> brain) {
        Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> originalMemories = brain.getMemories();
        for(MemoryModuleType<?> memoryType : memoryTypes){
            if(!originalMemories.containsKey(memoryType)) originalMemories.put(memoryType, Optional.empty());
        }
        BrainAccessor<E> brainAccessor = (BrainAccessor<E>) brain;
        Map<SensorType<? extends Sensor<?>>, Sensor<?>> sensors = brainAccessor.getSensors();
        // "vanilla" sensors
        for(SensorType<? extends Sensor<?>> sensorType : sensorTypes){
            if(!sensors.containsKey(sensorType)) sensors.put(sensorType, sensorType.create());
        }
        // "custom" JSON-driven sensors
        for(Map.Entry<CustomSensorType<? extends Sensor<?>>, JsonObject> customSensorEntry : customSensorTypes.entrySet()){
            CustomSensorType<? extends Sensor<?>> customSensorType = customSensorEntry.getKey();
            JsonObject customSensorObj = customSensorEntry.getValue();
            if(!sensors.containsKey(customSensorType)){
                sensors.put(customSensorType, customSensorType.createWithJson(customSensorObj));
            }
        }
        for(Sensor<?> sensor : sensors.values()) {
            for(MemoryModuleType<?> requiredMemoryType : sensor.requires()) {
                if(!originalMemories.containsKey(requiredMemoryType)) originalMemories.put(requiredMemoryType, Optional.empty());
            }
        }
    }

    public static <E extends LivingEntity> void updateActivity(E mob) {
        ResourceLocation etLocation = mob.getType().getRegistryName();
        Consumer<LivingEntity> updateActivityCallback = Aptitude.brainManager.getUpdateActivityCallback(etLocation);
        updateActivityCallback.accept(mob);
    }

    public static<E extends LivingEntity> Brain<E> getBrainCast(E mob) {
        return (Brain<E>) mob.getBrain();
    }

    public static boolean shouldTickBrain(Mob mob) {
        return !mob.getType().is(AptitudeResources.EXCLUDE_BRAIN_TICK);
    }

    public static boolean hasBrainFile(LivingEntity mob) {
        ResourceLocation etLocation = mob.getType().getRegistryName();
        return Aptitude.brainManager.hasBrainEntry(etLocation);
    }
}
