package com.infamous.aptitude.common.behavior.custom.sensor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.infamous.aptitude.common.behavior.custom.memory.AptitudeMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ZombieSpecificSensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(
              MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
              MemoryModuleType.NEAREST_LIVING_ENTITIES,
              MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
              MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD,
              MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
              MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN,
              MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
              AptitudeMemoryModuleTypes.NEAREST_VISIBLE_ADULT_ZOMBIES.get(),
              AptitudeMemoryModuleTypes.NEARBY_ADULT_ZOMBIES.get(),
              AptitudeMemoryModuleTypes.VISIBLE_ADULT_ZOMBIE_COUNT.get(),
              MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
              MemoryModuleType.NEAREST_REPELLENT);
   }

   protected void doTick(ServerLevel p_26726_, LivingEntity p_26727_) {
      Brain<?> brain = p_26727_.getBrain();
      brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, findNearestRepellent(p_26726_, p_26727_));
      Optional<Mob> optional = Optional.empty();
      Optional<Hoglin> optional1 = Optional.empty();
      Optional<Hoglin> optional2 = Optional.empty();
      Optional<Zombie> optional3 = Optional.empty();
      Optional<LivingEntity> optional4 = Optional.empty();
      Optional<Player> optional5 = Optional.empty();
      Optional<Player> optional6 = Optional.empty();
      int i = 0;
      List<Zombie> list = Lists.newArrayList();
      List<Zombie> list1 = Lists.newArrayList();
      NearestVisibleLivingEntities nearestvisiblelivingentities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

      for(LivingEntity livingentity : nearestvisiblelivingentities.findAll((p_186157_) -> {
         return true;
      })) {
         if (livingentity instanceof Hoglin) {
            Hoglin hoglin = (Hoglin)livingentity;
            if (hoglin.isBaby() && optional2.isEmpty()) {
               optional2 = Optional.of(hoglin);
            } else if (hoglin.isAdult()) {
               ++i;
               if (optional1.isEmpty() && hoglin.canBeHunted()) {
                  optional1 = Optional.of(hoglin);
               }
            }
         } else if (livingentity instanceof Husk) {
            Husk piglinbrute = (Husk) livingentity;
            list.add(piglinbrute);
         } else if (livingentity instanceof Zombie) {
            Zombie piglin = (Zombie)livingentity;
            if (piglin.isBaby() && optional3.isEmpty()) {
               optional3 = Optional.of(piglin);
            } else if (!piglin.isBaby()) {
               list.add(piglin);
            }
         } else if (livingentity instanceof Player) {
            Player player = (Player)livingentity;
            if (optional5.isEmpty() && !PiglinAi.isWearingGold(player) && p_26727_.canAttack(livingentity)) {
               optional5 = Optional.of(player);
            }

            if (optional6.isEmpty() && !player.isSpectator() && PiglinAi.isPlayerHoldingLovedItem(player)) {
               optional6 = Optional.of(player);
            }
         } else if (!optional.isEmpty() || !(livingentity instanceof WitherSkeleton) && !(livingentity instanceof WitherBoss)) {
            if (optional4.isEmpty() && PiglinAi.isZombified(livingentity.getType())) {
               optional4 = Optional.of(livingentity);
            }
         } else {
            optional = Optional.of((Mob)livingentity);
         }
      }

      for(LivingEntity livingentity1 : brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(ImmutableList.of())) {
         if (livingentity1 instanceof Zombie) {
            Zombie abstractpiglin = (Zombie)livingentity1;
            if (!abstractpiglin.isBaby()) {
               list1.add(abstractpiglin);
            }
         }
      }

      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, optional1);
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, optional2);
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, optional4);
      brain.setMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, optional5);
      brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, optional6);
      brain.setMemory(AptitudeMemoryModuleTypes.NEARBY_ADULT_ZOMBIES.get(), list1);
      brain.setMemory(AptitudeMemoryModuleTypes.NEAREST_VISIBLE_ADULT_ZOMBIES.get(), list);
      brain.setMemory(AptitudeMemoryModuleTypes.VISIBLE_ADULT_ZOMBIE_COUNT.get(), list.size());
      brain.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, i);
   }

   private static Optional<BlockPos> findNearestRepellent(ServerLevel p_26735_, LivingEntity p_26736_) {
      return BlockPos.findClosestMatch(p_26736_.blockPosition(), 8, 4, (p_186160_) -> {
         return isValidRepellent(p_26735_, p_186160_);
      });
   }

   private static boolean isValidRepellent(ServerLevel p_26729_, BlockPos p_26730_) {
      BlockState blockstate = p_26729_.getBlockState(p_26730_);
      boolean flag = blockstate.is(BlockTags.PIGLIN_REPELLENTS);
      return flag && blockstate.is(Blocks.SOUL_CAMPFIRE) ? CampfireBlock.isLitCampfire(blockstate) : flag;
   }
}