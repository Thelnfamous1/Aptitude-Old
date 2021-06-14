package com.infamous.aptitude.server.goal;

import com.infamous.aptitude.common.entity.ISwitchCombatTask;
import net.minecraft.entity.CreatureEntity;

public class SwitchableAttackGoal<T extends CreatureEntity & ISwitchCombatTask> extends AptitudeAttackGoal {
    protected T switchableMob;

    public SwitchableAttackGoal(T creature, double speedModifierIn, boolean mustSee) {
        super(creature, speedModifierIn, mustSee);
        this.switchableMob = creature;
    }

    @Override
    public boolean canUse() {
        return !this.switchableMob.isRanged() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.switchableMob.isRanged() && super.canContinueToUse();
    }
}
