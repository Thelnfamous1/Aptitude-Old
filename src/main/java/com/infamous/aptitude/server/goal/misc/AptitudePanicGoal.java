package com.infamous.aptitude.server.goal.misc;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;

public class AptitudePanicGoal extends PanicGoal {
    public AptitudePanicGoal(PathfinderMob creatureIn, double speedModifierIn) {
        super(creatureIn, speedModifierIn);
    }

    public boolean canUse() {
        return (this.mob.isBaby() || this.mob.isOnFire()) && super.canUse();
    }

}
