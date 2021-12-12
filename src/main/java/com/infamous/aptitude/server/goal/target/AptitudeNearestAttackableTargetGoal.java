package com.infamous.aptitude.server.goal.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class AptitudeNearestAttackableTargetGoal<T extends LivingEntity, A extends Mob> extends NearestAttackableTargetGoal<T> {
    protected final A attacker;
    private boolean babiesCanAttack;

    public AptitudeNearestAttackableTargetGoal(A attackerIn, Class<T> targetType, int randomIntervalIn, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(attackerIn, targetType, randomIntervalIn, mustSee, mustReach, targetPredicate);
        this.attacker = attackerIn;
    }

    public AptitudeNearestAttackableTargetGoal<T, A> setBabiesCanAttack(){
        this.babiesCanAttack = true;
        return this;
    }

    @Override
    public boolean canUse() {
        return (!this.attacker.isBaby() || this.babiesCanAttack) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return (!this.attacker.isBaby() || this.babiesCanAttack) && super.canContinueToUse();
    }
}
