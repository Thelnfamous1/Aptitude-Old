package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class AptitudeStopHoldingItemIfNoLongerAdmiring extends Behavior<LivingEntity> {
   private final Predicate<LivingEntity> shouldStopHoldingItem;
   private final Consumer<LivingEntity> stopHoldingItem;

   public AptitudeStopHoldingItemIfNoLongerAdmiring(Predicate<LivingEntity> shouldStopHoldingItem, Consumer<LivingEntity> stopHoldingItem) {
      super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_ABSENT));
      this.shouldStopHoldingItem = shouldStopHoldingItem;
      this.stopHoldingItem = stopHoldingItem;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity mob) {
      return this.shouldStopHoldingItem.test(mob);
   }

   protected void start(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      this.stopHoldingItem.accept(mob);
   }
}