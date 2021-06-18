package com.infamous.aptitude.server.goal.misc;

import com.infamous.aptitude.common.entity.IDevourer;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class DevourerFindItemsGoal<M extends MobEntity, D extends MobEntity & IDevourer> extends AptitudeFindItemsGoal<M> {
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
    protected boolean isUndesiredItem(ItemStack itemStack) {
        return super.isUndesiredItem(itemStack) || !itemStack.isEdible();
    }

    @Override
    protected ItemStack getFoundItemBySlot() {
        return this.devourer.getItemBySlot(this.devourer.getSlotForFood());
    }
}
