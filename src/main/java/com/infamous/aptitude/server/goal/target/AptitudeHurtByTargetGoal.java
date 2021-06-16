package com.infamous.aptitude.server.goal.target;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;

public class AptitudeHurtByTargetGoal<T extends CreatureEntity> extends HurtByTargetGoal {
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

    protected void alertOther(MobEntity toAlert, LivingEntity target) {
        if (!toAlert.isBaby() || this.babiesCanAttack) {
            super.alertOther(toAlert, target);
        }
    }
}
