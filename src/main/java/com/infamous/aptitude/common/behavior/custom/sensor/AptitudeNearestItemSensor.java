package com.infamous.aptitude.common.behavior.custom.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.infamous.aptitude.common.manager.base.BaseAIHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class AptitudeNearestItemSensor extends Sensor<Mob> {
   private static final long XZ_RANGE = 8L;
   private static final long Y_RANGE = 4L;
   public static final int MAX_DISTANCE_TO_WANTED_ITEM = 9;

   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
   }

   protected void doTick(ServerLevel serverLevel, Mob mob) {
      Brain<?> brain = mob.getBrain();
      List<ItemEntity> nearestItems = serverLevel.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate((double)XZ_RANGE, (double)Y_RANGE, (double)XZ_RANGE), (ie) -> true);
      nearestItems.sort(Comparator.comparingDouble(mob::distanceToSqr));
      Optional<ItemEntity> optional = nearestItems.stream()
              .filter((ie) -> this.wantsToPickUp(mob, ie.getItem()))
              .filter((ie) -> ie.closerThan(mob, (double)MAX_DISTANCE_TO_WANTED_ITEM))
              .filter(mob::hasLineOfSight)
              .findFirst();
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, optional);
   }

   protected boolean wantsToPickUp(Mob mob, ItemStack stack){
      return BaseAIHelper.hasBaseAIFile(mob) ? BaseAIHelper.wantsToPickUp(mob, stack) : mob.wantsToPickUp(stack);
   }
}