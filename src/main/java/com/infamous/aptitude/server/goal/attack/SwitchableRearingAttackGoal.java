package com.infamous.aptitude.server.goal.attack;

import com.infamous.aptitude.common.entity.IRearing;
import com.infamous.aptitude.common.entity.ISwitchCombatTask;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;

public class SwitchableRearingAttackGoal<T extends PathfinderMob & IRearing & ISwitchCombatTask> extends RearingAttackGoal<T> {

    public SwitchableRearingAttackGoal(T horse, double speedModifierIn, boolean mustSee) {
        super(horse, speedModifierIn, mustSee);
    }

    @Override
    public SwitchableRearingAttackGoal<T> setBabiesCanAttack(){
        return (SwitchableRearingAttackGoal<T>) super.setBabiesCanAttack();
    }

    @Override
    public boolean canUse() {
        return !this.creature.isRanged() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.creature.isRanged() && super.canContinueToUse();
    }

    @Override
    public void stop() {
        LivingEntity target = this.creature.getTarget();
        super.stop();
        if(this.creature.isRanged()){
            this.creature.setTarget(target);
        }
    }
}
