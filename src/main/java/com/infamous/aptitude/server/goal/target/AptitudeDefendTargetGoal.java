package com.infamous.aptitude.server.goal.target;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class AptitudeDefendTargetGoal<T extends LivingEntity, A extends MobEntity> extends AptitudeNearestAttackableTargetGoal<T, A>{
    public AptitudeDefendTargetGoal(A attackerIn, Class<T> targetType, int randomIntervalIn, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(attackerIn, targetType, randomIntervalIn, mustSee, mustReach, targetPredicate);
    }

    @Override
    protected double getFollowDistance() {
        return super.getFollowDistance() * 0.25D;
    }
}
