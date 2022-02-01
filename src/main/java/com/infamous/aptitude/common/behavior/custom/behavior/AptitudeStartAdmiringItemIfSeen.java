package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.Ingredient;

public class AptitudeStartAdmiringItemIfSeen extends Behavior<LivingEntity> {
   private final int admireDuration;
   private final Ingredient lovedItems;

   public AptitudeStartAdmiringItemIfSeen(Ingredient lovedItems, int admireDuration) {
      super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_ABSENT, MemoryModuleType.ADMIRING_DISABLED, MemoryStatus.VALUE_ABSENT, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryStatus.VALUE_ABSENT));
      this.lovedItems = lovedItems;
      this.admireDuration = admireDuration;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity mob) {
      ItemEntity itementity = mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get();
      return this.lovedItems.test(itementity.getItem());
   }

   @Override
   protected void start(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, (long)this.admireDuration);
   }
}