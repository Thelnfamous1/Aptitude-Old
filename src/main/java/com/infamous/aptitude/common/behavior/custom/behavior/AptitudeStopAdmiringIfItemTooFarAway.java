package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;

public class AptitudeStopAdmiringIfItemTooFarAway<E extends LivingEntity> extends Behavior<E> {
   private final Predicate<E> canAdmire;
   private final int maxDistanceToItem;

   public AptitudeStopAdmiringIfItemTooFarAway(Predicate<E> canAdmire, int maxDistanceToItem) {
      super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.REGISTERED));
      this.canAdmire = canAdmire;
      this.maxDistanceToItem = maxDistanceToItem;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel serverLevel, E mob) {
      if (!this.canAdmire.test(mob)) {
         return false;
      } else {
         Optional<ItemEntity> nearestVisibleWantedItem = mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
         return nearestVisibleWantedItem.map(itemEntity -> !itemEntity.closerThan(mob, (double) this.maxDistanceToItem)).orElse(true);
      }
   }

   @Override
   protected void start(ServerLevel serverLevel, E mob, long gameTime) {
      mob.getBrain().eraseMemory(MemoryModuleType.ADMIRING_ITEM);
   }
}