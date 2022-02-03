package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class AptitudeMeleeAttack extends Behavior<LivingEntity> {
   private final int cooldownBetweenAttacks;
   private final Predicate<LivingEntity> isHoldingUsableProjectileWeapon;
   private final BiPredicate<LivingEntity, LivingEntity> isWithinMeleeAttackRange;

   public AptitudeMeleeAttack(int cooldownBetweenAttacks, Predicate<LivingEntity> isHoldingUsableProjectileWeapon, BiPredicate<LivingEntity, LivingEntity> isWithinMeleeAttackRange) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT));
      this.cooldownBetweenAttacks = cooldownBetweenAttacks;
      this.isHoldingUsableProjectileWeapon = isHoldingUsableProjectileWeapon;
      this.isWithinMeleeAttackRange = isWithinMeleeAttackRange;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity mob) {
      LivingEntity attackTarget = this.getAttackTarget(mob);
      return !this.isHoldingUsableProjectileWeapon.test(mob)
              && BehaviorUtils.canSee(mob, attackTarget)
              && this.isWithinMeleeAttackRange.test(mob, attackTarget);
   }

   protected void start(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      LivingEntity attackTarget = this.getAttackTarget(mob);
      BehaviorUtils.lookAtEntity(mob, attackTarget);
      mob.swing(InteractionHand.MAIN_HAND);
      mob.doHurtTarget(attackTarget);
      mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long)this.cooldownBetweenAttacks);
   }

   private LivingEntity getAttackTarget(LivingEntity mob) {
      return mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
   }
}