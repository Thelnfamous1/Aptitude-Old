package com.infamous.aptitude.server.goal.attack;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.RangedAttackGoal;

public class AptitudeRangedAttackGoal<T extends MobEntity & IRangedAttackMob> extends RangedAttackGoal {
    protected final T rangedMob;
    private boolean babiesCanAttack;

    public AptitudeRangedAttackGoal(T rangedAttackMob, double speedModifierIn, int attackInterval, float attackRadius) {
        super(rangedAttackMob, speedModifierIn, attackInterval, attackRadius);
        this.rangedMob = rangedAttackMob;
    }

    public AptitudeRangedAttackGoal<T> setBabiesCanAttack(){
        this.babiesCanAttack = true;
        return this;
    }

    @Override
    public boolean canUse() {
        return (!this.rangedMob.isBaby() || this.babiesCanAttack) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return (!this.rangedMob.isBaby() || this.babiesCanAttack) && super.canContinueToUse();
    }
}
