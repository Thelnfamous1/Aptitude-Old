package com.infamous.aptitude.server.goal.attack;

import com.infamous.aptitude.common.entity.IRearing;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;

public class RearingAttackGoal<T extends CreatureEntity & IRearing> extends AptitudeAttackGoal<T> {

    public RearingAttackGoal(T rearingCreature, double speedModifierIn, boolean mustSee) {
        super(rearingCreature, speedModifierIn, mustSee);
    }

    protected void checkAndPerformAttack(LivingEntity target, double distSqToTarget) {
        double attackReachSqr = this.getAttackReachSqr(target);
        if (distSqToTarget <= attackReachSqr && this.isTimeToAttack()) {
            this.resetAttackCooldown();
            this.mob.doHurtTarget(target);
            this.creature.stopRearing();
        } else if (distSqToTarget <= attackReachSqr * 2.0D) {
            if (this.isTimeToAttack()) {
                this.creature.stopRearing();
                this.resetAttackCooldown();
            }

            if (this.getTicksUntilNextAttack() <= 10) {
                this.creature.startRearing();
                this.creature.playAngrySound();
            }
        } else {
            this.resetAttackCooldown();
            this.creature.stopRearing();
        }

    }

    public void stop() {
        this.creature.stopRearing();
        super.stop();
    }
}
