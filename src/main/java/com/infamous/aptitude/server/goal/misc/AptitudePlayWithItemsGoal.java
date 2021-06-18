package com.infamous.aptitude.server.goal.misc;

import com.infamous.aptitude.common.entity.IPlaysWithItems;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Predicate;

public class AptitudePlayWithItemsGoal<T extends MobEntity & IPlaysWithItems> extends Goal {
    protected final T mob;
    protected int cooldown;
    protected final Predicate<ItemEntity> itemEntityPredicate;
    protected final int cooldownInterval;

    public AptitudePlayWithItemsGoal(T mob, Predicate<ItemEntity> itemEntityPredicate, int cooldownInterval) {
        this.mob = mob;
        this.itemEntityPredicate = itemEntityPredicate;
        this.cooldownInterval = cooldownInterval;
    }

    public boolean canUse() {
        if (this.cooldown > this.mob.tickCount) { 
            return false;
        } else { 
            List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), this.itemEntityPredicate);
            ItemStack playItem = this.mob.getItemBySlot(this.getPlayItemSlot());
            return !list.isEmpty() || !playItem.isEmpty();
        }
    }

    protected EquipmentSlotType getPlayItemSlot() {
        return EquipmentSlotType.MAINHAND;
    }

    public void start() {
        List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), this.itemEntityPredicate);
        if (!list.isEmpty()) {
            this.mob.getNavigation().moveTo(list.get(0), (double)1.2F);
            this.mob.playSound(this.mob.getPlayingSound(), 1.0F, 1.0F);
        }

        this.resetCooldown();
    }

    protected void resetCooldown() {
        this.cooldown = 0;
    }

    public void stop() {
        ItemStack playItem = this.mob.getItemBySlot(this.getPlayItemSlot());
        if (!playItem.isEmpty()) {
            this.drop(playItem);
            this.mob.setItemSlot(this.getPlayItemSlot(), ItemStack.EMPTY);
            this.generateCooldown();
        }
    }

    protected void generateCooldown() {
        this.cooldown = this.mob.tickCount + this.mob.getRandom().nextInt(this.cooldownInterval);
    }

    public void tick() {
        List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), this.itemEntityPredicate);
        ItemStack playItem = this.mob.getItemBySlot(this.getPlayItemSlot());
        if (!playItem.isEmpty()) {
            this.drop(playItem);
            this.mob.setItemSlot(this.getPlayItemSlot(), ItemStack.EMPTY);
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