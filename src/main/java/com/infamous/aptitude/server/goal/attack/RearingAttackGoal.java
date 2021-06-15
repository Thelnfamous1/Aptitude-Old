package com.infamous.aptitude.server.goal.attack;

import com.infamous.aptitude.common.entity.IRearing;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;

public class RearingAttackGoal<T extends CreatureEntity & IRearing> extends AptitudeAttackGoal {
    protected T rearingCreature;
    
    public RearingAttackGoal(T rearingCreature, double speedModifierIn, boolean mustSee) {
        super(rearingCreature, speedModifierIn, mustSee);
        this.rearingCreature = rearingCreature;
    }

    protected void checkAndPerformAttack(LivingEntity target, double distSqToTarget) {
        double attackReachSqr = this.getAttackReachSqr(target);
        if (distSqToTarget <= attackReachSqr && this.isTimeToAttack()) {
            this.resetAttackCooldown();
            this.mob.doHurtTarget(target);
            this.rearingCreature.stopRearing();
        } else if (distSqToTarget <= attackReachSqr * 2.0D) {
            if (this.isTimeToAttack()) {
                this.rearingCreature.stopRearing();
                this.resetAttackCooldown();
            }

            if (this.getTicksUntilNextAttack() <= 10) {
                this.rearingCreature.startRearing();
                this.rearingCreature.playAngrySound();
            }
        } else {
            this.resetAttackCooldown();
            this.rearingCreature.stopRearing();
        }

    }

    public void stop() {
        this.rearingCreature.stopRearing();
        super.stop();
    }
}
