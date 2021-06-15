package com.infamous.aptitude.server.goal.target;

import com.infamous.aptitude.common.entity.ICanSpit;
import com.infamous.aptitude.common.entity.ISwitchCombatTask;
import net.minecraft.entity.CreatureEntity;

public class CanSpitHurtByTargetGoal<T extends CreatureEntity & ISwitchCombatTask & ICanSpit> extends AptitudeHurtByTargetGoal {
    protected T llama;

    public CanSpitHurtByTargetGoal(T llama, Class<?>... p_i50317_2_) {
        super(llama, p_i50317_2_);
        this.llama = llama;
    }

    public boolean canContinueToUse() {
        if (this.llama.getDidSpit()) {
            this.llama.setLlamaDidSpit(false);
        }

        return super.canContinueToUse();
    }
}
