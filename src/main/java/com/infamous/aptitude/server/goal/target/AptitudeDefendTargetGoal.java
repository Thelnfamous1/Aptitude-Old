package com.infamous.aptitude.server.goal.target;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class AptitudeDefendTargetGoal<T extends LivingEntity, A extends MobEntity> extends AptitudeNearestAttackableTargetGoal<T, A>{
    private double followDistanceFactor = 1.0D;

    public AptitudeDefendTargetGoal(A attackerIn, Class<T> targetType, int randomIntervalIn, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(attackerIn, targetType, randomIntervalIn, mustSee, mustReach, targetPredicate);
    }

    public AptitudeDefendTargetGoal<T, A> setFollowDistanceFactor(double followDistanceFactor) {
        this.followDistanceFactor = followDistanceFactor;
        return this;
    }

    @Override
    public AptitudeDefendTargetGoal<T, A> setBabiesCanAttack() {
        return (AptitudeDefendTargetGoal<T, A>) super.setBabiesCanAttack();
    }

    @Override
    protected double getFollowDistance() {
        return super.getFollowDistance() * this.followDistanceFactor;
    }
}
