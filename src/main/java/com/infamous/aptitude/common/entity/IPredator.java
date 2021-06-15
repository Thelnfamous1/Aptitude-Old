package com.infamous.aptitude.common.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.TickRangeConverter;

import java.util.Random;

public interface IPredator {
    RangedInteger TIME_BETWEEN_HUNTS = TickRangeConverter.rangeOfSeconds(30, 120);
    Random RANDOM = new Random();

    default void onHuntedPrey(LivingEntity killedEntity) {
        if(this.isPrey(killedEntity)){
            this.setHuntCooldown(getHuntInterval());
        }
    }

    int getHuntCooldown();

    void setHuntCooldown(int huntCooldown);

    default <T extends MobEntity & IPredator> boolean canHunt(T predator){
        if(this != predator) throw new IllegalArgumentException("Argument predator " + predator + " is not equal to this: " + this);

        return !predator.isBaby() && this.getHuntCooldown() <= 0;
    }

    boolean isPrey(LivingEntity living);

    default int getHuntInterval(){
        return TIME_BETWEEN_HUNTS.randomValue(RANDOM);
    }

}
