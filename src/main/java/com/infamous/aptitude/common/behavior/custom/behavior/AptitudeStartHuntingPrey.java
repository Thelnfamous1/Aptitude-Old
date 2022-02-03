package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.level.GameRules;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class AptitudeStartHuntingPrey<H extends LivingEntity, P extends LivingEntity, A extends LivingEntity> extends Behavior<H> {
   private final MemoryModuleType<P> preyMemory;
   private final MemoryModuleType<List<A>> visibleAlliesMemory;
   private final MemoryModuleType<List<A>> alliesMemory;
   private final Predicate<LivingEntity> canHunt;
   private final Predicate<LivingEntity> canBeHunted;
   private final UniformInt timeBetweenHunts;
   private final long angerExpiry;

   public AptitudeStartHuntingPrey(MemoryModuleType<P> preyMemory, MemoryModuleType<List<A>> visibleAlliesMemory, MemoryModuleType<List<A>> alliesMemory, Predicate<LivingEntity> canHunt, Predicate<LivingEntity> canBeHunted, UniformInt timeBetweenHunts, long angerExpiry) {
      super(ImmutableMap.of(
              preyMemory, MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.ANGRY_AT, MemoryStatus.VALUE_ABSENT,
              MemoryModuleType.HUNTED_RECENTLY, MemoryStatus.VALUE_ABSENT,
              visibleAlliesMemory, MemoryStatus.REGISTERED,
              alliesMemory, MemoryStatus.REGISTERED));
      this.preyMemory = preyMemory;
      this.visibleAlliesMemory = visibleAlliesMemory;
      this.alliesMemory = alliesMemory;
      this.canHunt = canHunt;
      this.canBeHunted = canBeHunted;
      this.timeBetweenHunts = timeBetweenHunts;
      this.angerExpiry = angerExpiry;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel serverLevel, H mob) {
      return !mob.isBaby() && !hasAnyoneNearbyHuntedRecently(mob);
   }

   @Override
   protected void start(ServerLevel serverLevel, H mob, long gameTime) {
      P prey = mob.getBrain().getMemory(this.preyMemory).get();
      this.setAngerTarget(mob, prey);
      dontKillAnyMorePreyForAWhile(mob, (long)this.timeBetweenHunts.sample(mob.level.random));
      this.broadcastAngerTarget(mob, prey);
      this.broadcastDontKillAnyMorePreyForAWhile(mob);
   }

   private boolean hasAnyoneNearbyHuntedRecently(H hunter) {
      return hunter.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY)
              || this.getVisibleAllies(hunter)
                  .stream()
                  .anyMatch((visibleAlly) -> visibleAlly.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY));
   }

   private List<A> getVisibleAllies(H hunter) {
      return hunter.getBrain().getMemory(this.visibleAlliesMemory).orElse(ImmutableList.of());
   }

   private void setAngerTarget(LivingEntity hunter, LivingEntity target) {
      if (Sensor.isEntityAttackableIgnoringLineOfSight(hunter, target)) {
         hunter.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
         hunter.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, target.getUUID(), this.angerExpiry);
         if (this.canHunt.test(hunter)) {
            dontKillAnyMorePreyForAWhile(hunter, (long)this.timeBetweenHunts.sample(hunter.level.random));
         }

         if (target.getType() == EntityType.PLAYER && hunter.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            hunter.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, this.angerExpiry);
         }

      }
   }

   private static void dontKillAnyMorePreyForAWhile(LivingEntity hunter, long huntCooldown) {
      hunter.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, huntCooldown);
   }

   private void broadcastAngerTarget(H hunter, P target) {
      this.getAllies(hunter).forEach((ally) -> {
         if (this.canHunt.test(ally) && this.canBeHunted.test(target)) {
            this.setAngerTargetIfCloserThanCurrent(ally, target);
         }
      });
   }

   private List<? extends LivingEntity> getAllies(H hunter) {
      return hunter.getBrain().getMemory(this.alliesMemory).orElse(ImmutableList.of());
   }

   private void setAngerTargetIfCloserThanCurrent(LivingEntity hunter, P target) {
      Optional<LivingEntity> optional = this.getAngerTarget(hunter);
      LivingEntity livingentity = BehaviorUtils.getNearestTarget(hunter, optional, target);
      if (optional.isEmpty() || optional.get() != livingentity) {
         this.setAngerTarget(hunter, livingentity);
      }
   }

   private Optional<LivingEntity> getAngerTarget(LivingEntity hunter) {
      return BehaviorUtils.getLivingEntityFromUUIDMemory(hunter, MemoryModuleType.ANGRY_AT);
   }

   private void broadcastDontKillAnyMorePreyForAWhile(H hunter) {
      this.getVisibleAllies(hunter).forEach(visibleAlly -> dontKillAnyMorePreyForAWhile(visibleAlly, (long)this.timeBetweenHunts.sample(hunter.level.random)));
   }
}