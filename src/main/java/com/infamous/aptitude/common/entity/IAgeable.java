package com.infamous.aptitude.common.entity;

import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Mob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Random;

public interface IAgeable {

    int BABY_AGE = -24000;
    int ADULT_AGE = 0;
    int FORCE_AGE_TIME = 40;

    void setBabyData(boolean isBaby);

    Random RANDOM = new Random();

    default SpawnGroupData finalizeAgeableSpawn(@Nullable SpawnGroupData livingEntityData){
        if (livingEntityData == null) {
            livingEntityData = new AgableMob.AgableMobGroupData(true);
        }

        AgableMob.AgableMobGroupData ageableentity$ageabledata = (AgableMob.AgableMobGroupData)livingEntityData;
        if (ageableentity$ageabledata.isShouldSpawnBaby()
                && ageableentity$ageabledata.getGroupSize() > 0
                && RANDOM.nextFloat() <= ageableentity$ageabledata.getBabySpawnChance()) {
            this.setAge(BABY_AGE);
        }

        ageableentity$ageabledata.increaseGroupSizeByOne();
        return livingEntityData;
    }

    @Nullable
    <T extends Mob & IAgeable> T getBreedOffspring(ServerLevel serverWorld, T ageable);

    default boolean canBreed(){
        return false;
    }

    default <T extends Mob & IAgeable> void addAgeableData(T ageable, CompoundTag compoundNBT){
        if(this != ageable) throw new IllegalArgumentException("Argument ageable " + ageable + " is not equal to this: " + this);

        compoundNBT.putInt("Age", this.getAge(ageable));
        compoundNBT.putInt("ForcedAge", this.getForcedAge());
    }

    default void readAgeableData(CompoundTag compoundNBT){
        this.setAge(compoundNBT.getInt("Age"));
        this.setForcedAge(compoundNBT.getInt("ForcedAge"));
    }

    void setForcedAge(int forcedAge);

    void setAgeRaw(int ageIn);

    default void setAge(int ageIn){
        int originalAge = this.getAgeRaw();
        this.setAgeRaw(ageIn);
        if (originalAge < ADULT_AGE && ageIn >= ADULT_AGE || originalAge >= ADULT_AGE && ageIn < ADULT_AGE) {
            this.setBabyData(ageIn < ADULT_AGE);
            this.ageBoundaryReached();
        }
    }

    int getForcedAge();

    default <T extends Mob & IAgeable> int getAge(T ageable){
        if(this != ageable) throw new IllegalArgumentException("Argument ageable " + ageable + " is not equal to this: " + this);

        if (ageable.level.isClientSide) {
            return this.getBabyData() ? -1 : 1;
        } else {
            return this.getAgeRaw();
        }
    }

    boolean getBabyData();

    int getAgeRaw();

    default <T extends Mob & IAgeable> void ageUp(T ageable, int ageIn, boolean forceAge) {
        if(this != ageable) throw new IllegalArgumentException("Argument ageable " + ageable + " is not equal to this: " + this);

        int age = this.getAge(ageable);
        age = age + ageIn * 20;
        if (age > ADULT_AGE) {
            age = ADULT_AGE;
        }

        int zeroAge = age - age;
        this.setAge(age);
        if (forceAge) {
            this.setForcedAge(this.getForcedAge() + zeroAge);
            if (this.getForcedAgeTimer() == 0) {
                this.setForcedAgeTimer(FORCE_AGE_TIME);
            }
        }

        if (this.getAge(ageable) == ADULT_AGE) {
            this.setAge(this.getForcedAge());
        }

    }

    void setForcedAgeTimer(int i);

    int getForcedAgeTimer();

    default <T extends Mob & IAgeable> void ageUp(T ageable, int ageIn) {
        this.ageUp(ageable, ageIn, false);
    }

    default <T extends Mob & IAgeable> void ageableAiStep(T ageable){
        if(this != ageable) throw new IllegalArgumentException("Argument ageable " + ageable + " is not equal to this: " + this);

        if (ageable.level.isClientSide) {
            if (this.getForcedAgeTimer() > 0) {
                if (this.getForcedAgeTimer() % 4 == 0) {
                    ageable.level.addParticle(ParticleTypes.HAPPY_VILLAGER, ageable.getRandomX(1.0D), ageable.getRandomY() + 0.5D, ageable.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
                }

                this.setForcedAgeTimer(this.getForcedAgeTimer() - 1);
            }
        } else if (ageable.isAlive()) {
            int age = this.getAge(ageable);
            if (age < ADULT_AGE) {
                ++age;
                this.setAge(age);
            } else if (age > ADULT_AGE) {
                --age;
                this.setAge(age);
            }
        }
    }

    default void ageBoundaryReached(){

    }
}
