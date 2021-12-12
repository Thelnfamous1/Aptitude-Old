package com.infamous.aptitude.server.goal.attack;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class AptitudeAttackGoal<T extends PathfinderMob> extends MeleeAttackGoal {
    private boolean babiesCanAttack;
    protected T creature;

    public AptitudeAttackGoal(T creature, double speedModifierIn, boolean mustSee) {
        super(creature, speedModifierIn, mustSee);
        this.creature = creature;
    }

    public AptitudeAttackGoal<T> setBabiesCanAttack(){
        this.babiesCanAttack = true;
        return this;
    }

    @Override
    public boolean canUse() {
        return (!this.mob.isBaby() || this.babiesCanAttack) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return (!this.mob.isBaby() || this.babiesCanAttack) && super.canContinueToUse();
    }
}
