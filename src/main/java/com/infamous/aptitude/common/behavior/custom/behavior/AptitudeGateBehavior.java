package com.infamous.aptitude.common.behavior.custom.behavior;

import com.infamous.aptitude.mixin.BehaviorAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AptitudeGateBehavior<E extends LivingEntity> extends Behavior<E> {
   private final Set<MemoryModuleType<?>> exitErasedMemories;
   private final AptitudeGateBehavior.OrderPolicy orderPolicy;
   private final AptitudeGateBehavior.RunningPolicy runningPolicy;
   private final ShufflingList<Behavior<? super E>> behaviors = new ShufflingList<>();

   public AptitudeGateBehavior(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, Set<MemoryModuleType<?>> exitErasedMemories, AptitudeGateBehavior.OrderPolicy orderPolicy, AptitudeGateBehavior.RunningPolicy runningPolicy, List<Pair<Behavior<?>, Integer>> behaviorsIn) {
      super(entryCondition);
      this.exitErasedMemories = exitErasedMemories;
      this.orderPolicy = orderPolicy;
      this.runningPolicy = runningPolicy;
      behaviorsIn.forEach((entry) -> {
         Behavior<?> behavior = entry.getFirst();
         try{
            this.behaviors.add((Behavior<? super E>) behavior, entry.getSecond());
         } catch (ClassCastException e){
            throw new RuntimeException("Invalid behavior for AptitudeGateBehavior: " + behavior);
         }
      });
   }

   @Override
   protected boolean canStillUse(ServerLevel serverLevel, E mob, long gameTime) {
      return this.behaviors.stream().filter((behavior) -> {
         return behavior.getStatus() == Behavior.Status.RUNNING;
      }).anyMatch((behavior) -> {
         return ((BehaviorAccessor<E>)behavior).callCanStillUse(serverLevel, mob, gameTime);
      });
   }

   @Override
   protected boolean timedOut(long gameTime) {
      return false;
   }

   @Override
   protected void start(ServerLevel serverLevel, E mob, long gameTime) {
      this.orderPolicy.apply(this.behaviors);
      this.runningPolicy.apply(this.behaviors.stream(), serverLevel, mob, gameTime);
   }

   @Override
   protected void tick(ServerLevel serverLevel, E mob, long gameTime) {
      this.behaviors.stream().filter((behavior) -> {
         return behavior.getStatus() == Behavior.Status.RUNNING;
      }).forEach((behavior) -> {
         behavior.tickOrStop(serverLevel, mob, gameTime);
      });
   }

   @Override
   protected void stop(ServerLevel serverLevel, E mob, long gameTime) {
      this.behaviors.stream().filter((behavior) -> {
         return behavior.getStatus() == Behavior.Status.RUNNING;
      }).forEach((behavior) -> {
         behavior.doStop(serverLevel, mob, gameTime);
      });
      this.exitErasedMemories.forEach(mob.getBrain()::eraseMemory);
   }

   @Override
   public String toString() {
      Set<? extends Behavior<?>> set = this.behaviors.stream().filter((behavior) -> {
         return behavior.getStatus() == Behavior.Status.RUNNING;
      }).collect(Collectors.toSet());
      return "(" + this.getClass().getSimpleName() + "): " + set;
   }

   public enum OrderPolicy {
      ORDERED((list) -> {
      }),
      SHUFFLED(ShufflingList::shuffle);

      private final Consumer<ShufflingList<?>> consumer;

      OrderPolicy(Consumer<ShufflingList<?>> listConsumer) {
         this.consumer = listConsumer;
      }

      public void apply(ShufflingList<?> list) {
         this.consumer.accept(list);
      }
   }

   public enum RunningPolicy {
      RUN_ONE {
         public <E extends LivingEntity> void apply(Stream<Behavior<? super E>> behaviorStream, ServerLevel serverLevel, E mob, long gameTime) {
            behaviorStream.filter((behavior) -> {
               return behavior.getStatus() == Behavior.Status.STOPPED;
            }).filter((behavior) -> {
               return behavior.tryStart(serverLevel, mob, gameTime);
            }).findFirst();
         }
      },
      TRY_ALL {
         public <E extends LivingEntity> void apply(Stream<Behavior<? super E>> behaviorStream, ServerLevel serverLevel, E mob, long gameTime) {
            behaviorStream.filter((behavior) -> {
               return behavior.getStatus() == Behavior.Status.STOPPED;
            }).forEach((behavior) -> {
               behavior.tryStart(serverLevel, mob, gameTime);
            });
         }
      };

      public abstract <E extends LivingEntity> void apply(Stream<Behavior<? super E>> behaviorStream, ServerLevel serverLevel, E mob, long gameTime);
   }
}