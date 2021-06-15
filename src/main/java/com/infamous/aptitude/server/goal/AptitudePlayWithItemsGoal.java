package com.infamous.aptitude.server.goal;

import com.infamous.aptitude.common.entity.IDevourer;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Predicate;

public class AptitudePlayWithItemsGoal<T extends MobEntity & IDevourer> extends Goal {
    private final T mob;
    private int cooldown;
    private final Predicate<ItemEntity> itemEntityPredicate;

    public AptitudePlayWithItemsGoal(T mob, Predicate<ItemEntity> itemEntityPredicate) {
        this.mob = mob;
        this.itemEntityPredicate = itemEntityPredicate;
    }

    public boolean canUse() {
        if (this.cooldown > this.mob.tickCount) { 
            return false;
        } else { 
            List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), this.itemEntityPredicate);

            ItemStack itemBySlot = this.mob.getItemBySlot(this.mob.getSlotForFood());
            boolean canEatFood = this.mob.canEat(this.mob, itemBySlot);

            return (!list.isEmpty() || !itemBySlot.isEmpty()) && !canEatFood;
        }
    }

    public void start() {
        List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), this.itemEntityPredicate);
        if (!list.isEmpty()) {
            this.mob.getNavigation().moveTo(list.get(0), (double)1.2F);
            this.playPlaySound();
        }

        this.cooldown = 0;
    }

    protected void playPlaySound() {
        this.mob.playAmbientSound();
    }

    public void stop() {
        ItemStack itemBySlot = this.mob.getItemBySlot(this.mob.getSlotForFood());
        boolean canEatFood = this.mob.canEat(this.mob, itemBySlot);
        if(canEatFood){
            this.cooldown = this.mob.tickCount + this.mob.getRandom().nextInt(100);
        } else if (!itemBySlot.isEmpty()) {
            this.drop(itemBySlot);
            this.mob.setItemSlot(this.mob.getSlotForFood(), ItemStack.EMPTY);
            this.cooldown = this.mob.tickCount + this.mob.getRandom().nextInt(100);
        }
    }

    public void tick() {
        List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), this.itemEntityPredicate);
        ItemStack itemstack = this.mob.getItemBySlot(this.mob.getSlotForFood());
        if (!itemstack.isEmpty()) {
            this.drop(itemstack);
            this.mob.setItemSlot(this.mob.getSlotForFood(), ItemStack.EMPTY);
        } else if (!list.isEmpty()) {
            this.mob.getNavigation().moveTo(list.get(0), (double)1.2F);
        }

    }

    private void drop(ItemStack stack) {
         if (!stack.isEmpty()) {
            double d0 = this.mob.getEyeY() - (double)0.3F;
            ItemEntity itemEntity = new ItemEntity(this.mob.level, this.mob.getX(), d0, this.mob.getZ(), stack);
            itemEntity.setPickUpDelay(40);
            itemEntity.setThrower(this.mob.getUUID());
            float f = 0.3F;
            float f1 = this.mob.getRandom().nextFloat() * ((float)Math.PI * 2F);
            float f2 = 0.02F * this.mob.getRandom().nextFloat();
            itemEntity.setDeltaMovement((double)(0.3F * -MathHelper.sin(this.mob.yRot * ((float)Math.PI / 180F)) * MathHelper.cos(this.mob.xRot * ((float)Math.PI / 180F)) + MathHelper.cos(f1) * f2), (double)(0.3F * MathHelper.sin(this.mob.xRot * ((float)Math.PI / 180F)) * 1.5F), (double)(0.3F * MathHelper.cos(this.mob.yRot * ((float)Math.PI / 180F)) * MathHelper.cos(this.mob.xRot * ((float)Math.PI / 180F)) + MathHelper.sin(f1) * f2));
            this.mob.level.addFreshEntity(itemEntity);
         }
    }
}