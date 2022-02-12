package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.GameRules;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

public class AptitudeRetaliateAndBroadcast extends AptitudeRetaliate{
    private final BiPredicate<LivingEntity, LivingEntity> allyIgnoreIf;
    private final MemoryModuleType<List<LivingEntity>> nearbyAlliesMemory;

    public AptitudeRetaliateAndBroadcast(BiPredicate<LivingEntity, LivingEntity> ignoreIf, BiPredicate<LivingEntity, LivingEntity> allyIgnoreIf, MemoryModuleType<List<LivingEntity>> nearbyAlliesMemory) {
        super(ignoreIf);
        this.allyIgnoreIf = allyIgnoreIf;
        this.nearbyAlliesMemory = nearbyAlliesMemory;
    }

    @Override
    protected void start(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
        LivingEntity hurtByEntity = this.getHurtByEntity(mob);
        if (hurtByEntity.getType() == EntityType.PLAYER && mob.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            this.setAngerTargetToNearestTargetablePlayerIfFound(mob, hurtByEntity);
            this.broadcastUniversalAnger(mob);
        } else {
            this.setAngerTarget(mob, hurtByEntity);
            this.broadcastAngerTarget(mob, hurtByEntity);
        }
    }


    private void broadcastUniversalAnger(LivingEntity angry) {
        this.getNearbyAllies(angry).forEach((ally) -> {
            this.getNearestVisibleTargetablePlayer(ally).ifPresent((player) -> {
                this.setAngerTarget(ally, player);
            });
        });
    }

    private void broadcastAngerTarget(LivingEntity angry, LivingEntity target) {
        this.getNearbyAllies(angry).forEach((ally) -> {
            if(!this.allyIgnoreIf.test(ally, target)){
                this.setAngerTargetIfCloserThanCurrent(ally, target);
            }
        });
    }

    private void setAngerTargetIfCloserThanCurrent(LivingEntity angry, LivingEntity target) {
        Optional<LivingEntity> angerTarget = this.getAngerTarget(angry);
        LivingEntity nearestTarget = BehaviorUtils.getNearestTarget(angry, angerTarget, target);
        if (angerTarget.isEmpty() || angerTarget.get() != nearestTarget) {
            this.setAngerTarget(angry, nearestTarget);
        }
    }

    private List<LivingEntity> getNearbyAllies(LivingEntity angry) {
        return angry.getBrain().hasMemoryValue(this.nearbyAlliesMemory) ? angry.getBrain().getMemory(this.nearbyAlliesMemory).orElse(ImmutableList.of()) : ImmutableList.of();
    }
}
