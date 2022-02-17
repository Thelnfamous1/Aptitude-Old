package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class AptitudePlayDead extends Behavior<LivingEntity> {
   private final Predicate<LivingEntity> canPlayDead;
   private final Consumer<LivingEntity> startPlayingDead;

   public AptitudePlayDead(Predicate<LivingEntity> canPlayDead, Consumer<LivingEntity> startPlayingDead, int duration) {
      super(ImmutableMap.of(MemoryModuleType.PLAY_DEAD_TICKS, MemoryStatus.VALUE_PRESENT, MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_PRESENT), duration);
      this.canPlayDead = canPlayDead;
      this.startPlayingDead = startPlayingDead;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity mob) {
      return this.canPlayDead.test(mob);
   }

   protected boolean canStillUse(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      return this.canPlayDead.test(mob) && mob.getBrain().hasMemoryValue(MemoryModuleType.PLAY_DEAD_TICKS);
   }

   protected void start(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      Brain<?> brain = mob.getBrain();
      brain.eraseMemory(MemoryModuleType.WALK_TARGET);
      brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
      this.startPlayingDead.accept(mob);
   }
}