package com.infamous.aptitude.common.behavior;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.function.Function;

public class BehaviorFunctions {

    private static Optional<? extends LivingEntity> notBreedingThenNearestAttackable(LivingEntity livingEntity) {
        return BehaviorPredicates.isBreeding(livingEntity) ? Optional.empty() : livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
    }

    private static Optional<? extends LivingEntity> nearestNotCreeper(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty()).findClosest(le -> !(le instanceof Creeper));
    }

    private static Optional<? extends LivingEntity> notPacifiedNotBreedingThenNearestPlayer(LivingEntity livingEntity) {
        return !BehaviorPredicates.isPacified(livingEntity) && !BehaviorPredicates.isBreeding(livingEntity) ? livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) : Optional.empty();
    }

    private static Optional<? extends LivingEntity> notNearZombieThenAngryAtOrPlayerRevengeOrNemesisOrPlayerNotGilded(LivingEntity livingEntity) {
        if (isNearZombified(livingEntity)) {
            return Optional.empty();
        } else {
            Optional<LivingEntity> angryAt = getAngryAt(livingEntity);
            if(angryAt.isPresent()){
                return angryAt;
            } else {
                Optional<Player> playerRevenge = getPlayerRevenge(livingEntity);
                if (playerRevenge.isPresent()) return playerRevenge;

                Optional<Mob> optionalNemesis = getNemesis(livingEntity);
                if (optionalNemesis.isPresent()) {
                    return optionalNemesis;
                } else {
                    return getPlayerNotGilded(livingEntity);
                }
            }
        }
    }

    private static Optional<Mob> getNemesis(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
    }

    private static Optional<? extends LivingEntity> getPlayerNotGilded(LivingEntity livingEntity) {
        Brain<?> brain = livingEntity.getBrain();
        Optional<Player> optionalPlayerNotGilded = brain.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
        return optionalPlayerNotGilded.isPresent() && Sensor.isEntityAttackable(livingEntity, optionalPlayerNotGilded.get()) ? optionalPlayerNotGilded : Optional.empty();
    }

    private static Optional<LivingEntity> getAngryAt(LivingEntity livingEntity){
        Optional<LivingEntity> optionalAngryAt = BehaviorUtils.getLivingEntityFromUUIDMemory(livingEntity, MemoryModuleType.ANGRY_AT);
        return optionalAngryAt.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(livingEntity, optionalAngryAt.get()) ? optionalAngryAt : Optional.empty();
    }

    private static Optional<Player> getPlayerRevenge(LivingEntity livingEntity) {
        Brain<?> brain = livingEntity.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
            Optional<Player> optionalPlayer = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
            if (optionalPlayer.isPresent()) {
                return optionalPlayer;
            }
        }
        return Optional.empty();
    }

    private static Optional<LivingEntity> getRevenge(Brain<?> brain) {
        if (brain.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
            Optional<LivingEntity> optionalTarget = brain.getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
            if (optionalTarget.isPresent()) {
                return optionalTarget;
            }
        }
        return Optional.empty();
    }

    private static boolean isNearZombified(LivingEntity livingEntity) {
        Brain<?> brain = livingEntity.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
            LivingEntity zombified = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get();
            return livingEntity.closerThan(zombified, 6.0D);
        } else {
            return false;
        }
    }

    static Function<Mob, Optional<? extends LivingEntity>> functionFromJson(JsonObject targetFinderFunctionObj) {
        JsonObject evaluateMemoryObj = GsonHelper.getAsJsonObject(targetFinderFunctionObj, "evaluate_memory");
        MemoryModuleType<?> memoryType = BehaviorHelper.parseMemoryType(evaluateMemoryObj, "type");

        Map<Set<EntityType<?>>, Boolean> entityTypeFilters = new HashMap<>();
        JsonArray filters = GsonHelper.getAsJsonArray(evaluateMemoryObj, "filters");
        filters.forEach(je -> {
            JsonObject elementObject = je.getAsJsonObject();
            String mode = GsonHelper.getAsString(elementObject, "mode", "");
            Boolean negate = GsonHelper.getAsBoolean(elementObject, "negate", false);
            JsonArray values = GsonHelper.getAsJsonArray(elementObject, "values");

            if(mode.toLowerCase(Locale.ROOT).equals("entity_type")){
                Set<EntityType<?>> typesForFilter = new HashSet<>();
                values.forEach(je1 -> {
                    EntityType<?> entityType = BehaviorHelper.parseEntityTypeFromElement(je1);
                    typesForFilter.add(entityType);
                });
                entityTypeFilters.put(typesForFilter, negate);
            }
        });

        return mob -> {
            Optional<?> memory = mob.getBrain().getMemory(memoryType);
            if(memory.isPresent()){
                Object memoryValue = memory.get();
                if(memoryValue instanceof NearestVisibleLivingEntities nvle){
                    return nvle.findClosest(le -> {
                        boolean entityTypeCheck = true;
                        for(Map.Entry<Set<EntityType<?>>, Boolean> entry : entityTypeFilters.entrySet()){
                            Set<EntityType<?>> typesToCheck = entry.getKey();
                            Boolean negate = entry.getValue();
                            boolean contained = typesToCheck.contains(le.getType());
                            entityTypeCheck = negate != contained;
                            if(!entityTypeCheck){
                                break;
                            }
                        }
                        return entityTypeCheck;
                    });
                } else if(memoryValue instanceof LivingEntity le){
                    return Optional.of(le);
                }
            }
            return Optional.empty();
        };
    }

}
