package com.infamous.aptitude.server.goal.misc;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class AptitudeFindItemsGoal<T extends MobEntity> extends Goal {
    protected final T mob;
    private final Predicate<ItemEntity> itemEntityPredicate;
    protected final int randomInterval;

    public AptitudeFindItemsGoal(T mob, Predicate<ItemEntity> itemEntityPredicate, int randomInterval) {
        this.mob = mob;
        this.itemEntityPredicate = itemEntityPredicate;
        this.randomInterval = randomInterval;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

      public boolean canUse() {
         if (!this.getFoundItemBySlot().isEmpty()) {
            return false;
         } else if (this.mob.getTarget() == null && this.mob.getLastHurtByMob() == null) {
            if (!this.canMove()) {
               return false;
            } else if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
               return false;
            } else {
               List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), this.itemEntityPredicate);
               return !list.isEmpty() && this.getFoundItemBySlot().isEmpty();
            }
         } else {
            return false;
         }
      }

    protected ItemStack getFoundItemBySlot() {
        return this.mob.getItemBySlot(EquipmentSlotType.MAINHAND);
    }

    protected boolean canMove() {
        return !this.mob.isSleeping();
    }

    public void tick() {
         List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), this.itemEntityPredicate);
         ItemStack itemBySlot = this.getFoundItemBySlot();
         if (itemBySlot.isEmpty() && !list.isEmpty()) {
            this.mob.getNavigation().moveTo(list.get(0), (double)1.2F);
         }

      }

      public void start() {
         List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), this.itemEntityPredicate);
         if (!list.isEmpty()) {
            this.mob.getNavigation().moveTo(list.get(0), (double)1.2F);
         }

      }
   }