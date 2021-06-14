package com.infamous.aptitude.server.goal.animal;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;

import com.infamous.aptitude.common.entity.IAnimal;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class AptitudeBreedGoal<T extends MobEntity & IAnimal> extends Goal {
   private static final EntityPredicate PARTNER_TARGETING = (new EntityPredicate()).range(8.0D).allowInvulnerable().allowSameTeam().allowUnseeable();
   protected final T animal;
   private final Class<? extends MobEntity> partnerClass;
   protected final World level;
   @Nullable
   protected T partner;
   private int loveTime;
   private final int maxLoveTime;
   private final double partnerSearchDist;
   private final double breedDistSq;
   private final double speedModifier;

   public AptitudeBreedGoal(T animalIn, double speedModifierIn, int maxLoveTimeIn, double partnerSearchDistIn, double breedDistIn) {
      this(animalIn, speedModifierIn, animalIn.getClass(), maxLoveTimeIn, partnerSearchDistIn, breedDistIn);
   }

   public AptitudeBreedGoal(T animalIn, double speedModifierIn, Class<? extends MobEntity> partnerClassIn, int maxLoveTimeIn, double partnerSearchDistIn, double breedDistIn) {
      this.animal = animalIn;
      this.level = animalIn.level;
      this.partnerClass = partnerClassIn;
      this.speedModifier = speedModifierIn;
      this.maxLoveTime = maxLoveTimeIn;
      this.partnerSearchDist = partnerSearchDistIn;
      this.breedDistSq = breedDistIn *  breedDistIn;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   public boolean canUse() {
      if (!this.animal.isInLove()) {
         return false;
      } else {
         this.partner = this.getFreePartner();
         return this.partner != null;
      }
   }

   public boolean canContinueToUse() {
      return this.partner != null && this.partner.isAlive() && this.partner.isInLove() && this.loveTime < this.maxLoveTime;
   }

   public void stop() {
      this.partner = null;
      this.loveTime = 0;
   }

   public void tick() {
      if(this.partner != null){
         this.animal.getLookControl().setLookAt(this.partner, 10.0F, (float)this.animal.getMaxHeadXRot());
         this.animal.getNavigation().moveTo(this.partner, this.speedModifier);
      }
      ++this.loveTime;
      if (this.loveTime >= this.maxLoveTime && this.partner != null && this.animal.distanceToSqr(this.partner) < breedDistSq) {
         this.breed();
      }

   }

   @Nullable
   private T getFreePartner() {
      List<MobEntity> list = this.level.getNearbyEntities(this.partnerClass, PARTNER_TARGETING, this.animal, this.animal.getBoundingBox().inflate(this.partnerSearchDist));
      double minDistSq = Double.MAX_VALUE;
      T freePartner = null;

      for(MobEntity nearbyMob : list) {
         T nearbyAnimal = nearbyMob instanceof IAnimal ? (T) nearbyMob : null;
         if (nearbyAnimal != null
                 && this.animal.canMate(nearbyAnimal)
                 && this.animal.distanceToSqr(nearbyAnimal) < minDistSq) {
            freePartner = nearbyAnimal;
            minDistSq = this.animal.distanceToSqr(nearbyAnimal);
         }
      }

      return freePartner;
   }

   protected void breed() {
      this.animal.spawnChildFromBreeding((ServerWorld)this.level, this.animal, this.partner);
   }
}