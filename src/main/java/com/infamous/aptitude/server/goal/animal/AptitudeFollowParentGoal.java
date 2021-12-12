package com.infamous.aptitude.server.goal.animal;

import java.util.List;

import com.infamous.aptitude.common.entity.IAnimal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.entity.passive.AnimalEntity;

public class AptitudeFollowParentGoal<T extends Mob, A extends Mob & IAnimal> extends Goal {
   protected final A animal;
   private A parent;
   protected final double speedModifier;
   private int timeToRecalcPath;
   protected final double parentSearchDist;
   protected final double nearParentDistSq;

   public AptitudeFollowParentGoal(T animalIn, double speedModifierIn, double parentSearchDistIn, double nearParentDistIn) {
      if(animalIn instanceof IAnimal){
         this.animal = (A) animalIn;
      } else{
         throw new IllegalArgumentException("Invalid type for AptitudeFollowParentGoal: " + animalIn.getType());
      }
      this.speedModifier = speedModifierIn;
      this.parentSearchDist = parentSearchDistIn;
      this.nearParentDistSq = nearParentDistIn * nearParentDistIn;
   }

   public boolean canUse() {
      if (this.animal.getAge(this.animal) >= IAnimal.ADULT_AGE) {
         return false;
      } else {
         List<Mob> list = this.animal.level.getEntitiesOfClass(this.animal.getClass(), this.animal.getBoundingBox().inflate(this.parentSearchDist, this.parentSearchDist / 2, this.parentSearchDist));
         A parentAnimal = null;
         double minDistSq = Double.MAX_VALUE;

         for(Mob nearbyMob : list) {
            A nearbyAnimal = nearbyMob instanceof IAnimal ? (A) nearbyMob : null;
            if (nearbyAnimal != null && nearbyAnimal.getAge(nearbyAnimal) >= IAnimal.ADULT_AGE) {
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
      if (this.animal.getAge(this.animal) >= 0) {
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