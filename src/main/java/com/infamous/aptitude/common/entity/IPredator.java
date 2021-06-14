package com.infamous.aptitude.common.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;

public interface IPredator {

    int getHuntCooldown();

    void setHuntCooldown(int huntCooldown);

    default <T extends MobEntity & IPredator> boolean canHunt(T predator){
        if(this != predator) throw new IllegalArgumentException("Argument predator " + predator + " is not equal to this: " + this);

        return !predator.isBaby() && this.getHuntCooldown() <= 0;
    }

    boolean isPrey(LivingEntity living);


}
