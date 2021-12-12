package com.infamous.aptitude.server.goal.target;

import java.util.function.Predicate;
import javax.annotation.Nullable;

import com.infamous.aptitude.common.entity.IPredator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.entity.TamableAnimal;

public class UntamedHuntGoal<T extends LivingEntity, M extends TamableAnimal, P extends TamableAnimal & IPredator> extends HuntGoal<T, M, P> {

   public UntamedHuntGoal(M tameable, Class<T> targetType, int randomIntervalIn, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicateIn) {
      super(tameable, targetType, randomIntervalIn, mustSee, mustReach, targetPredicateIn);
   }

   public boolean canUse() {
      return !this.predator.isTame() && super.canUse();
   }
}