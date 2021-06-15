package com.infamous.aptitude.common.entity;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;

public interface IDevourer {
    int EAT_ID = 45;
    int FINISHED_EATING_ID = 2;

    default <T extends MobEntity & IDevourer> void eatsFoodAiStep(T devourer){
        if(this != devourer) throw new IllegalArgumentException("Argument devourer " + devourer + " is not equal to this: " + this);

        if (!devourer.level.isClientSide && devourer.isAlive() && devourer.isEffectiveAi()) {
            this.incrementTicksSinceEaten();
            ItemStack itemBySlot = devourer.getItemBySlot(this.getSlotForFood());
            if (this.canEat(devourer, itemBySlot)) {
                // dummy-proofing start/finish eat time, preferring finish
                if (this.getTicksSinceEaten() > Math.max(this.getFinishEatTime(), this.getStartEatTime())) {
                    ItemStack finishedItem = itemBySlot.finishUsingItem(devourer.level, devourer);
                    if (!finishedItem.isEmpty()) {
                        devourer.setItemSlot(this.getSlotForFood(), finishedItem);
                    }

                    this.onFinishedEating();
                    this.setTicksSinceEaten(0);
                }
                // dummy-proofing start/finish eat time, preferring start
                else if (this.getTicksSinceEaten() > Math.min(this.getStartEatTime(), this.getFinishEatTime())
                        && devourer.getRandom().nextFloat() < 0.1F) {
                    devourer.playSound(devourer.getEatingSound(itemBySlot), 1.0F, 1.0F);
                    devourer.level.broadcastEntityEvent(devourer, (byte)EAT_ID);
                }
            }
        }
    }

    default int getStartEatTime() {
        return 560;
    }

    default int getFinishEatTime() {
        return this.getStartEatTime() + 40;
    }

    default EquipmentSlotType getSlotForFood() {
        return EquipmentSlotType.MAINHAND;
    }

    default void onFinishedEating(){
        this.setEatCooldown(this.getEatInterval());
    }

    default <T extends MobEntity & IDevourer> void handleEatEvent(T devourer){
        if(this != devourer) throw new IllegalArgumentException("Argument devourer " + devourer + " is not equal to this: " + this);

        ItemStack itemBySlot = devourer.getItemBySlot(this.getSlotForFood());
        if (!itemBySlot.isEmpty()) {
            for(int i = 0; i < 8; ++i) {
                Vector3d eatSpeedVector = (new Vector3d(((double)devourer.getRandom().nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D)).xRot(-devourer.xRot * ((float)Math.PI / 180F)).yRot(-devourer.yRot * ((float)Math.PI / 180F));
                devourer.level.addParticle(new ItemParticleData(ParticleTypes.ITEM, itemBySlot), devourer.getX() + devourer.getLookAngle().x / 2.0D, devourer.getY(), devourer.getZ() + devourer.getLookAngle().z / 2.0D, eatSpeedVector.x, eatSpeedVector.y + 0.05D, eatSpeedVector.z);
            }
        }
    }

    default <T extends MobEntity & IDevourer> boolean canEat(T eatsFood, ItemStack stack) {
        return stack.getItem().isEdible()
                && !eatsFood.isAggressive()
                && !eatsFood.isSleeping()
                && this.getEatCooldown() <= 0;
    }

    default <T extends MobEntity & IDevourer> void handlePickUpItem(T devourer, ItemEntity itemEntity) {
        if(this != devourer) throw new IllegalArgumentException("Argument devourer " + devourer + " is not equal to this: " + this);

        ItemStack itemstack = itemEntity.getItem();
        if (devourer.canHoldItem(itemstack)) {
            int i = itemstack.getCount();
            if (i > 1) {
                this.dropItemStack(devourer, itemstack.split(i - 1));
            }

            this.spitOutItem(devourer, devourer.getItemBySlot(this.getSlotForFood()));
            devourer.onItemPickup(itemEntity);
            devourer.setItemSlot(this.getSlotForFood(), itemstack.split(1));
            devourer.setGuaranteedDrop(this.getSlotForFood());
            devourer.take(itemEntity, itemstack.getCount());
            itemEntity.remove();
            this.setTicksSinceEaten(0);
        }
    }

    default <T extends MobEntity & IDevourer> void spitOutItem(T devourer, ItemStack stackToSpitOut) {
        if(this != devourer) throw new IllegalArgumentException("Argument devourer " + devourer + " is not equal to this: " + this);

        if (!stackToSpitOut.isEmpty() && !devourer.level.isClientSide) {
            ItemEntity itemEntity = new ItemEntity(devourer.level, devourer.getX() + devourer.getLookAngle().x, devourer.getY() + 1.0D, devourer.getZ() + devourer.getLookAngle().z, stackToSpitOut);
            itemEntity.setPickUpDelay(40);
            itemEntity.setThrower(devourer.getUUID());
            devourer.playSound(this.getSpitOutItemSound(), 1.0F, 1.0F);
            devourer.level.addFreshEntity(itemEntity);
        }
    }

    default <T extends MobEntity & IDevourer> boolean isHungry(T devourer) {
        if(this != devourer) throw new IllegalArgumentException("Argument devourer " + devourer + " is not equal to this: " + this);

        return !devourer.isAggressive()
                && !devourer.isSleeping()
                && this.getEatCooldown() <= 0
                && devourer.getItemBySlot(this.getSlotForFood()).isEmpty();
    }

    SoundEvent getSpitOutItemSound();

    default <T extends MobEntity & IDevourer> void dropItemStack(T devourer, ItemStack stackToDrop) {
        if(this != devourer) throw new IllegalArgumentException("Argument devourer " + devourer + " is not equal to this: " + this);

        ItemEntity itemEntity = new ItemEntity(devourer.level, devourer.getX(), devourer.getY(), devourer.getZ(), stackToDrop);
        devourer.level.addFreshEntity(itemEntity);
    }

    int getTicksSinceEaten();

    void setTicksSinceEaten(int ticksSinceEaten);

    default void incrementTicksSinceEaten(){
        this.setTicksSinceEaten(this.getTicksSinceEaten() + 1);
    }

    int getEatCooldown();

    void setEatCooldown(int eatCooldown);

    default int getEatInterval(){
        return 200;
    }
}
