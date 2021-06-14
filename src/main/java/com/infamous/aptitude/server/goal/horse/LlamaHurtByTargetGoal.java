package com.infamous.aptitude.server.goal.horse;

import com.infamous.aptitude.common.entity.IAptitudeLlama;
import com.infamous.aptitude.common.entity.ISwitchCombatTask;
import com.infamous.aptitude.server.goal.AptitudeHurtByTargetGoal;
import net.minecraft.entity.CreatureEntity;

public class LlamaHurtByTargetGoal<T extends CreatureEntity & ISwitchCombatTask & IAptitudeLlama> extends AptitudeHurtByTargetGoal {
    protected T llama;

    public LlamaHurtByTargetGoal(T llama, Class<?>... p_i50317_2_) {
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
