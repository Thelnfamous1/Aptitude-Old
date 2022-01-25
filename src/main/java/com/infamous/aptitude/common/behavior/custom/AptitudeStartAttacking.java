package com.infamous.aptitude.common.behavior.custom;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class AptitudeStartAttacking<E extends LivingEntity> extends Behavior<E> {
   private final Predicate<E> canAttackPredicate;
   private final Function<E, Optional<? extends LivingEntity>> targetFinderFunction;

   public AptitudeStartAttacking(Predicate<E> canAttackPredicate, Function<E, Optional<? extends LivingEntity>> targetFinderFunction) {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED));
      this.canAttackPredicate = canAttackPredicate;
      this.targetFinderFunction = targetFinderFunction;
   }

   public AptitudeStartAttacking(Function<E, Optional<? extends LivingEntity>> targetFinderFunction) {
      this((p_24212_) -> {
         return true;
      }, targetFinderFunction);
   }

   public boolean checkExtraStartConditions(ServerLevel serverLevel, E mob) {
      if (!this.canAttackPredicate.test(mob)) {
         return false;
      } else {
         Optional<? extends LivingEntity> optional = this.targetFinderFunction.apply(mob);
         return optional.isPresent() && mob.canAttack(optional.get());
      }
   }

   public void start(ServerLevel serverLevel, E mob, long gameTime) {
      this.targetFinderFunction.apply(mob).ifPresent((le) -> {
         this.setAttackTarget(mob, le);
      });
   }

   private void setAttackTarget(E mob, LivingEntity target) {
      mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
      mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
   }
}