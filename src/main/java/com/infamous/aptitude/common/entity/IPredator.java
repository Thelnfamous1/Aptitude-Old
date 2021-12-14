package com.infamous.aptitude.common.entity;

import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.Random;

public interface IPredator {
    UniformInt TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
    Random RANDOM = new Random();

    default void onHuntedPrey(LivingEntity killedEntity) {
        if(this.isPrey(killedEntity)){
            this.setHuntCooldown(getHuntInterval());
        }
    }

    int getHuntCooldown();

    void setHuntCooldown(int huntCooldown);

    default <T extends Mob & IPredator> boolean canHunt(T predator){
        if(this != predator) throw new IllegalArgumentException("Argument predator " + predator + " is not equal to this: " + this);

        return !predator.isBaby() && this.getHuntCooldown() <= 0;
    }

    boolean isPrey(LivingEntity living);

    default int getHuntInterval(){
        return TIME_BETWEEN_HUNTS.sample(RANDOM);
    }

}
