package com.infamous.aptitude.server.goal.attack;

import com.infamous.aptitude.common.entity.ISwitchCombatTask;
import net.minecraft.entity.CreatureEntity;

public class SwitchableAttackGoal<T extends CreatureEntity & ISwitchCombatTask> extends AptitudeAttackGoal<T> {

    public SwitchableAttackGoal(T creature, double speedModifierIn, boolean mustSee) {
        super(creature, speedModifierIn, mustSee);
    }

    @Override
    public SwitchableAttackGoal<T> setBabiesCanAttack(){
        return (SwitchableAttackGoal<T>) super.setBabiesCanAttack();
    }

    @Override
    public boolean canUse() {
        return !this.creature.isRanged() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.creature.isRanged() && super.canContinueToUse();
    }
}
