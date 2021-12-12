package com.infamous.aptitude.common.entity;

import com.infamous.aptitude.server.advancement.IAptitudeBredAnimalsTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.BredAnimalsTrigger;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameRules;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IAnimal extends IAgeable {

    int LOVE_ID = 18;
    double RIDING_OFFSET = 0.14D;
    int BREED_COOLDOWN = 6000;
    int LOVE_TICKS = 600;

    default <T extends Mob & IAnimal> void animalCustomServerAiStep(T animal){
        if(this != animal) throw new IllegalArgumentException("Argument animal " + animal + " is not equal to this: " + this);

        if (this.getAge(animal) != 0) {
            this.setInLoveTime(0);
        }
    }

    void setInLoveTime(int inLoveTicks);

    default  <T extends Mob & IAnimal> void animalAiStep(T animal){
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

    default void addAnimalData(CompoundTag compoundNBT){
        compoundNBT.putInt("InLove", this.getInLoveTime());
        if (this.getLoveCause() != null) {
            compoundNBT.putUUID("LoveCause", this.getLoveCause());
        }
    }

    default void readAnimalData(CompoundTag compoundNBT){
        this.setInLoveTime(compoundNBT.getInt("InLove"));
        this.setLoveCause(compoundNBT.hasUUID("LoveCause") ? compoundNBT.getUUID("LoveCause") : null);

    }

    void setLoveCause(UUID uuid);

    UUID getLoveCause();

    boolean isFood(ItemStack stack);

    default  <T extends Mob & IAnimal> void handleBreedEvent(T animal){
        if(this != animal) throw new IllegalArgumentException("Argument animal " + animal + " is not equal to this: " + this);

        for(int i = 0; i < 7; ++i) {
            double d0 = animal.getRandom().nextGaussian() * 0.02D;
            double d1 = animal.getRandom().nextGaussian() * 0.02D;
            double d2 = animal.getRandom().nextGaussian() * 0.02D;
            animal.level.addParticle(ParticleTypes.HEART, animal.getRandomX(1.0D), animal.getRandomY() + 0.5D, animal.getRandomZ(1.0D), d0, d1, d2);
        }
    }

    default  <T extends Mob & IAnimal> void spawnChildFromBreeding(ServerLevel serverWorld, T parent, T partner) {
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
            ServerPlayer serverplayerentity = getLoveCausePlayer(parent);
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
                serverWorld.addFreshEntity(new ExperienceOrb(serverWorld, parent.getX(), parent.getY(), parent.getZ(), parent.getRandom().nextInt(7) + 1));
            }

        }
    }

    default void resetLove(){
        this.setInLoveTime(0);
    }

    @Nullable
    default <T extends Mob & IAnimal> ServerPlayer getLoveCausePlayer(T animal){
        if(this != animal) throw new IllegalArgumentException("Argument animal " + animal + " is not equal to this: " + this);

        if (this.getLoveCause() == null) {
            return null;
        } else {
            Player playerentity = animal.level.getPlayerByUUID(this.getLoveCause());
            return playerentity instanceof ServerPlayer ? (ServerPlayer)playerentity : null;
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

    default  <T extends Mob & IAnimal> InteractionResult animalInteract(T animal, Player player, InteractionHand hand){
        if(this != animal) throw new IllegalArgumentException("Argument animal " + animal + " is not equal to this: " + this);

        ItemStack itemInHand = player.getItemInHand(hand);
        if (this.isFood(itemInHand) && this.canAcceptFood(animal, itemInHand)) {
            int age = this.getAge(animal);
            if (!animal.level.isClientSide
                    && age == ADULT_AGE
                    && this.canFallInLove()) {
                this.usePlayerItem(player, itemInHand);
                this.setInLove(animal, player);
                return InteractionResult.SUCCESS;
            }

            if (animal.isBaby()) {
                this.usePlayerItem(player, itemInHand);
                this.ageUp(animal, (int)((float)(-age / 20) * 0.1F), true);
                return InteractionResult.sidedSuccess(animal.level.isClientSide);
            }

            if (animal.level.isClientSide) {
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    default <T extends Mob & IAnimal> boolean canAcceptFood(T animal, ItemStack stack){
        if(this != animal) throw new IllegalArgumentException("Argument animal " + animal + " is not equal to this: " + this);

        return !animal.isAggressive() && !animal.isSleeping();
    }

    default void usePlayerItem(Player player, ItemStack stack) {
        if (!player.abilities.instabuild) {
            stack.shrink(1);
        }
    }

    default boolean canFallInLove() {
        return this.getInLoveTime() <= 0;
    }

    default <T extends Mob & IAnimal> void setInLove(T animal, @Nullable Player playerEntity) {
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
