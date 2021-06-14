package com.infamous.aptitude.common.entity;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Random;

public interface IAgeable {

    int BABY_AGE = -24000;
    int ADULT_AGE = 0;
    int FORCE_AGE_TIME = 40;

    void setBabyData(boolean isBaby);

    Random RANDOM = new Random();

    default ILivingEntityData finalizeAgeableSpawn(@Nullable ILivingEntityData livingEntityData){
        if (livingEntityData == null) {
            livingEntityData = new AgeableEntity.AgeableData(true);
        }

        AgeableEntity.AgeableData ageableentity$ageabledata = (AgeableEntity.AgeableData)livingEntityData;
        if (ageableentity$ageabledata.isShouldSpawnBaby()
                && ageableentity$ageabledata.getGroupSize() > 0
                && RANDOM.nextFloat() <= ageableentity$ageabledata.getBabySpawnChance()) {
            this.setAge(BABY_AGE);
        }

        ageableentity$ageabledata.increaseGroupSizeByOne();
        return livingEntityData;
    }

    @Nullable
    <T extends MobEntity & IAgeable> T getBreedOffspring(ServerWorld serverWorld, T ageable);

    default boolean canBreed(){
        return false;
    }

    default void addAgeableData(CompoundNBT compoundNBT){
        compoundNBT.putInt("Age", this.getAge());
        compoundNBT.putInt("ForcedAge", this.getForcedAge());
    }

    default void readAgeableData(CompoundNBT compoundNBT){
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

    int getAge();

    int getAgeRaw();

    default void ageUp(int ageIn, boolean forceAge) {
        int age = this.getAge();
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

        if (this.getAge() == ADULT_AGE) {
            this.setAge(this.getForcedAge());
        }

    }

    void setForcedAgeTimer(int i);

    int getForcedAgeTimer();

    default void ageUp(int ageIn) {
        this.ageUp(ageIn, false);
    }

    default  <T extends MobEntity & IAgeable> void ageableAiStep(T ageable){
        if(this != ageable) return;

        if (ageable.level.isClientSide) {
            if (this.getForcedAgeTimer() > 0) {
                if (this.getForcedAgeTimer() % 4 == 0) {
                    ageable.level.addParticle(ParticleTypes.HAPPY_VILLAGER, ageable.getRandomX(1.0D), ageable.getRandomY() + 0.5D, ageable.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
                }

                this.setForcedAgeTimer(this.getForcedAgeTimer() - 1);
            }
        } else if (ageable.isAlive()) {
            int age = this.getAge();
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
