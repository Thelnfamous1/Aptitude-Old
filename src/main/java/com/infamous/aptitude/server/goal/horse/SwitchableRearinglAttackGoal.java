package com.infamous.aptitude.server.goal.horse;

import com.infamous.aptitude.common.entity.IRearable;
import com.infamous.aptitude.common.entity.ISwitchCombatTask;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;

public class SwitchableRearinglAttackGoal<T extends CreatureEntity & IRearable & ISwitchCombatTask> extends RearingAttackGoal<T> {

    public SwitchableRearinglAttackGoal(T horse, double speedModifierIn, boolean mustSee) {
        super(horse, speedModifierIn, mustSee);
    }

    @Override
    public boolean canUse() {
        return !this.horse.isRanged() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.horse.isRanged() && super.canContinueToUse();
    }

    @Override
    public void stop() {
        LivingEntity target = this.horse.getTarget();
        super.stop();
        if(this.horse.isRanged()){
            this.horse.setTarget(target);
        }
    }
}
