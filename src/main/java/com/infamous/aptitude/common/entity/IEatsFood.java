package com.infamous.aptitude.common.entity;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;

public interface IEatsFood {
    int EAT_ID = 45;
    int FINISHED_EATING_ID = 2;

    default <T extends MobEntity & IEatsFood> void eatsFoodAiStep(T eatsFood){
        if(this != eatsFood) throw new IllegalArgumentException("Argument eatsFood " + eatsFood + " is not equal to this: " + this);

        if (!eatsFood.level.isClientSide && eatsFood.isAlive() && eatsFood.isEffectiveAi()) {
            this.incrementTicksSinceEaten();
            ItemStack itemBySlot = eatsFood.getItemBySlot(this.getSlotForFood());
            if (this.canEat(eatsFood, itemBySlot)) {
                if (this.getTicksSinceEaten() > this.getFinishEatTime()) {
                    ItemStack finishedItem = itemBySlot.finishUsingItem(eatsFood.level, eatsFood);
                    if (!finishedItem.isEmpty()) {
                        eatsFood.setItemSlot(this.getSlotForFood(), finishedItem);
                    }

                    this.onFinishedEating();
                    this.setTicksSinceEaten(0);
                } else if (this.getTicksSinceEaten() > this.getStartEatTime()
                        && eatsFood.getRandom().nextFloat() < 0.1F) {
                    eatsFood.playSound(eatsFood.getEatingSound(itemBySlot), 1.0F, 1.0F);
                    eatsFood.level.broadcastEntityEvent(eatsFood, (byte)EAT_ID);
                }
            }
        }
    }

    default int getStartEatTime() {
        return 560;
    }

    default int getFinishEatTime() {
        return 600;
    }

    default EquipmentSlotType getSlotForFood() {
        return EquipmentSlotType.MAINHAND;
    }

    default void onFinishedEating(){
        this.setEatCooldown(this.getEatInterval());
    }

    default <T extends MobEntity & IEatsFood> void handleEatEvent(T eatsFood){
        if(this != eatsFood) throw new IllegalArgumentException("Argument eatsFood " + eatsFood + " is not equal to this: " + this);

        ItemStack itemBySlot = eatsFood.getItemBySlot(this.getSlotForFood());
        if (!itemBySlot.isEmpty()) {
            for(int i = 0; i < 8; ++i) {
                Vector3d eatSpeedVector = (new Vector3d(((double)eatsFood.getRandom().nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D)).xRot(-eatsFood.xRot * ((float)Math.PI / 180F)).yRot(-eatsFood.yRot * ((float)Math.PI / 180F));
                eatsFood.level.addParticle(new ItemParticleData(ParticleTypes.ITEM, itemBySlot), eatsFood.getX() + eatsFood.getLookAngle().x / 2.0D, eatsFood.getY(), eatsFood.getZ() + eatsFood.getLookAngle().z / 2.0D, eatSpeedVector.x, eatSpeedVector.y + 0.05D, eatSpeedVector.z);
            }
        }
    }

    default <T extends MobEntity & IEatsFood> boolean canEat(T eatsFood, ItemStack stack) {
        return stack.getItem().isEdible()
                && !eatsFood.isAggressive()
                && eatsFood.isOnGround()
                && !eatsFood.isSleeping()
                && this.getEatCooldown() <= 0;
    }

    default <T extends MobEntity & IEatsFood> void handlePickUpItem(T eatsFood, ItemEntity itemEntity) {
        if(this != eatsFood) throw new IllegalArgumentException("Argument eatsFood " + eatsFood + " is not equal to this: " + this);

        ItemStack itemstack = itemEntity.getItem();
        if (eatsFood.canHoldItem(itemstack)) {
            int i = itemstack.getCount();
            if (i > 1) {
                this.dropItemStack(eatsFood, itemstack.split(i - 1));
            }

            this.spitOutItem(eatsFood, eatsFood.getItemBySlot(this.getSlotForFood()));
            eatsFood.onItemPickup(itemEntity);
            eatsFood.setItemSlot(this.getSlotForFood(), itemstack.split(1));
            eatsFood.setGuaranteedDrop(this.getSlotForFood());
            eatsFood.take(itemEntity, itemstack.getCount());
            itemEntity.remove();
            this.setTicksSinceEaten(0);
        }
    }

    default <T extends MobEntity & IEatsFood> void spitOutItem(T eatsFood, ItemStack stackToSpitOut) {
        if(this != eatsFood) throw new IllegalArgumentException("Argument eatsFood " + eatsFood + " is not equal to this: " + this);

        if (!stackToSpitOut.isEmpty() && !eatsFood.level.isClientSide) {
            ItemEntity itemEntity = new ItemEntity(eatsFood.level, eatsFood.getX() + eatsFood.getLookAngle().x, eatsFood.getY() + 1.0D, eatsFood.getZ() + eatsFood.getLookAngle().z, stackToSpitOut);
            itemEntity.setPickUpDelay(40);
            itemEntity.setThrower(eatsFood.getUUID());
            eatsFood.playSound(this.getSpitOutItemSound(), 1.0F, 1.0F);
            eatsFood.level.addFreshEntity(itemEntity);
        }
    }

    SoundEvent getSpitOutItemSound();

    default <T extends MobEntity & IEatsFood> void dropItemStack(T eatsFood, ItemStack stackToDrop) {
        if(this != eatsFood) throw new IllegalArgumentException("Argument eatsFood " + eatsFood + " is not equal to this: " + this);

        ItemEntity itemEntity = new ItemEntity(eatsFood.level, eatsFood.getX(), eatsFood.getY(), eatsFood.getZ(), stackToDrop);
        eatsFood.level.addFreshEntity(itemEntity);
    }

    int getTicksSinceEaten();

    void setTicksSinceEaten(int ticksSinceEaten);

    default void incrementTicksSinceEaten(){
        this.setTicksSinceEaten(this.getTicksSinceEaten() + 1);
    }

    int getEatCooldown();

    void setEatCooldown(int eatCooldown);

    int getEatInterval();
}
