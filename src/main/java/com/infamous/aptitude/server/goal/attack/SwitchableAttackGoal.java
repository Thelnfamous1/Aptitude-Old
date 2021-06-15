package com.infamous.aptitude.server.goal.attack;

import com.infamous.aptitude.common.entity.ISwitchCombatTask;
import net.minecraft.entity.CreatureEntity;

public class SwitchableAttackGoal<T extends CreatureEntity & ISwitchCombatTask> extends AptitudeAttackGoal {
    protected T switchableCreature;

    public SwitchableAttackGoal(T creature, double speedModifierIn, boolean mustSee) {
        super(creature, speedModifierIn, mustSee);
        this.switchableCreature = creature;
    }

    @Override
    public boolean canUse() {
        return !this.switchableCreature.isRanged() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.switchableCreature.isRanged() && super.canContinueToUse();
    }
}
