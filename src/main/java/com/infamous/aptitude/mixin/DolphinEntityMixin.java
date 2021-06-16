package com.infamous.aptitude.mixin;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.entity.IAgeable;
import com.infamous.aptitude.common.entity.IAnimal;
import com.infamous.aptitude.common.entity.IDevourer;
import com.infamous.aptitude.common.entity.IPredator;
import com.infamous.aptitude.common.util.AptitudePredicates;
import com.infamous.aptitude.server.goal.target.AptitudeHurtByTargetGoal;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(DolphinEntity.class)
public class DolphinEntityMixin extends WaterMobEntity implements IAnimal, IPredator, IDevourer {
    //private static final Ingredient DOLPHIN_FOOD_ITEMS = Ingredient.of(AptitudeResources.DOLPHINS_EAT);

    private static final DataParameter<Boolean> DATA_BABY_ID = EntityDataManager.defineId(DolphinEntity.class, DataSerializers.BOOLEAN);
    protected int age;
    protected int forcedAge;
    protected int forcedAgeTimer;
    private int inLove;
    private UUID loveCause;
    private int huntCooldown;
    private int ticksSinceEaten;
    private int eatCooldown;
    private boolean addedHurtByReplacements;

    protected DolphinEntityMixin(EntityType<? extends WaterMobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void onAboutToAddGoal(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.targetSelector && priority == 1 && goal instanceof HurtByTargetGoal && !this.addedHurtByReplacements){
            goalSelector.addGoal(priority, new AptitudeHurtByTargetGoal(this, GuardianEntity.class).setAlertOthers());
            this.addedHurtByReplacements = true;
        } else {
            goalSelector.addGoal(priority, goal);
        }
    }

    @Inject(at = @At("RETURN"), method = "defineSynchedData")
    private void addAgeableData(CallbackInfo ci){
        this.entityData.define(DATA_BABY_ID, false);
    }


    @Inject(at = @At("RETURN"), method = "addAdditionalSaveData")
    private void addSaveData(CompoundNBT compoundNBT, CallbackInfo ci){
        this.addAnimalData(compoundNBT);
        this.addAgeableData(this, compoundNBT);
    }

    @Inject(at = @At("RETURN"), method = "readAdditionalSaveData")
    private void readSaveData(CompoundNBT compoundNBT, CallbackInfo ci){
        this.readAnimalData(compoundNBT);
        this.readAgeableData(compoundNBT);
    }

    @Inject(at = @At("HEAD"), method = "handleEntityEvent", cancellable = true)
    private void checkCustomEvents(byte eventId, CallbackInfo ci){
        if(eventId == IAnimal.LOVE_ID){
            this.handleBreedEvent(this);
            ci.cancel();
        } else if(eventId == IDevourer.EAT_ID){
            this.handleEatEvent(this);
            ci.cancel();
        } else if(eventId == FINISHED_EATING_ID){
            this.onFinishedEating();
            ci.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "finalizeSpawn", cancellable = true)
    private void finalize(IServerWorld serverWorld, DifficultyInstance difficultyInstance, SpawnReason spawnReason, ILivingEntityData livingEntityData, CompoundNBT compoundNBT, CallbackInfoReturnable<ILivingEntityData> cir){
        ILivingEntityData ageableData = this.finalizeAgeableSpawn(livingEntityData);
        this.setHuntCooldown(getHuntInterval());
        cir.setReturnValue(super.finalizeSpawn(serverWorld, difficultyInstance, spawnReason, ageableData, compoundNBT));
    }

    @Inject(at = @At("HEAD"), method = "mobInteract", cancellable = true)
    private void handleAnimalInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResultType> cir){
        ActionResultType animalInteractResult = this.animalInteract(this, player, hand);
        if(animalInteractResult.consumesAction()){
            this.setPersistenceRequired();
            cir.setReturnValue(animalInteractResult);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "pickUpItem", cancellable = true)
    private void customPickUpItem(ItemEntity itemEntity, CallbackInfo ci){
        this.handlePickUpItem(this, itemEntity);

        ci.cancel();
        Aptitude.LOGGER.debug("Silently overwrote DolphinEntity#pickUpItem for {}", this);
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> dataParameter) {
        if (DATA_BABY_ID.equals(dataParameter)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(dataParameter);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        } else {
            this.setInLoveTime(0);
            return super.hurt(damageSource, amount);
        }
    }

    @Override
    public void killed(ServerWorld serverWorld, LivingEntity killedEntity) {
        super.killed(serverWorld, killedEntity);
        this.onHuntedPrey(killedEntity);
    }

    @Override
    protected void customServerAiStep() {
        this.animalCustomServerAiStep(this);
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.ageableAiStep(this);
        this.animalAiStep(this);
        this.devourerAiStep(this);
    }

    @Override
    public boolean canHoldItem(ItemStack stackToHold) {
        Item itemToHold = stackToHold.getItem();
        ItemStack itemBySlot = this.getItemBySlot(this.getSlotForFood());
        return itemBySlot.isEmpty()
                || this.getTicksSinceEaten() > 0
                    && (itemToHold.isEdible() && this.isFood(stackToHold))
                    && !(itemBySlot.getItem().isEdible() && this.isFood(stackToHold));
    }

    @Override
    public boolean removeWhenFarAway(double p_213397_1_) {
        return !this.isPersistenceRequired();
    }

    @Override
    public void setBaby(boolean isBaby) {
        this.setAge(isBaby ? IAgeable.BABY_AGE : IAgeable.ADULT_AGE);
    }

    @Override
    public boolean isBaby() {
        return this.getAge(this) < IAgeable.ADULT_AGE;
    }

    @Override
    public boolean getBabyData() {
        return this.entityData.get(DATA_BABY_ID);
    }

    @Override
    public void setBabyData(boolean isBaby) {
        this.entityData.set(DATA_BABY_ID, isBaby);
    }

    @Override
    public double getMyRidingOffset() {
        return IAnimal.RIDING_OFFSET;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends MobEntity & IAgeable> T getBreedOffspring(ServerWorld serverWorld, T ageable) {
        T dolphin = (T) EntityType.DOLPHIN.create(serverWorld);
        if(dolphin != null & ageable instanceof IAnimal && ((IAnimal) ageable).wasBredRecently()){
            dolphin.setPersistenceRequired();
        }
        return dolphin;
    }

    @Override
    public void setForcedAge(int forcedAge) {
        this.forcedAge = forcedAge;
    }

    @Override
    public void setAgeRaw(int ageIn) {
        this.age = ageIn;
    }

    @Override
    public int getForcedAge() {
        return this.forcedAge;
    }

    @Override
    public int getAgeRaw() {
        return this.age;
    }

    @Override
    public void setForcedAgeTimer(int i) {
        this.forcedAgeTimer = i;
    }

    @Override
    public int getForcedAgeTimer() {
        return this.forcedAgeTimer;
    }

    @Override
    public void setInLoveTime(int inLoveTicks) {
        this.inLove = inLoveTicks;
    }

    @Override
    public int getInLoveTime() {
        return this.inLove;
    }

    @Override
    public void setLoveCause(UUID uuid) {
        this.loveCause = uuid;
    }

    @Override
    public UUID getLoveCause() {
        return this.loveCause;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return AptitudePredicates.DOLPHIN_FOOD_PREDICATE.test(stack);
    }

    @Override
    public int getHuntCooldown() {
        return this.huntCooldown;
    }

    @Override
    public void setHuntCooldown(int huntCooldown) {
        this.huntCooldown = huntCooldown;
    }

    @Override
    public boolean isPrey(LivingEntity living) {
        return AptitudePredicates.DOLPHIN_PREY_PREDICATE.test(living);
    }

    @Override
    public int getTicksSinceEaten() {
        return this.ticksSinceEaten;
    }

    @Override
    public void setTicksSinceEaten(int ticksSinceEaten) {
        this.ticksSinceEaten = ticksSinceEaten;
    }

    @Override
    public int getEatCooldown() {
        return this.eatCooldown;
    }

    @Override
    public void setEatCooldown(int eatCooldown) {
        this.eatCooldown = eatCooldown;
    }

    @Override
    public void onFinishedEating() {
        if(!this.level.isClientSide && this.getAge(this) == ADULT_AGE && this.canFallInLove()){
            this.setInLove(this, null);
        }
        if (this.isBaby()) {
            this.ageUp(this, (int)((float)(-this.getAge(this) / 20) * 0.1F), true);
            this.level.broadcastEntityEvent(this, (byte) FINISHED_EATING_ID);
        }
    }

    @Override
    public <T extends MobEntity & IDevourer> boolean canEat(T devourer, ItemStack stack) {
        return this.isFood(stack) && IDevourer.super.canEat(devourer, stack);
    }

    @Override
    public SoundEvent getEatingSound(ItemStack p_213353_1_) {
        return SoundEvents.DOLPHIN_EAT;
    }

    @Override
    public SoundEvent getSpitOutItemSound() {
        return SoundEvents.DOLPHIN_PLAY;
    }

    @Override
    public void usePlayerItem(PlayerEntity player, ItemStack stack) {
        if(this.isFood(stack)){
            this.playSound(this.getEatingSound(stack), 1.0F, 1.0F);
            if(stack.isEdible()) {
                this.heal(stack.getItem().getFoodProperties().getNutrition());
            }
        }
        IAnimal.super.usePlayerItem(player, stack);
    }

    @Override
    public <T extends MobEntity & IAnimal> boolean canAcceptFood(T animal, ItemStack stack) {
        return this.isHungry(this);
    }
}
