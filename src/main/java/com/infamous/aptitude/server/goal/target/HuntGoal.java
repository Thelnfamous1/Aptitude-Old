package com.infamous.aptitude.server.goal.target;

import com.infamous.aptitude.common.entity.IPredator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class HuntGoal<T extends LivingEntity, M extends Mob, P extends Mob & IPredator> extends NearestAttackableTargetGoal<T> {
    protected final P predator;

    public HuntGoal(M predatorIn, Class<T> targetType, int randomIntervalIn, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(predatorIn, targetType, randomIntervalIn, mustSee, mustReach, targetPredicate);
        if(predatorIn instanceof IPredator){
            this.predator = (P) predatorIn;
        } else{
            throw new IllegalArgumentException("Invalid type for HuntGoal: " + predatorIn.getType());
        }
    }

    @Override
    public boolean canUse() {
        return this.predator.canHunt(this.predator) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.predator.canHunt(this.predator) && super.canContinueToUse();
    }
}
