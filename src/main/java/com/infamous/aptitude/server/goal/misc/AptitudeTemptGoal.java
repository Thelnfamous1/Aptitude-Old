package com.infamous.aptitude.server.goal.misc;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.function.Predicate;

public class AptitudeTemptGoal extends Goal {
   private static final TargetingConditions TEMP_TARGETING = TargetingConditions.forNonCombat().range(10.0D).ignoreLineOfSight();
   protected final PathfinderMob mob;
   private final double speedModifier;
   private double px;
   private double py;
   private double pz;
   private double pRotX;
   private double pRotY;
   protected Player player;
   private int calmDown;
   private boolean isRunning;
   private final Predicate<ItemStack> temptItemPredicate;
   private final boolean canScare;

   public AptitudeTemptGoal(PathfinderMob creature, double p_i47822_2_, Predicate<ItemStack> p_i47822_4_, boolean p_i47822_5_) {
      this(creature, p_i47822_2_, p_i47822_5_, p_i47822_4_);
   }

   public AptitudeTemptGoal(PathfinderMob creature, double p_i47823_2_, boolean p_i47823_4_, Predicate<ItemStack> p_i47823_5_) {
      this.mob = creature;
      this.speedModifier = p_i47823_2_;
      this.temptItemPredicate = p_i47823_5_;
      this.canScare = p_i47823_4_;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   public boolean canUse() {
      if(this.mob.isAggressive()){
         return false;
      }
      if (this.calmDown > 0) {
         --this.calmDown;
         return false;
      } else {
         this.player = this.mob.level.getNearestPlayer(TEMP_TARGETING, this.mob);
         if (this.player == null) {
            return false;
         } else {
            return this.shouldFollowItem(this.player.getMainHandItem()) || this.shouldFollowItem(this.player.getOffhandItem());
         }
      }
   }

   protected boolean shouldFollowItem(ItemStack stack) {
      return temptItemPredicate.test(stack);
   }

   public boolean canContinueToUse() {
      if (this.canScare()) {
         if (this.mob.distanceToSqr(this.player) < 36.0D) {
            if (this.player.distanceToSqr(this.px, this.py, this.pz) > 0.010000000000000002D) {
               return false;
            }

            if (Math.abs((double)this.player.getXRot() - this.pRotX) > 5.0D || Math.abs((double)this.player.getYRot() - this.pRotY) > 5.0D) {
               return false;
            }
         } else {
            this.px = this.player.getX();
            this.py = this.player.getY();
            this.pz = this.player.getZ();
         }

         this.pRotX = (double)this.player.getXRot();
         this.pRotY = (double)this.player.getYRot();
      }

      return this.canUse();
   }

   protected boolean canScare() {
      return this.canScare;
   }

   public void start() {
      this.px = this.player.getX();
      this.py = this.player.getY();
      this.pz = this.player.getZ();
      this.isRunning = true;
   }

   public void stop() {
      this.player = null;
      this.mob.getNavigation().stop();
      this.calmDown = 100;
      this.isRunning = false;
   }

   public void tick() {
      this.mob.getLookControl().setLookAt(this.player, (float)(this.mob.getMaxHeadYRot() + 20), (float)this.mob.getMaxHeadXRot());
      if (this.mob.distanceToSqr(this.player) < 6.25D) {
         this.mob.getNavigation().stop();
      } else {
         this.mob.getNavigation().moveTo(this.player, this.speedModifier);
      }

   }

   public boolean isRunning() {
      return this.isRunning;
   }
}