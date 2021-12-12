package com.infamous.aptitude.server.goal.misc;

import com.infamous.aptitude.common.entity.IDevourer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class DevourerFindItemsGoal<M extends Mob, D extends Mob & IDevourer> extends AptitudeFindItemsGoal<M> {
    protected final D devourer;

    public DevourerFindItemsGoal(M mob, Predicate<ItemEntity> predicate, int randomInterval) {
        super(mob, predicate, randomInterval);
        if(mob instanceof IDevourer){
            this.devourer = (D) mob;
        } else{
            throw new IllegalArgumentException("Invalid type for DevourerFindItemsGoal: " + mob.getType());
        }
    }

    @Override
    protected boolean shouldFindItem(ItemStack itemStack) {
        return super.shouldFindItem(itemStack) || !this.devourer.canEat(this.devourer, itemStack);
    }

    @Override
    protected ItemStack getFoundItemBySlot() {
        return this.devourer.getItemBySlot(this.devourer.getSlotForFood());
    }
}
