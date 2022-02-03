package com.infamous.aptitude.common.behavior.custom.sensor;

import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;

// TODO
public class AptitudeAttackableSensor extends NearestVisibleLivingEntitySensor {
   public static final float TARGET_DETECTION_DISTANCE = 8.0F;
   private final float targetDetectionDistance;
   private final Tag<EntityType<?>> huntTargets;
   private final Tag<EntityType<?>> alwaysHostiles;
   private final boolean aquatic;

   public AptitudeAttackableSensor(boolean aquatic, float targetDetectionDistance, Tag<EntityType<?>> huntTargets, Tag<EntityType<?>> alwaysHostiles){
      this.aquatic = aquatic;
      this.targetDetectionDistance = targetDetectionDistance;
      this.huntTargets = huntTargets;
      this.alwaysHostiles = alwaysHostiles;
   }

   @Override
   protected boolean isMatchingEntity(LivingEntity attacker, LivingEntity target) {
      return this.isClose(attacker, target)
              && (!this.aquatic || target.isInWaterOrBubble())
              && (this.isHostileTarget(target) || this.isHuntTarget(attacker, target))
              && Sensor.isEntityAttackable(attacker, target);
   }

   private boolean isHuntTarget(LivingEntity attacker, LivingEntity target) {
      return !attacker.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && this.huntTargets.contains(target.getType());
   }

   private boolean isHostileTarget(LivingEntity target) {
      return this.alwaysHostiles.contains(target.getType());
   }

   private boolean isClose(LivingEntity attacker, LivingEntity target) {
      return target.distanceToSqr(attacker) <= (double)(this.targetDetectionDistance * this.targetDetectionDistance);
   }

   @Override
   protected MemoryModuleType<LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_ATTACKABLE;
   }
}