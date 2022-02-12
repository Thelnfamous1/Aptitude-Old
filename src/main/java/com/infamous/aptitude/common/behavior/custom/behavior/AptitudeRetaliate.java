package com.infamous.aptitude.common.behavior.custom.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.aptitude.common.util.ReflectionHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

import java.util.Optional;
import java.util.function.BiPredicate;

public class AptitudeRetaliate extends Behavior<LivingEntity> {
    private final BiPredicate<LivingEntity, LivingEntity> ignoreIf;

    public AptitudeRetaliate(BiPredicate<LivingEntity, LivingEntity> ignoreIf) {
        super(ImmutableMap.of(
                MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryStatus.REGISTERED,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED,
                MemoryModuleType.ANGRY_AT, MemoryStatus.REGISTERED,
                MemoryModuleType.UNIVERSAL_ANGER, MemoryStatus.REGISTERED));
        this.ignoreIf = ignoreIf;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity mob) {
        LivingEntity hurtByEntity = this.getHurtByEntity(mob);

        if (Sensor.isEntityAttackableIgnoringLineOfSight(mob, hurtByEntity) && !BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(mob, hurtByEntity, 4.0D)) {
            return !this.ignoreIf.test(mob, hurtByEntity);
        }

        return false;

    }

    @Override
    protected void start(ServerLevel serverLevel, LivingEntity mob, long gameTime) {
        LivingEntity hurtByEntity = this.getHurtByEntity(mob);
        if (hurtByEntity.getType() == EntityType.PLAYER && mob.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            this.setAngerTargetToNearestTargetablePlayerIfFound(mob, hurtByEntity);
        } else {
            this.setAngerTarget(mob, hurtByEntity);
        }
    }

    protected void setAngerTargetToNearestTargetablePlayerIfFound(LivingEntity angry, LivingEntity target) {
        Optional<Player> nearestVisibleTargetablePlayer = this.getNearestVisibleTargetablePlayer(angry);
        if (nearestVisibleTargetablePlayer.isPresent()) {
            this.setAngerTarget(angry, nearestVisibleTargetablePlayer.get());
        } else {
            this.setAngerTarget(angry, target);
        }

    }

    protected Optional<Player> getNearestVisibleTargetablePlayer(LivingEntity angry) {
        return angry.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) ? angry.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) : Optional.empty();
    }

    protected void setAngerTarget(LivingEntity angry, LivingEntity target) {
        if (Sensor.isEntityAttackableIgnoringLineOfSight(angry, target)) {
            angry.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            angry.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, target.getUUID(), 600L);

            // TODO:
            if (target.getType() == EntityType.HOGLIN && angry instanceof AbstractPiglin piglin && ReflectionHelper.reflectPiglinCanHunt(piglin)) {
                setHuntedRecently(angry);
            }

            if (target.getType() == EntityType.PLAYER && angry.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                angry.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
            }

        }
    }

    protected Optional<LivingEntity> getAngerTarget(LivingEntity angry) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(angry, MemoryModuleType.ANGRY_AT);
    }

    protected LivingEntity getHurtByEntity(LivingEntity mob) {
        return mob.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY).get();
    }

    //TODO:
    protected static void setHuntedRecently(LivingEntity livingEntity) {
        livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long) TimeUtil.rangeOfSeconds(30, 120).sample(livingEntity.level.random));
    }
}
