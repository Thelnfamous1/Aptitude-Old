package com.infamous.aptitude.server.goal.target;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

public class AptitudeHurtByTargetGoal<T extends PathfinderMob> extends HurtByTargetGoal {
    private boolean babiesCanAttack;
    protected final T creatureAsGeneric;

    public AptitudeHurtByTargetGoal(T creature, Class<?>... toIgnoreDamage) {
        super(creature, toIgnoreDamage);
        this.creatureAsGeneric = creature;
    }

    public AptitudeHurtByTargetGoal<T> setBabiesCanAttack(){
        this.babiesCanAttack = true;
        return this;
    }

    public void start() {
        super.start();
        if (this.mob.isBaby() && !this.babiesCanAttack) {
            this.alertOthers();
            this.stop();
        }

    }

    protected void alertOther(Mob toAlert, LivingEntity target) {
        if (!toAlert.isBaby() || this.babiesCanAttack) {
            super.alertOther(toAlert, target);
        }
    }
}
