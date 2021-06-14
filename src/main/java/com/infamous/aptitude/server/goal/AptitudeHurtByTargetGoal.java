package com.infamous.aptitude.server.goal;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;

public class AptitudeHurtByTargetGoal extends HurtByTargetGoal {

    public AptitudeHurtByTargetGoal(CreatureEntity creature, Class<?>... toIgnoreDamage) {
        super(creature, toIgnoreDamage);
    }

    public void start() {
        super.start();
        if (this.mob.isBaby()) {
            this.alertOthers();
            this.stop();
        }

    }

    protected void alertOther(MobEntity toAlert, LivingEntity target) {
        if (!toAlert.isBaby()) {
            super.alertOther(toAlert, target);
        }

    }
}
