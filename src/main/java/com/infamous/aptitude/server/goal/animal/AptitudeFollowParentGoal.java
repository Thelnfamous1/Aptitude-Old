package com.infamous.aptitude.server.goal.animal;

import java.util.List;

import com.infamous.aptitude.common.entity.IAnimal;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.AnimalEntity;

public class AptitudeFollowParentGoal<T extends MobEntity & IAnimal> extends Goal {
   private final T animal;
   private T parent;
   private final double speedModifier;
   private int timeToRecalcPath;
   private final double parentSearchDist;
   private final double nearParentDistSq;

   public AptitudeFollowParentGoal(T animalIn, double speedModifierIn, double parentSearchDistIn, double nearParentDistIn) {
      this.animal = animalIn;
      this.speedModifier = speedModifierIn;
      this.parentSearchDist = parentSearchDistIn;
      this.nearParentDistSq = nearParentDistIn * nearParentDistIn;
   }

   public boolean canUse() {
      if (this.animal.getAge() >= IAnimal.ADULT_AGE) {
         return false;
      } else {
         List<MobEntity> list = this.animal.level.getEntitiesOfClass(this.animal.getClass(), this.animal.getBoundingBox().inflate(this.parentSearchDist, this.parentSearchDist / 2, this.parentSearchDist));
         T parentAnimal = null;
         double minDistSq = Double.MAX_VALUE;

         for(MobEntity nearbyMob : list) {
            T nearbyAnimal = nearbyMob instanceof IAnimal ? (T) nearbyMob : null;
            if (nearbyAnimal != null && nearbyAnimal.getAge() >= IAnimal.ADULT_AGE) {
               double distSqrToNearby = this.animal.distanceToSqr(nearbyAnimal);
               if (!(distSqrToNearby > minDistSq)) {
                  minDistSq = distSqrToNearby;
                  parentAnimal = nearbyAnimal;
               }
            }
         }

         if (parentAnimal == null) {
            return false;
         } else if (minDistSq < this.nearParentDistSq) {
            return false;
         } else {
            this.parent = parentAnimal;
            return true;
         }
      }
   }

   public boolean canContinueToUse() {
      if (this.animal.getAge() >= 0) {
         return false;
      } else if (!this.parent.isAlive()) {
         return false;
      } else {
         double distSqrToParent = this.animal.distanceToSqr(this.parent);
         return !(distSqrToParent < this.nearParentDistSq) && !(distSqrToParent > 256.0D);
      }
   }

   public void start() {
      this.timeToRecalcPath = 0;
   }

   public void stop() {
      this.parent = null;
   }

   public void tick() {
      if (--this.timeToRecalcPath <= 0) {
         this.timeToRecalcPath = 10;
         this.animal.getNavigation().moveTo(this.parent, this.speedModifier);
      }
   }
}