package com.infamous.aptitude.common.entity;

import com.infamous.aptitude.server.advancement.IAptitudeBredAnimalsTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.BredAnimalsTrigger;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IAnimal extends IAgeable {

    int LOVE_ID = 18;
    double RIDING_OFFSET = 0.14D;
    int BREED_COOLDOWN = 6000;
    int LOVE_TICKS = 600;

    default <T extends MobEntity & IAnimal> void animalCustomServerAiStep(T animal){
        if(this != animal) throw new IllegalArgumentException("Argument animal " + animal + " is not equal to this: " + this);

        if (this.getAge(animal) != 0) {
            this.setInLoveTime(0);
        }
    }

    void setInLoveTime(int inLoveTicks);

    default  <T extends MobEntity & IAnimal> void animalAiStep(T animal){
        if(this != animal) throw new IllegalArgumentException("Argument animal " + animal + " is not equal to this: " + this);

        if (this.getAge(animal) != ADULT_AGE) {
            this.setInLoveTime(0);
        }

        if (this.getInLoveTime() > 0) {
            this.setInLoveTime(this.getInLoveTime() - 1);
            if (this.getInLoveTime() % 10 == 0) {
                double d0 = animal.getRandom().nextGaussian() * 0.02D;
                double d1 = animal.getRandom().nextGaussian() * 0.02D;
                double d2 = animal.getRandom().nextGaussian() * 0.02D;
                animal.level.addParticle(ParticleTypes.HEART, animal.getRandomX(1.0D), animal.getRandomY() + 0.5D, animal.getRandomZ(1.0D), d0, d1, d2);
            }
        }
    }

    int getInLoveTime();

    default void addAnimalData(CompoundNBT compoundNBT){
        compoundNBT.putInt("InLove", this.getInLoveTime());
        if (this.getLoveCause() != null) {
            compoundNBT.putUUID("LoveCause", this.getLoveCause());
        }
    }

    default void readAnimalData(CompoundNBT compoundNBT){
        this.setInLoveTime(compoundNBT.getInt("InLove"));
        this.setLoveCause(compoundNBT.hasUUID("LoveCause") ? compoundNBT.getUUID("LoveCause") : null);

    }

    void setLoveCause(UUID uuid);

    UUID getLoveCause();

    boolean isFood(ItemStack stack);

    default  <T extends MobEntity & IAnimal> void handleBreedEvent(T animal){
        if(this != animal) throw new IllegalArgumentException("Argument animal " + animal + " is not equal to this: " + this);

        for(int i = 0; i < 7; ++i) {
            double d0 = animal.getRandom().nextGaussian() * 0.02D;
            double d1 = animal.getRandom().nextGaussian() * 0.02D;
            double d2 = animal.getRandom().nextGaussian() * 0.02D;
            animal.level.addParticle(ParticleTypes.HEART, animal.getRandomX(1.0D), animal.getRandomY() + 0.5D, animal.getRandomZ(1.0D), d0, d1, d2);
        }
    }

    default  <T extends MobEntity & IAnimal> void spawnChildFromBreeding(ServerWorld serverWorld, T parent, T partner) {
        if(this != parent) throw new IllegalArgumentException("Argument parent " + parent + " is not equal to this: " + this);

        T child = this.getBreedOffspring(serverWorld, partner);
        /*
        final net.minecraftforge.event.entity.living.BabyEntitySpawnEvent event = new net.minecraftforge.event.entity.living.BabyEntitySpawnEvent(this, partner, child);
        final boolean cancelled = net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
        child = event.getChild();
        if (cancelled) {
            //Reset the "inLove" state for the animals
            parent.setAge(MAX_AGE);
            partner.setAge(MAX_AGE);
            parent.resetLove();
            partner.resetLove();
            return;
        }
         */
        if (child != null) {
            ServerPlayerEntity serverplayerentity = getLoveCausePlayer(parent);
            if (serverplayerentity == null && partner.getLoveCause() != null) {
                serverplayerentity = getLoveCausePlayer(partner);
            }

            if (serverplayerentity != null) {
                serverplayerentity.awardStat(Stats.ANIMALS_BRED);
                BredAnimalsTrigger bredAnimalsTrigger = CriteriaTriggers.BRED_ANIMALS;
                if(bredAnimalsTrigger instanceof IAptitudeBredAnimalsTrigger){
                    ((IAptitudeBredAnimalsTrigger) bredAnimalsTrigger).trigger(serverplayerentity, parent, partner, child);
                }
            }

            this.setAge(BREED_COOLDOWN);
            partner.setAge(BREED_COOLDOWN);
            this.resetLove();
            partner.resetLove();
            child.setBaby(true);
            child.moveTo(parent.getX(), parent.getY(), parent.getZ(), 0.0F, 0.0F);
            serverWorld.addFreshEntityWithPassengers(child);
            serverWorld.broadcastEntityEvent(parent, (byte)LOVE_ID);
            if (serverWorld.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                serverWorld.addFreshEntity(new ExperienceOrbEntity(serverWorld, parent.getX(), parent.getY(), parent.getZ(), parent.getRandom().nextInt(7) + 1));
            }

        }
    }

    default void resetLove(){
        this.setInLoveTime(0);
    }

    @Nullable
    default <T extends MobEntity & IAnimal> ServerPlayerEntity getLoveCausePlayer(T animal){
        if(this != animal) throw new IllegalArgumentException("Argument animal " + animal + " is not equal to this: " + this);

        if (this.getLoveCause() == null) {
            return null;
        } else {
            PlayerEntity playerentity = animal.level.getPlayerByUUID(this.getLoveCause());
            return playerentity instanceof ServerPlayerEntity ? (ServerPlayerEntity)playerentity : null;
        }
    }

    default boolean canMate(IAnimal partner) {
        if (partner == this) {
            return false;
        } else if (partner.getClass() != this.getClass()) {
            return false;
        } else {
            return this.isInLove() && partner.isInLove();
        }
    }

    default boolean isInLove(){
        return this.getInLoveTime() > 0;
    }

    default  <T extends MobEntity & IAnimal> ActionResultType animalInteract(T animal, PlayerEntity player, Hand hand){
        if(this != animal) throw new IllegalArgumentException("Argument animal " + animal + " is not equal to this: " + this);

        ItemStack itemInHand = player.getItemInHand(hand);
        if (this.isFood(itemInHand) && this.canAcceptFood(animal, itemInHand)) {
            int age = this.getAge(animal);
            if (!animal.level.isClientSide
                    && age == ADULT_AGE
                    && this.canFallInLove()) {
                this.usePlayerItem(player, itemInHand);
                this.setInLove(animal, player);
                return ActionResultType.SUCCESS;
            }

            if (animal.isBaby()) {
                this.usePlayerItem(player, itemInHand);
                this.ageUp(animal, (int)((float)(-age / 20) * 0.1F), true);
                return ActionResultType.sidedSuccess(animal.level.isClientSide);
            }

            if (animal.level.isClientSide) {
                return ActionResultType.CONSUME;
            }
        }
        return ActionResultType.PASS;
    }

    default <T extends MobEntity & IAnimal> boolean canAcceptFood(T animal, ItemStack stack){
        if(this != animal) throw new IllegalArgumentException("Argument animal " + animal + " is not equal to this: " + this);

        return !animal.isAggressive() && !animal.isSleeping();
    }

    default void usePlayerItem(PlayerEntity player, ItemStack stack) {
        if (!player.abilities.instabuild) {
            stack.shrink(1);
        }
    }

    default boolean canFallInLove() {
        return this.getInLoveTime() <= 0;
    }

    default <T extends MobEntity & IAnimal> void setInLove(T animal, @Nullable PlayerEntity playerEntity) {
        if(this != animal) throw new IllegalArgumentException("Argument animal " + animal + " is not equal to this: " + this);

        this.setInLoveTime(LOVE_TICKS);
        if (playerEntity != null) {
            this.setLoveCause(playerEntity.getUUID());
        } else{
            this.setLoveCause(null);
        }

        animal.level.broadcastEntityEvent(animal, (byte)LOVE_ID);
    }

    default boolean wasBredRecently(){
        return this.getLoveCause() != null;
    }
}
