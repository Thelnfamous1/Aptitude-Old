package com.infamous.aptitude.server.goal.horse;

import com.infamous.aptitude.common.entity.IRearable;
import com.infamous.aptitude.server.goal.AptitudeAttackGoal;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;

public class RearingAttackGoal<T extends CreatureEntity & IRearable> extends AptitudeAttackGoal {
    protected T horse;
    
    public RearingAttackGoal(T horse, double speedModifierIn, boolean mustSee) {
        super(horse, speedModifierIn, mustSee);
        this.horse = horse;
    }

    protected void checkAndPerformAttack(LivingEntity target, double distSqToTarget) {
        double attackReachSqr = this.getAttackReachSqr(target);
        if (distSqToTarget <= attackReachSqr && this.isTimeToAttack()) {
            this.resetAttackCooldown();
            this.mob.doHurtTarget(target);
            this.horse.stopRearing();
        } else if (distSqToTarget <= attackReachSqr * 2.0D) {
            if (this.isTimeToAttack()) {
                this.horse.stopRearing();
                this.resetAttackCooldown();
            }

            if (this.getTicksUntilNextAttack() <= 10) {
                this.horse.startRearing();
                this.horse.playAngrySound();
            }
        } else {
            this.resetAttackCooldown();
            this.horse.stopRearing();
        }

    }

    public void stop() {
        this.horse.stopRearing();
        super.stop();
    }
}
