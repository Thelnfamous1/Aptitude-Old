package com.infamous.aptitude.common.behavior.custom.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.infamous.aptitude.common.behavior.custom.JsonFriendly;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class AptitudeTemptingSensor extends Sensor<PathfinderMob> implements JsonFriendly<AptitudeTemptingSensor> {

   private static final int TEMPTATION_RANGE_DEFAULT = 10;
   private static final TargetingConditions TEMP_TARGETING_DEFAULT = TargetingConditions.forNonCombat().range(TEMPTATION_RANGE_DEFAULT).ignoreLineOfSight();
   private static final Ingredient TEMPTATIONS_DEFAULT = Ingredient.EMPTY;
   private int temptationRange;
   private TargetingConditions temptTargeting;
   private Ingredient temptations;

   public AptitudeTemptingSensor() {
      this.temptationRange = TEMPTATION_RANGE_DEFAULT;
      this.temptTargeting = TEMP_TARGETING_DEFAULT;
      this.temptations = TEMPTATIONS_DEFAULT;
   }

   @Override
   public AptitudeTemptingSensor fromJson(JsonObject jsonObject) {
      this.temptationRange = GsonHelper.getAsInt(jsonObject, "temptationRange", TEMPTATION_RANGE_DEFAULT);
      this.temptTargeting = BehaviorHelper.parseTargetingConditionsOrDefault(jsonObject, "temptTargeting",
              this.temptationRange == TEMPTATION_RANGE_DEFAULT ?
                      TEMP_TARGETING_DEFAULT :
                      TEMP_TARGETING_DEFAULT.copy().range(this.temptationRange)
      );
      this.temptations = Ingredient.fromJson(jsonObject.get("temptations"));
      return this;
   }

   @Override
   protected void doTick(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
      Brain<?> brain = pathfinderMob.getBrain();
      List<Player> detectedPlayers = serverLevel.players()
              .stream()
              .filter(EntitySelector.NO_SPECTATORS)
              .filter((serverPlayer) -> this.temptTargeting.test(pathfinderMob, serverPlayer))
              .filter((serverPlayer) -> pathfinderMob.closerThan(serverPlayer, this.temptationRange))
              .filter(this::playerHoldingTemptation)
              .sorted(Comparator.comparingDouble(pathfinderMob::distanceToSqr))
              .collect(Collectors.toList());
      if (!detectedPlayers.isEmpty()) {
         Player temptingPlayer = detectedPlayers.get(0);
         brain.setMemory(MemoryModuleType.TEMPTING_PLAYER, temptingPlayer);
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