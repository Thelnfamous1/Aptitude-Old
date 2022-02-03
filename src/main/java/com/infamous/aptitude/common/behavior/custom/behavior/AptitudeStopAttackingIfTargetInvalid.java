package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class AptitudeStopAttackingIfTargetInvalid<E extends LivingEntity> extends Behavior<E> {
   private static final long TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200L;
   private final BiPredicate<E, LivingEntity> stopAttackingWhen;
   private final Consumer<E> onTargetErased;

   public AptitudeStopAttackingIfTargetInvalid(BiPredicate<E, LivingEntity> stopAttackingWhen, Consumer<E> onTargetErased) {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED));
      this.stopAttackingWhen = stopAttackingWhen;
      this.onTargetErased = onTargetErased;
   }

   public AptitudeStopAttackingIfTargetInvalid(BiPredicate<E, LivingEntity> stopAttackingWhen) {
      this(stopAttackingWhen, (e) -> {
      });
   }

   public AptitudeStopAttackingIfTargetInvalid(Consumer<E> onTargetErased) {
      this((e, e1) -> {
         return false;
      }, onTargetErased);
   }

   public AptitudeStopAttackingIfTargetInvalid() {
      this((e, e1) -> {
         return false;
      }, (e) -> {
      });
   }

   @Override
   protected void start(ServerLevel serverLevel, E mob, long gameTime) {
      LivingEntity attackTarget = this.getAttackTarget(mob);
      if (!mob.canAttack(attackTarget)) {
         this.clearAttackTarget(mob);
      } else if (isTiredOfTryingToReachTarget(mob)) {
         this.clearAttackTarget(mob);
      } else if (this.isCurrentTargetDeadOrRemoved(mob)) {
         this.clearAttackTarget(mob);
      } else if (this.isCurrentTargetInDifferentLevel(mob)) {
         this.clearAttackTarget(mob);
      } else if (this.stopAttackingWhen.test(mob, this.getAttackTarget(mob))) {
         this.clearAttackTarget(mob);
      }
   }

   private boolean isCurrentTargetInDifferentLevel(E attacker) {
      return this.getAttackTarget(attacker).level != attacker.level;
   }

   private LivingEntity getAttackTarget(E attacker) {
      return attacker.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
   }

   private static <E extends LivingEntity> boolean isTiredOfTryingToReachTarget(E attacker) {
      Optional<Long> cantReachWalkTargetSince = attacker.getBrain().getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      return cantReachWalkTargetSince.isPresent() && attacker.level.getGameTime() - cantReachWalkTargetSince.get() > TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE;
   }

   private boolean isCurrentTargetDeadOrRemoved(E attacker) {
      Optional<LivingEntity> attackTarget = attacker.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
      return attackTarget.isPresent() && !attackTarget.get().isAlive();
   }

   protected void clearAttackTarget(E attacker) {
      this.onTargetErased.accept(attacker);
      attacker.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
   }
}