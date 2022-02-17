package com.infamous.aptitude.common.behavior.custom.sensor;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.infamous.aptitude.common.behavior.custom.JsonFriendly;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;

import java.util.HashMap;
import java.util.Map;

public class AptitudeHostilesSensor extends NearestVisibleLivingEntitySensor implements JsonFriendly<AptitudeHostilesSensor> {

   private Map<EntityType<?>, Float> acceptableDistanceFromHostiles;

   public AptitudeHostilesSensor(){
      this.acceptableDistanceFromHostiles = ImmutableMap.of();
   }

   @Override
   public AptitudeHostilesSensor fromJson(JsonObject jsonObject) {
      ImmutableMap.Builder<EntityType<?>, Float> acceptableDistanceFromHostilesBuilder = ImmutableMap.builder();
      JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "acceptableDistanceFromHostiles");
      jsonArray.forEach(jsonElement -> {
         JsonObject elemObj = jsonElement.getAsJsonObject();
         EntityType<?> entityType = BehaviorHelper.parseEntityType(elemObj, "hostile");
         Float acceptableDistance = GsonHelper.getAsFloat(elemObj, "acceptable_distance");
         acceptableDistanceFromHostilesBuilder.put(entityType, acceptableDistance);
      });
      this.acceptableDistanceFromHostiles = acceptableDistanceFromHostilesBuilder.build();
      return this;
   }

   @Override
   protected boolean isMatchingEntity(LivingEntity searcher, LivingEntity target) {
      return this.isHostile(target) && this.isClose(searcher, target);
   }

   private boolean isClose(LivingEntity searcher, LivingEntity target) {
      float acceptableDistance = this.acceptableDistanceFromHostiles.get(target.getType());
      return target.distanceToSqr(searcher) <= (double)(acceptableDistance * acceptableDistance);
   }

   @Override
   protected MemoryModuleType<LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_HOSTILE;
   }

   private boolean isHostile(LivingEntity target) {
      return this.acceptableDistanceFromHostiles.containsKey(target.getType());
   }
}