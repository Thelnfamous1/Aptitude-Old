package com.infamous.aptitude.server.goal.attack;

import com.infamous.aptitude.common.entity.IRearable;
import com.infamous.aptitude.common.entity.ISwitchCombatTask;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;

public class SwitchableRearingAttackGoal<T extends CreatureEntity & IRearable & ISwitchCombatTask> extends RearingAttackGoal<T> {

    public SwitchableRearingAttackGoal(T horse, double speedModifierIn, boolean mustSee) {
        super(horse, speedModifierIn, mustSee);
    }

    @Override
    public boolean canUse() {
        return !this.rearingCreature.isRanged() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.rearingCreature.isRanged() && super.canContinueToUse();
    }

    @Override
    public void stop() {
        LivingEntity target = this.rearingCreature.getTarget();
        super.stop();
        if(this.rearingCreature.isRanged()){
            this.rearingCreature.setTarget(target);
        }
    }
}
