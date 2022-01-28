package com.infamous.aptitude.common.behavior.custom;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class AptitudeSetWalkTargetAwayFrom<T> extends Behavior<PathfinderMob> {
   private final MemoryModuleType<T> walkAwayFromMemory;
   private final float speedModifier;
   private final int desiredDistance;
   private final Function<T, Vec3> toPosition;

   public AptitudeSetWalkTargetAwayFrom(MemoryModuleType<?> walkAwayFromMemory, float speedModifier, int desiredDistance, boolean p_23990_, Function<?, Vec3> toPosition) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, p_23990_ ? MemoryStatus.REGISTERED : MemoryStatus.VALUE_ABSENT, walkAwayFromMemory, MemoryStatus.VALUE_PRESENT));
      this.walkAwayFromMemory = (MemoryModuleType<T>) walkAwayFromMemory;
      this.speedModifier = speedModifier;
      this.desiredDistance = desiredDistance;
      this.toPosition = (Function<T, Vec3>) toPosition;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
      return !this.alreadyWalkingAwayFromPosWithSameSpeed(pathfinderMob) && pathfinderMob.position().closerThan(this.getPosToAvoid(pathfinderMob), (double) this.desiredDistance);
   }

   private Vec3 getPosToAvoid(PathfinderMob pathfinderMob) {
      return this.toPosition.apply(pathfinderMob.getBrain().getMemory(this.walkAwayFromMemory).get());
   }

   private boolean alreadyWalkingAwayFromPosWithSameSpeed(PathfinderMob pathfinderMob) {
      if (!pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
         return false;
      } else {
         WalkTarget walktarget = pathfinderMob.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get();
         if (walktarget.getSpeedModifier() != this.speedModifier) {
            return false;
         } else {
            Vec3 vec3 = walktarget.getTarget().currentPosition().subtract(pathfinderMob.position());
            Vec3 vec31 = this.getPosToAvoid(pathfinderMob).subtract(pathfinderMob.position());
            return vec3.dot(vec31) < 0.0D;
         }
      }
   }

   @Override
   protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long p_24005_) {
      moveAwayFrom(pathfinderMob, this.getPosToAvoid(pathfinderMob), this.speedModifier);
   }

   private static void moveAwayFrom(PathfinderMob pathfinderMob, Vec3 awayFromPos, float speedModifier) {
      for(int i = 0; i < 10; ++i) {
         Vec3 vec3 = LandRandomPos.getPosAway(pathfinderMob, 16, 7, awayFromPos);
         if (vec3 != null) {
            pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, speedModifier, 0));
            return;
         }
      }

   }
}