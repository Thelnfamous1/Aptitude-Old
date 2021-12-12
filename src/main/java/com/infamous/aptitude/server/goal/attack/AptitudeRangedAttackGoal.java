package com.infamous.aptitude.server.goal.attack;

import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;

public class AptitudeRangedAttackGoal<T extends Mob & RangedAttackMob> extends RangedAttackGoal {
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
