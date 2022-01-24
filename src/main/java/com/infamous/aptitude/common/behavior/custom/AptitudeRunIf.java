package com.infamous.aptitude.common.behavior.custom;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class AptitudeRunIf<E extends LivingEntity> extends Behavior<E> {
   private final Predicate<E> predicate;
   private final Behavior<?> wrappedBehavior;
   private final boolean checkWhileRunningAlso;

   public AptitudeRunIf(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, Predicate<E> predicate, Behavior<?> wrappedBehavior, boolean checkWhileRunningAlso) {
      super(mergeMaps(entryCondition, wrappedBehavior.entryCondition));
      this.predicate = predicate;
      this.wrappedBehavior = wrappedBehavior;
      this.checkWhileRunningAlso = checkWhileRunningAlso;
   }

   private static Map<MemoryModuleType<?>, MemoryStatus> mergeMaps(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, Map<MemoryModuleType<?>, MemoryStatus> entryCondition1) {
      Map<MemoryModuleType<?>, MemoryStatus> map = Maps.newHashMap();
      map.putAll(entryCondition);
      map.putAll(entryCondition1);
      return map;
   }

   public AptitudeRunIf(Predicate<E> predicate, Behavior<?> wrappedBehavior, boolean checkWhileRunningAlso) {
      this(ImmutableMap.of(), predicate, wrappedBehavior, checkWhileRunningAlso);
   }

   public AptitudeRunIf(Predicate<E> predicate, Behavior<?> wrappedBehavior) {
      this(ImmutableMap.of(), predicate, wrappedBehavior, false);
   }

   public AptitudeRunIf(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, Behavior<?> wrappedBehavior) {
      this(entryCondition, (le) -> {
         return true;
      }, wrappedBehavior, false);
   }

   protected boolean checkExtraStartConditions(ServerLevel serverLevel, E mob) {
      return this.predicate.test(mob) && this.wrappedBehavior.checkExtraStartConditions(serverLevel, mob);
   }

   protected boolean canStillUse(ServerLevel serverLevel, E mob, long gameTime) {
      return this.checkWhileRunningAlso && this.predicate.test(mob) && this.wrappedBehavior.canStillUse(serverLevel, mob, gameTime);
   }

   protected boolean timedOut(long gameTime) {
      return false;
   }

   protected void start(ServerLevel serverLevel, E mob, long gameTime) {
      this.wrappedBehavior.start(serverLevel, mob, gameTime);
   }

   protected void tick(ServerLevel serverLevel, E mob, long gameTime) {
      this.wrappedBehavior.tick(serverLevel, mob, gameTime);
   }

   protected void stop(ServerLevel serverLevel, E mob, long gameTime) {
      this.wrappedBehavior.stop(serverLevel, mob, gameTime);
   }

   public String toString() {
      return "AptitudeRunIf: " + this.wrappedBehavior;
   }
}