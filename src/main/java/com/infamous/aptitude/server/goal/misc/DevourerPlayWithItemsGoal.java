package com.infamous.aptitude.server.goal.misc;

import com.infamous.aptitude.common.entity.IDevourer;
import com.infamous.aptitude.common.entity.IPlaysWithItems;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class DevourerPlayWithItemsGoal<M extends Mob & IPlaysWithItems, D extends Mob & IDevourer & IPlaysWithItems> extends AptitudePlayWithItemsGoal<M> {
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
    protected EquipmentSlot getPlayItemSlot() {
        return this.devourer.getSlotForFood();
    }
}
