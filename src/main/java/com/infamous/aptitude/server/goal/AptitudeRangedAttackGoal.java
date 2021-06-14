package com.infamous.aptitude.server.goal;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.RangedAttackGoal;

public class AptitudeRangedAttackGoal<T extends MobEntity & IRangedAttackMob> extends RangedAttackGoal {
    protected final T ageableMob;

    public AptitudeRangedAttackGoal(T rangedAttackMob, double speedModifierIn, int attackInterval, float attackRadius) {
        super(rangedAttackMob, speedModifierIn, attackInterval, attackRadius);
        this.ageableMob = rangedAttackMob;
    }

    @Override
    public boolean canUse() {
        return !this.ageableMob.isBaby() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.ageableMob.isBaby() && super.canContinueToUse();
    }
}
