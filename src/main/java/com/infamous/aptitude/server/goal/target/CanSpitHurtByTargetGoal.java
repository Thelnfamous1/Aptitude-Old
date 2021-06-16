package com.infamous.aptitude.server.goal.target;

import com.infamous.aptitude.common.entity.ICanSpit;
import com.infamous.aptitude.common.entity.ISwitchCombatTask;
import net.minecraft.entity.CreatureEntity;

public class CanSpitHurtByTargetGoal<T extends CreatureEntity & ISwitchCombatTask & ICanSpit> extends AptitudeHurtByTargetGoal<T> {
    public CanSpitHurtByTargetGoal(T llama, Class<?>... p_i50317_2_) {
        super(llama, p_i50317_2_);
    }

    @Override
    public CanSpitHurtByTargetGoal<T> setBabiesCanAttack() {
        return (CanSpitHurtByTargetGoal<T>) super.setBabiesCanAttack();
    }

    public boolean canContinueToUse() {
        if (this.creatureAsGeneric.getDidSpit()) {
            this.creatureAsGeneric.setLlamaDidSpit(false);
        }

        return super.canContinueToUse();
    }
}
