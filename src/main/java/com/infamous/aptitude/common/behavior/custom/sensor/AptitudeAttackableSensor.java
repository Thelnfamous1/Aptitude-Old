package com.infamous.aptitude.common.behavior.custom.sensor;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;

// TODO
public class AptitudeAttackableSensor extends NearestVisibleLivingEntitySensor {
   public static final float TARGET_DETECTION_DISTANCE = 8.0F;

   @Override
   protected boolean isMatchingEntity(LivingEntity attacker, LivingEntity target) {
      return this.isClose(attacker, target)
              && target.isInWaterOrBubble()
              && (this.isHostileTarget(target) || this.isHuntTarget(attacker, target))
              && Sensor.isEntityAttackable(attacker, target);
   }

   private boolean isHuntTarget(LivingEntity attacker, LivingEntity target) {
      return !attacker.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && EntityTypeTags.AXOLOTL_HUNT_TARGETS.contains(target.getType());
   }

   private boolean isHostileTarget(LivingEntity target) {
      return EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES.contains(target.getType());
   }

   private boolean isClose(LivingEntity attacker, LivingEntity target) {
      return target.distanceToSqr(attacker) <= (double)(TARGET_DETECTION_DISTANCE * TARGET_DETECTION_DISTANCE);
   }

   @Override
   protected MemoryModuleType<LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_ATTACKABLE;
   }
}