package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class AptitudeValidatePlayDead extends Behavior<LivingEntity> {
   public AptitudeValidatePlayDead() {
      super(ImmutableMap.of(MemoryModuleType.PLAY_DEAD_TICKS, MemoryStatus.VALUE_PRESENT));
   }

   protected void start(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      Brain<?> brain = mob.getBrain();
      int i = brain.getMemory(MemoryModuleType.PLAY_DEAD_TICKS).get();
      if (i <= 0) {
         brain.eraseMemory(MemoryModuleType.PLAY_DEAD_TICKS);
         brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
         brain.useDefaultActivity();
      } else {
         brain.setMemory(MemoryModuleType.PLAY_DEAD_TICKS, i - 1);
      }

   }
}