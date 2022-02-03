package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class AptitudeCrossbowAttack extends Behavior<LivingEntity> {
   private static final int TIMEOUT = 1200;
   private int attackDelay;
   private AptitudeCrossbowAttack.CrossbowState crossbowState = AptitudeCrossbowAttack.CrossbowState.UNCHARGED;
   private final BiPredicate<LivingEntity, LivingEntity> isWithinProjectileAttackRange;
   private final BiConsumer<LivingEntity, Boolean> setChargingCrossbow;
   private final BiConsumer<LivingEntity, LivingEntity> performRangedAttack;

   public AptitudeCrossbowAttack(BiPredicate<LivingEntity, LivingEntity> isWithinProjectileAttackRange, BiConsumer<LivingEntity, Boolean> setChargingCrossbow, BiConsumer<LivingEntity, LivingEntity> performRangedAttack) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), TIMEOUT);
      this.isWithinProjectileAttackRange = isWithinProjectileAttackRange;
      this.setChargingCrossbow = setChargingCrossbow;
      this.performRangedAttack = performRangedAttack;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity mob) {
      LivingEntity attackTarget = getAttackTarget(mob);
      return mob.isHolding(is -> is.getItem() instanceof CrossbowItem)
              && BehaviorUtils.canSee(mob, attackTarget)
              && this.isWithinProjectileAttackRange.test(mob, attackTarget);
   }

   @Override
   protected boolean canStillUse(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      return mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.checkExtraStartConditions(serverLevel, mob);
   }

   @Override
   protected void tick(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      LivingEntity attackTarget = getAttackTarget(mob);
      this.lookAtTarget(mob, attackTarget);
      this.crossbowAttack(mob, attackTarget);
   }

   @Override
   protected void stop(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      if (mob.isUsingItem()) {
         mob.stopUsingItem();
      }

      if (mob.isHolding(is -> is.getItem() instanceof CrossbowItem)) {
         this.setChargingCrossbow.accept(mob, false);
         CrossbowItem.setCharged(mob.getUseItem(), false);
      }

   }

   private void crossbowAttack(LivingEntity shooter, LivingEntity target) {
      if (this.crossbowState == AptitudeCrossbowAttack.CrossbowState.UNCHARGED) {
         shooter.startUsingItem(ProjectileUtil.getWeaponHoldingHand(shooter, item -> item instanceof CrossbowItem));
         this.crossbowState = AptitudeCrossbowAttack.CrossbowState.CHARGING;
         this.setChargingCrossbow.accept(shooter, true);
      } else if (this.crossbowState == AptitudeCrossbowAttack.CrossbowState.CHARGING) {
         if (!shooter.isUsingItem()) {
            this.crossbowState = AptitudeCrossbowAttack.CrossbowState.UNCHARGED;
         }

         int i = shooter.getTicksUsingItem();
         ItemStack itemstack = shooter.getUseItem();
         if (i >= CrossbowItem.getChargeDuration(itemstack)) {
            shooter.releaseUsingItem();
            this.crossbowState = AptitudeCrossbowAttack.CrossbowState.CHARGED;
            this.attackDelay = 20 + shooter.getRandom().nextInt(20);
            this.setChargingCrossbow.accept(shooter, false);
         }
      } else if (this.crossbowState == AptitudeCrossbowAttack.CrossbowState.CHARGED) {
         --this.attackDelay;
         if (this.attackDelay == 0) {
            this.crossbowState = AptitudeCrossbowAttack.CrossbowState.READY_TO_ATTACK;
         }
      } else if (this.crossbowState == AptitudeCrossbowAttack.CrossbowState.READY_TO_ATTACK) {
         this.performRangedAttack.accept(shooter, target);
         ItemStack itemstack1 = shooter.getItemInHand(ProjectileUtil.getWeaponHoldingHand(shooter, item -> item instanceof CrossbowItem));
         CrossbowItem.setCharged(itemstack1, false);
         this.crossbowState = AptitudeCrossbowAttack.CrossbowState.UNCHARGED;
      }

   }

   private void lookAtTarget(LivingEntity shooter, LivingEntity target) {
      shooter.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
   }

   private static LivingEntity getAttackTarget(LivingEntity shooter) {
      return shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
   }

   static enum CrossbowState {
      UNCHARGED,
      CHARGING,
      CHARGED,
      READY_TO_ATTACK;
   }
}