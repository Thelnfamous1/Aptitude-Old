package com.infamous.aptitude.server.goal;

import com.infamous.aptitude.common.entity.ISwitchCombatTask;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.MobEntity;

public class SwitchableRangedAttackGoal<T extends MobEntity & IRangedAttackMob & ISwitchCombatTask> extends AptitudeRangedAttackGoal<T> {

    public SwitchableRangedAttackGoal(T rangedAttackMob, double speedModifierIn, int attackInterval, float attackRadius) {
        super(rangedAttackMob, speedModifierIn, attackInterval, attackRadius);
    }

    @Override
    public boolean canUse() {
        return this.ageableMob.isRanged() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.ageableMob.isRanged() && super.canContinueToUse();
    }

}
