package com.infamous.aptitude.common.behavior.custom.sensor;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;

import java.util.Map;

public class AptitudeHostilesSensor extends NearestVisibleLivingEntitySensor {
   private static final ImmutableMap<EntityType<?>, Float> ACCEPTABLE_DISTANCE_FROM_HOSTILES =
           ImmutableMap.<EntityType<?>, Float>builder()
                   .put(EntityType.DROWNED, 8.0F)
                   .put(EntityType.EVOKER, 12.0F)
                   .put(EntityType.HUSK, 8.0F)
                   .put(EntityType.ILLUSIONER, 12.0F)
                   .put(EntityType.PILLAGER, 15.0F)
                   .put(EntityType.RAVAGER, 12.0F)
                   .put(EntityType.VEX, 8.0F)
                   .put(EntityType.VINDICATOR, 10.0F)
                   .put(EntityType.ZOGLIN, 10.0F)
                   .put(EntityType.ZOMBIE, 8.0F)
                   .put(EntityType.ZOMBIE_VILLAGER, 8.0F).build();

   private final Map<EntityType<?>, Float> acceptableDistanceFromHostiles;

   public AptitudeHostilesSensor(Map<EntityType<?>, Float> acceptableDistanceFromHostiles){
      this.acceptableDistanceFromHostiles = acceptableDistanceFromHostiles;
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