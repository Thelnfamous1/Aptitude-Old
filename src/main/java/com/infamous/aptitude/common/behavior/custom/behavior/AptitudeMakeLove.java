package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.aptitude.common.behavior.custom.memory.AptitudeMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class AptitudeMakeLove extends Behavior<LivingEntity> {
   private static final int BREED_RANGE = 3;
   private static final int MIN_DURATION = 60;
   private static final int MAX_DURATION = 110;
   private final EntityType<? extends LivingEntity> partnerType;
   private final float speedModifier;
   private final Predicate<LivingEntity> isInLove;
   private final BiPredicate<LivingEntity, LivingEntity> canMate;
   private final BiConsumer<LivingEntity, LivingEntity> spawnChildFromBreeding;
   private long spawnChildAtTime;

   public AptitudeMakeLove(EntityType<? extends LivingEntity> partnerType, Predicate<LivingEntity> isInLove, BiPredicate<LivingEntity, LivingEntity> canMate, BiConsumer<LivingEntity, LivingEntity> spawnChildFromBreeding, float speedModifier) {
      super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT, AptitudeMemoryModuleTypes.BREED_TARGET.get(), MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), MAX_DURATION);
      this.partnerType = partnerType;
      this.isInLove = isInLove;
      this.canMate = canMate;
      this.spawnChildFromBreeding = spawnChildFromBreeding;
      this.speedModifier = speedModifier;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity mob) {
      return this.isInLove.test(mob) && this.findValidBreedPartner(mob).isPresent();
   }

   @Override
   protected void start(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      LivingEntity partner = this.findValidBreedPartner(mob).get();
      mob.getBrain().setMemory(AptitudeMemoryModuleTypes.BREED_TARGET.get(), partner);
      partner.getBrain().setMemory(AptitudeMemoryModuleTypes.BREED_TARGET.get(), mob);
      BehaviorUtils.lockGazeAndWalkToEachOther(mob, partner, this.speedModifier);
      int spawnChildDuration = MIN_DURATION + mob.getRandom().nextInt(50);
      this.spawnChildAtTime = gameTime + (long)spawnChildDuration;
   }

   @Override
   protected boolean canStillUse(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      if (!this.hasBreedTargetOfRightType(mob)) {
         return false;
      } else {
         LivingEntity breedTarget = this.getBreedTarget(mob);
         return breedTarget.isAlive() && this.canMate.test(mob, breedTarget) && BehaviorUtils.entityIsVisible(mob.getBrain(), breedTarget) && gameTime <= this.spawnChildAtTime;
      }
   }

   @Override
   protected void tick(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      LivingEntity breedTarget = this.getBreedTarget(mob);
      BehaviorUtils.lockGazeAndWalkToEachOther(mob, breedTarget, this.speedModifier);
      if (mob.closerThan(breedTarget, (double)BREED_RANGE)) {
         if (gameTime >= this.spawnChildAtTime) {
            this.spawnChildFromBreeding.accept(mob, breedTarget);
            mob.getBrain().eraseMemory(AptitudeMemoryModuleTypes.BREED_TARGET.get());
            breedTarget.getBrain().eraseMemory(AptitudeMemoryModuleTypes.BREED_TARGET.get());
         }

      }
   }

   @Override
   protected void stop(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
      mob.getBrain().eraseMemory(AptitudeMemoryModuleTypes.BREED_TARGET.get());
      mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      this.spawnChildAtTime = 0L;
   }

   private LivingEntity getBreedTarget(LivingEntity mater) {
      return mater.getBrain().getMemory(AptitudeMemoryModuleTypes.BREED_TARGET.get()).get();
   }

   private boolean hasBreedTargetOfRightType(LivingEntity mater) {
      Brain<?> brain = mater.getBrain();
      return brain.hasMemoryValue(AptitudeMemoryModuleTypes.BREED_TARGET.get()) && brain.getMemory(AptitudeMemoryModuleTypes.BREED_TARGET.get()).get().getType() == this.partnerType;
   }

   private Optional<? extends LivingEntity> findValidBreedPartner(LivingEntity mater) {
      return mater.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findClosest((le) -> {
         if (le.getType() == this.partnerType) {
            return this.canMate.test(mater, le);
         }

         return false;
      });
   }
}