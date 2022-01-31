package com.infamous.aptitude.common.behavior.custom.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class AptitudeTemptingSensor extends Sensor<PathfinderMob> {
   public static final int TEMPTATION_RANGE = 10;
   private static final TargetingConditions TEMPT_TARGETING = TargetingConditions.forNonCombat().range(TEMPTATION_RANGE).ignoreLineOfSight();
   private final Ingredient temptations;

   public AptitudeTemptingSensor(Ingredient ingredient) {
      this.temptations = ingredient;
   }

   @Override
   protected void doTick(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
      Brain<?> brain = pathfinderMob.getBrain();
      List<Player> list = serverLevel.players()
              .stream()
              .filter(EntitySelector.NO_SPECTATORS)
              .filter((p_148342_) -> TEMPT_TARGETING.test(pathfinderMob, p_148342_))
              .filter((p_148335_) -> pathfinderMob.closerThan(p_148335_, TEMPTATION_RANGE))
              .filter(this::playerHoldingTemptation)
              .sorted(Comparator.comparingDouble(pathfinderMob::distanceToSqr))
              .collect(Collectors.toList());
      if (!list.isEmpty()) {
         Player player = list.get(0);
         brain.setMemory(MemoryModuleType.TEMPTING_PLAYER, player);
      } else {
         brain.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
      }

   }

   private boolean playerHoldingTemptation(Player player) {
      return this.isTemptation(player.getMainHandItem()) || this.isTemptation(player.getOffhandItem());
   }

   private boolean isTemptation(ItemStack stack) {
      return this.temptations.test(stack);
   }

   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
   }
}