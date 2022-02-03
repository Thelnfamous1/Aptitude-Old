package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableMap;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraftforge.common.util.TriPredicate;

public class AptitudeSetWalkTargetFromAttackTargetIfTargetOutOfReach extends Behavior<LivingEntity> {
   private final Function<LivingEntity, Float> speedModifier;
   private final BiPredicate<LivingEntity, LivingEntity> isWithinAttackRange;

   public AptitudeSetWalkTargetFromAttackTargetIfTargetOutOfReach(float speedModifierRaw, BiPredicate<LivingEntity, LivingEntity> isWithinAttackRange) {
      this((livingEntity) -> speedModifierRaw, isWithinAttackRange);
   }

   public AptitudeSetWalkTargetFromAttackTargetIfTargetOutOfReach(Function<LivingEntity, Float> speedModifier, BiPredicate<LivingEntity, LivingEntity> isWithinAttackRange) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.REGISTERED));
      this.speedModifier = speedModifier;
      this.isWithinAttackRange = isWithinAttackRange;
   }

   @Override
   protected void start(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      LivingEntity attackTarget = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
      if (BehaviorUtils.canSee(mob, attackTarget) && this.isWithinAttackRange.test(mob, attackTarget)) {
         this.clearWalkTarget(mob);
      } else {
         this.setWalkAndLookTarget(mob, attackTarget);
      }

   }

   private void setWalkAndLookTarget(LivingEntity attacker, LivingEntity target) {
      Brain<?> brain = attacker.getBrain();
      brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
      WalkTarget walktarget = new WalkTarget(new EntityTracker(target, false), this.speedModifier.apply(attacker), 0);
      brain.setMemory(MemoryModuleType.WALK_TARGET, walktarget);
   }

   private void clearWalkTarget(LivingEntity attacker) {
      attacker.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
   }
}