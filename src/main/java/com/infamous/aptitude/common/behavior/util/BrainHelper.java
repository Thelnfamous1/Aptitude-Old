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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BrainHelper {

    public static void clearAIAndRemakeBrain(Mob mob, ServerLevel serverLevel) {
        mob.goalSelector.removeAllGoals();
        mob.targetSelector.removeAllGoals();
        remakeBrain(mob, serverLevel);
    }

    public static <E extends LivingEntity> void remakeBrain(E mob, ServerLevel serverLevel){
        ResourceLocation etLocation = ForgeRegistries.ENTITIES.getKey(mob.getType());

        Set<MemoryModuleType<?>> memoryTypes = Aptitude.brainManager.getMemoryTypes(etLocation);
        Set<SensorType<? extends Sensor<?>>> sensorTypes = Aptitude.brainManager.getSensorTypes(etLocation);
        Map<Activity, List<Pair<Integer, JsonObject>>> prioritizedBehaviorsByActivity = Aptitude.brainManager.getPrioritizedBehaviorsByActivity(etLocation);
        Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = Aptitude.brainManager.getActivityRequirements(etLocation);
        Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = Aptitude.brainManager.getActivityMemoriesToEraseWhenStopped(etLocation);
        Set<Activity> coreActivities = Aptitude.brainManager.getCoreActivities(etLocation);
        Pair<Activity, Boolean> defaultActivity = Aptitude.brainManager.getDefaultActivity(etLocation);

        Brain<E> newBrain = replaceBrain(mob, serverLevel);
        // Add custom memory module types and sensor types
        remakeWithCustomMemoriesAndSensors(memoryTypes, sensorTypes, newBrain);
        // Add custom behaviors
        remakeWithCustomBehaviors(prioritizedBehaviorsByActivity, activityRequirements, activityMemoriesToEraseWhenStopped, newBrain);
        newBrain.setCoreActivities(coreActivities);
        newBrain.setDefaultActivity(defaultActivity.getFirst());
        if(defaultActivity.getSecond()){
            newBrain.useDefaultActivity();
        }


        Aptitude.LOGGER.info("New brain created for {}!", mob);
        Aptitude.LOGGER.info("Printing memory types!");
        newBrain.getMemories().keySet().forEach(Aptitude.LOGGER::info);
        Aptitude.LOGGER.info("Printing sensor types!");
        Map<SensorType<? extends Sensor<?>>, Sensor<?>> sensors = ((BrainAccessor<E>) newBrain).getSensors();
        sensors.keySet().forEach(st -> Aptitude.LOGGER.info(st.getRegistryName().toString()));

    }

    private static <E extends LivingEntity> Brain<E> replaceBrain(E mob, ServerLevel serverLevel) {
        Brain<E> originalBrain = (Brain<E>) mob.getBrain();
        originalBrain.stopAll(serverLevel, mob);
        ((LivingEntityAccessor) mob).setBrain(originalBrain.copyWithoutBehaviors());
        Brain<E> newBrain = (Brain<E>) mob.getBrain();
        return newBrain;
    }

    private static <E extends LivingEntity> void remakeWithCustomBehaviors(Map<Activity, List<Pair<Integer, JsonObject>>> prioritizedBehaviorsByActivity, Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements, Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped, Brain<E> newBrain) {
        for(Map.Entry<Activity, List<Pair<Integer, JsonObject>>> entry : prioritizedBehaviorsByActivity.entrySet()){
            Activity activity = entry.getKey();
            List<Pair<Integer, JsonObject>> prioritizedBehaviorJsons = entry.getValue();
            ImmutableList.Builder<Pair<Integer, Behavior<? super E>>> prioritizedBehaviorsBuilder = ImmutableList.builder();
            prioritizedBehaviorJsons.forEach(p -> {
                //Aptitude.LOGGER.info("Reading in behavior object to make brain: {}", p.getSecond());
                Integer priority = p.getFirst();
                Behavior<?> behavior = BehaviorHelper.parseBehavior(p.getSecond(), "type");
                Behavior<? super E> behaviorCast = (Behavior<? super E>)behavior;
                prioritizedBehaviorsBuilder.add(Pair.of(priority, behaviorCast));
            });

            ImmutableList<Pair<Integer, Behavior<? super E>>> prioritzedBehaviors = prioritizedBehaviorsBuilder.build();
            prioritzedBehaviors.forEach(pb -> Aptitude.LOGGER.info("Adding {} with priority {} for activity {}", pb.getSecond(), pb.getFirst(), activity));
            newBrain.addActivityAndRemoveMemoriesWhenStopped(activity, prioritzedBehaviors, activityRequirements.getOrDefault(activity, Sets.newHashSet()), activityMemoriesToEraseWhenStopped.getOrDefault(activity, Sets.newHashSet()));

            /*
            Map<Integer, Map<Activity, Set<Behavior<?>>>> availableBehaviorsByPriority = ((BrainAccessor<E>) newBrain).getAvailableBehaviorsByPriority();
            for(Map.Entry<Integer, Map<Activity, Set<Behavior<?>>>> abbpEntry : availableBehaviorsByPriority.entrySet()){
                Aptitude.LOGGER.info("Checking available behaviors for priority: {}", abbpEntry.getKey());
                Map<Activity, Set<Behavior<?>>> activitiesToBehaviors = abbpEntry.getValue();
                if(activitiesToBehaviors.containsKey(activity)){
                    Set<Behavior<?>> behaviors = activitiesToBehaviors.get(activity);
                    behaviors.forEach(b -> Aptitude.LOGGER.info("Activity {} has behavior: {}", activity, b));
                }
            }
             */
        }
    }

    private static <E extends LivingEntity> void remakeWithCustomMemoriesAndSensors(Set<MemoryModuleType<?>> memoryTypes, Set<SensorType<? extends Sensor<?>>> sensorTypes, Brain<E> brain) {
        Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> originalMemories = brain.getMemories();
        for(MemoryModuleType<?> memoryType : memoryTypes){
            if(!originalMemories.containsKey(memoryType)) originalMemories.put(memoryType, Optional.empty());
        }
        BrainAccessor<E> brainAccessor = (BrainAccessor<E>) brain;
        Map<SensorType<? extends Sensor<?>>, Sensor<?>> originalSensors = brainAccessor.getSensors();
        for(SensorType<? extends Sensor<?>> sensorType : sensorTypes){
            if(!originalSensors.containsKey(sensorType)) originalSensors.put(sensorType, sensorType.create());
        }
        for(Sensor<?> sensor : originalSensors.values()) {
            for(MemoryModuleType<?> requiredMemoryType : sensor.requires()) {
                if(!originalMemories.containsKey(requiredMemoryType)) originalMemories.put(requiredMemoryType, Optional.empty());
            }
        }
    }

    public static <E extends Mob> void updateActivity(E mob) {
        Brain<E> brain = getBrainCast(mob);
        ResourceLocation etLocation = ForgeRegistries.ENTITIES.getKey(mob.getType());
        List<Activity> rotatingActivities = Aptitude.brainManager.getRotatingActivities(etLocation);

        //brain.getActiveActivities().forEach(a -> Aptitude.LOGGER.info("Was running activity: {}", a));
        //brain.getRunningBehaviors().forEach(b -> Aptitude.LOGGER.info("Running behavior: {}", b));

        brain.setActiveActivityToFirstValid(rotatingActivities);
        mob.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));

        //brain.getActiveActivities().forEach(a -> Aptitude.LOGGER.info("Now running activity: {}", a));
    }

    public static<E extends Mob> Brain<E> getBrainCast(E mob) {
        return (Brain<E>) mob.getBrain();
    }
}
