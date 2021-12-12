package com.infamous.aptitude.server.goal.attack;

import com.infamous.aptitude.common.entity.ISwitchCombatTask;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.Mob;

public class SwitchableRangedAttackGoal<T extends Mob & RangedAttackMob & ISwitchCombatTask> extends AptitudeRangedAttackGoal<T> {

    public SwitchableRangedAttackGoal(T rangedAttackMob, double speedModifierIn, int attackInterval, float attackRadius) {
        super(rangedAttackMob, speedModifierIn, attackInterval, attackRadius);
    }

    @Override
    public SwitchableRangedAttackGoal<T> setBabiesCanAttack(){
        return (SwitchableRangedAttackGoal<T>) super.setBabiesCanAttack();
    }

    @Override
    public boolean canUse() {
        return this.rangedMob.isRanged() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.rangedMob.isRanged() && super.canContinueToUse();
    }

}
