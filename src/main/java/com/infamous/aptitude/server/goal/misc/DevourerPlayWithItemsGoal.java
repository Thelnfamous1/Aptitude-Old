package com.infamous.aptitude.server.goal.misc;

import com.infamous.aptitude.common.entity.IDevourer;
import com.infamous.aptitude.common.entity.IPlaysWithItems;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class DevourerPlayWithItemsGoal<M extends MobEntity & IPlaysWithItems, D extends MobEntity & IDevourer & IPlaysWithItems> extends AptitudePlayWithItemsGoal<M> {
    protected final D devourer;

    public DevourerPlayWithItemsGoal(M mob, Predicate<ItemEntity> predicate, int randomInterval) {
        super(mob, predicate, randomInterval);
        if(mob instanceof IDevourer){
            this.devourer = (D) mob;
        } else{
            throw new IllegalArgumentException("Invalid type for DevourerPlayWithItemsGoal: " + mob.getType());
        }
    }

    @Override
    public boolean canUse() {
        return super.canUse() && !this.canEatHeldItem();
    }

    @Override
    public void tick() {
        if(!this.canEatHeldItem()){
            super.tick();
        }
    }

    @Override
    public void stop() {
        if(!this.canEatHeldItem()){
            this.generateCooldown();
            super.stop();
        }
    }

    protected boolean canEatHeldItem() {
        ItemStack itemBySlot = this.devourer.getItemBySlot(this.devourer.getSlotForFood());
        return this.devourer.canEat(this.devourer, itemBySlot);
    }

    @Override
    protected EquipmentSlotType getPlayItemSlot() {
        return this.devourer.getSlotForFood();
    }
}
