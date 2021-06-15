package com.infamous.aptitude.server.goal.misc;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.PanicGoal;

public class AptitudePanicGoal extends PanicGoal {
    public AptitudePanicGoal(CreatureEntity creatureIn, double speedModifierIn) {
        super(creatureIn, speedModifierIn);
    }

    public boolean canUse() {
        return (this.mob.isBaby() || this.mob.isOnFire()) && super.canUse();
    }

}
