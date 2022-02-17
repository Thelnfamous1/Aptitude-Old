package com.infamous.aptitude.common.behavior.custom.sensor;

import com.google.gson.JsonObject;
import com.infamous.aptitude.common.behavior.custom.JsonFriendly;
import com.infamous.aptitude.common.behavior.util.PredicateHelper;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.function.BiPredicate;

public class AptitudeAttackableSensor extends NearestVisibleLivingEntitySensor implements JsonFriendly<AptitudeAttackableSensor> {
   private static final float TARGET_DETECTION_DISTANCE_DEFAULT = 8.0F;
   private float targetDetectionDistance;
   private BiPredicate<LivingEntity, LivingEntity> isAttackable;

   public AptitudeAttackableSensor(){
      this.targetDetectionDistance = TARGET_DETECTION_DISTANCE_DEFAULT;
      this.isAttackable = (le, le1) -> false;
   }

   @Override
   public AptitudeAttackableSensor fromJson(JsonObject jsonObject) {
      this.targetDetectionDistance = GsonHelper.getAsFloat(jsonObject, "targetDetectionDistance", TARGET_DETECTION_DISTANCE_DEFAULT);
      this.isAttackable = PredicateHelper.parseBiPredicate(jsonObject, "isAttackable", "type");
      return this;
   }

   @Override
   protected boolean isMatchingEntity(LivingEntity attacker, LivingEntity target) {
      return this.isClose(attacker, target)
              && this.isAttackable(attacker, target)
              && Sensor.isEntityAttackable(attacker, target);
   }

   private boolean isAttackable(LivingEntity attacker, LivingEntity target) {
      return this.isAttackable.test(attacker, target);
   }

   private boolean isClose(LivingEntity attacker, LivingEntity target) {
      return target.distanceToSqr(attacker) <= (double)(this.targetDetectionDistance * this.targetDetectionDistance);
   }

   @Override
   protected MemoryModuleType<LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_ATTACKABLE;
   }
}