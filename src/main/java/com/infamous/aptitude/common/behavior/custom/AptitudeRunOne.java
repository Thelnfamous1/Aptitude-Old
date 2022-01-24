package com.infamous.aptitude.common.behavior.custom;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class AptitudeRunOne<E extends LivingEntity> extends AptitudeGateBehavior<E> {
   public AptitudeRunOne(List<Pair<Behavior<?>, Integer>> behaviorsIn) {
      this(ImmutableMap.of(), behaviorsIn);
   }

   public AptitudeRunOne(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, List<Pair<Behavior<?>, Integer>> behaviorsIn) {
      super(entryCondition, ImmutableSet.of(), AptitudeGateBehavior.OrderPolicy.SHUFFLED, AptitudeGateBehavior.RunningPolicy.RUN_ONE, behaviorsIn);
   }
}