package com.infamous.aptitude.common.behavior.custom.sensor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.infamous.aptitude.common.behavior.custom.memory.AptitudeMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.piglin.Piglin;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PigSpecificSensor extends Sensor<Pig> {
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, AptitudeMemoryModuleTypes.NEAREST_VISIBLE_ADULT_PIGS.get(), MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, AptitudeMemoryModuleTypes.VISIBLE_ADULT_PIG_COUNT.get());
    }

    @Override
    protected void doTick(ServerLevel serverLevel, Pig sensorPig) {
        Brain<?> brain = sensorPig.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, this.findNearestRepellent(serverLevel, sensorPig));
        Optional<Piglin> optional = Optional.empty();
        int i = 0;
        List<Pig> list = Lists.newArrayList();
        NearestVisibleLivingEntities nearestvisiblelivingentities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for(LivingEntity livingentity : nearestvisiblelivingentities.findAll((livingEntity) -> {
            return !livingEntity.isBaby() && (livingEntity instanceof Piglin || livingEntity instanceof Pig);
        })) {
            if (livingentity instanceof Piglin piglin) {
                ++i;
                if (optional.isEmpty()) {
                    optional = Optional.of(piglin);
                }
            }

            if (livingentity instanceof Pig pig) {
                list.add(pig);
            }
        }

        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, optional);
        brain.setMemory(AptitudeMemoryModuleTypes.NEAREST_VISIBLE_ADULT_PIGS.get(), list);
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, i);
        brain.setMemory(AptitudeMemoryModuleTypes.VISIBLE_ADULT_PIG_COUNT.get(), list.size());
    }

    private Optional<BlockPos> findNearestRepellent(ServerLevel serverLevel, Pig pig) {
        return BlockPos.findClosestMatch(pig.blockPosition(), 8, 4, (p_186148_) -> {
            return serverLevel.getBlockState(p_186148_).is(BlockTags.HOGLIN_REPELLENTS);
        });
    }
}
