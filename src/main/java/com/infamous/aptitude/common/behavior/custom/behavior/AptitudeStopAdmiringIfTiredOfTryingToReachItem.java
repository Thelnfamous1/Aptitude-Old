package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class AptitudeStopAdmiringIfTiredOfTryingToReachItem<E extends LivingEntity> extends Behavior<E> {
   private final Predicate<E> canAdmire;
   private final int maxTimeToReachItem;
   private final int disableTime;

   public AptitudeStopAdmiringIfTiredOfTryingToReachItem(Predicate<E> canAdmire, int maxTimeToReachItem, int disableTime) {
      super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT, MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, MemoryStatus.REGISTERED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryStatus.REGISTERED));
      this.canAdmire = canAdmire;
      this.maxTimeToReachItem = maxTimeToReachItem;
      this.disableTime = disableTime;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel serverLevel, E mob) {
      return this.canAdmire.test(mob);
   }

   @Override
   protected void start(ServerLevel serverLevel, E mob, long gameTime) {
      Brain<?> brain = mob.getBrain();
      Optional<Integer> timeTryingToReachAdmireItem = brain.getMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
      if (timeTryingToReachAdmireItem.isEmpty()) {
         brain.setMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, 0);
      } else {
         int time = timeTryingToReachAdmireItem.get();
         if (time > this.maxTimeToReachItem) {
            brain.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
            brain.eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            brain.setMemoryWithExpiry(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, true, (long)this.disableTime);
         } else {
            brain.setMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, time + 1);
         }
      }

   }
}