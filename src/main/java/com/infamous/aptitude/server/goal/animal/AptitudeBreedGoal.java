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

public class AptitudeBreedGoal<T extends MobEntity, A extends MobEntity & IAnimal> extends Goal {
   private static final EntityPredicate PARTNER_TARGETING = (new EntityPredicate()).range(8.0D).allowInvulnerable().allowSameTeam().allowUnseeable();
   protected final A animal;
   private final Class<? extends MobEntity> partnerClass;
   protected final World level;
   @Nullable
   protected A partnerMob;
   private int searchTime;
   private final int maxSearchTime;
   private int loveTime;
   private final double partnerSearchDist;
   private final double breedDistSq;
   private final double speedModifier;

   public AptitudeBreedGoal(T animalIn, double speedModifierIn, int maxLoveTimeIn, double partnerSearchDistIn, double breedDistIn) {
      this(animalIn, speedModifierIn, animalIn.getClass(), maxLoveTimeIn, partnerSearchDistIn, breedDistIn);
   }

   public AptitudeBreedGoal(T animalIn, double speedModifierIn, Class<? extends MobEntity> partnerClassIn, int maxSearchTimeIn, double partnerSearchDistIn, double breedDistIn) {
      if(animalIn instanceof IAnimal){
         this.animal = (A) animalIn;
      } else{
         throw new IllegalArgumentException("Invalid type for AptitudeBreedGoal: " + animalIn.getType());
      }
      this.level = animalIn.level;
      this.partnerClass = partnerClassIn;
      this.speedModifier = speedModifierIn;
      this.maxSearchTime = maxSearchTimeIn;
      this.partnerSearchDist = partnerSearchDistIn;
      this.breedDistSq = breedDistIn *  breedDistIn;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   public boolean canUse() {
      if (!this.animal.isInLove()) {
         return false;
      } else {
         this.partnerMob = this.getFreePartner();
         return this.partnerMob != null;
      }
   }

   public boolean canContinueToUse() {
      return this.partnerMob != null
              && this.partnerMob.isAlive()
              && this.animal.isInLove()
              && this.partnerMob.isInLove()
              && this.searchTime < this.maxSearchTime
              && this.loveTime < 60;
   }

   public void stop() {
      this.partnerMob = null;
      this.searchTime = 0;
      this.loveTime = 0;
   }

   public void tick() {
      if(this.partnerMob != null){
         this.animal.getLookControl().setLookAt(this.partnerMob, 10.0F, (float)this.animal.getMaxHeadXRot());
         this.animal.getNavigation().moveTo(this.partnerMob, this.speedModifier);
         ++this.searchTime;
         if (this.animal.distanceToSqr(this.partnerMob) < breedDistSq) {
            this.loveTime++;
            if(this.loveTime >= 60){
               this.breed();
            }
         }
      }

   }

   @Nullable
   private A getFreePartner() {
      List<MobEntity> list = this.level.getNearbyEntities(this.partnerClass, PARTNER_TARGETING, this.animal, this.animal.getBoundingBox().inflate(this.partnerSearchDist));
      double minDistSq = Double.MAX_VALUE;
      A freePartner = null;

      for(MobEntity nearbyMob : list) {
         A nearbyAnimal = nearbyMob instanceof IAnimal ? (A) nearbyMob : null;
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
      this.animal.spawnChildFromBreeding((ServerWorld)this.level, this.animal, this.partnerMob);
   }
}