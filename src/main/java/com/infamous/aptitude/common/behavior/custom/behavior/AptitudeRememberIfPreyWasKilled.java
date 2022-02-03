package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.Predicate;

public class AptitudeRememberIfPreyWasKilled<H extends LivingEntity> extends Behavior<H> {
   private final Predicate<LivingEntity> preyPredicate;
   private final UniformInt timeBetweenHunts;

   public AptitudeRememberIfPreyWasKilled(Predicate<LivingEntity> preyPredicate, UniformInt timeBetweenHunts) {
      super(ImmutableMap.of(
              MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.HUNTED_RECENTLY, MemoryStatus.REGISTERED));
      this.preyPredicate = preyPredicate;
      this.timeBetweenHunts = timeBetweenHunts;
   }

   protected void start(ServerLevel p_35133_, H mob, long gameTime) {
      if (this.isAttackTargetDeadHoglin(mob)) {
         this.dontKillAnyMorePreyForAWhile(mob, (long)this.timeBetweenHunts.sample(mob.level.random));
      }
   }

   private void dontKillAnyMorePreyForAWhile(LivingEntity hunter, long huntCooldown) {
      hunter.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, huntCooldown);
   }

   private boolean isAttackTargetDeadHoglin(H hunter) {
      LivingEntity attackTarget = hunter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
      return this.preyPredicate.test(attackTarget) && attackTarget.isDeadOrDying();
   }
}