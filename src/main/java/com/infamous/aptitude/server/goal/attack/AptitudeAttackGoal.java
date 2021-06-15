package com.infamous.aptitude.server.goal.attack;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

public class AptitudeAttackGoal extends MeleeAttackGoal {
    public AptitudeAttackGoal(CreatureEntity creature, double speedModifierIn, boolean mustSee) {
        super(creature, speedModifierIn, mustSee);
    }

    @Override
    public boolean canUse() {
        return !this.mob.isBaby() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.isBaby() && super.canContinueToUse();
    }
}
