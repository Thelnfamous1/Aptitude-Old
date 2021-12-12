package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.entity.*;
import com.infamous.aptitude.common.util.AptitudeHelper;
import com.infamous.aptitude.common.util.AptitudePredicates;
import com.infamous.aptitude.server.goal.misc.DevourerPlayWithItemsGoal;
import com.infamous.aptitude.server.goal.target.AptitudeHurtByTargetGoal;
import net.minecraft.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.*;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.UUID;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;

@Mixin(Dolphin.class)
public abstract class DolphinEntityMixin extends WaterAnimal implements IAnimal, IPredator, IDevourer, IPlaysWithItems {
    //private static final Ingredient DOLPHIN_FOOD_ITEMS = Ingredient.of(AptitudeResources.DOLPHINS_EAT);

    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Dolphin.class, EntityDataSerializers.BOOLEAN);
    protected int age;
    protected int forcedAge;
    protected int forcedAgeTimer;
    private int inLove;
    private UUID loveCause;
    private int huntCooldown;
    private int ticksSinceEaten;
    private int eatCooldown;
    private boolean addedHurtByReplacements;
    private boolean addedPlayWithItemsReplacement;

    protected DolphinEntityMixin(EntityType<? extends WaterAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void onAboutToAddGoal(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.targetSelector && priority == 1 && goal instanceof HurtByTargetGoal && !this.addedHurtByReplacements){
            goalSelector.addGoal(priority, new AptitudeHurtByTargetGoal<>(this, Guardian.class).setAlertOthers());
            this.addedHurtByReplacements = true;
        } else if(goalSelector == this.goalSelector && priority == 8 && !this.addedPlayWithItemsReplacement){
            goalSelector.addGoal(priority, new DevourerPlayWithItemsGoal<>(this, AptitudePredicates.ALLOWED_ITEMS, 100));
            this.addedPlayWithItemsReplacement = true;
        } else {
            goalSelector.addGoal(priority, goal);
        }
    }

    @Inject(at = @At("RETURN"), method = "defineSynchedData")
    private void addAgeableData(CallbackInfo ci){
        this.entityData.define(DATA_BABY_ID, false);
    }


    @Inject(at = @At("RETURN"), method = "addAdditionalSaveData")
    private void addSaveData(CompoundTag compoundNBT, CallbackInfo ci){
        this.addAnimalData(compoundNBT);
        this.addAgeableData(this, compoundNBT);
    }

    @Inject(at = @At("RETURN"), method = "readAdditionalSaveData")
    private void readSaveData(CompoundTag compoundNBT, CallbackInfo ci){
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
    private void finalize(ServerLevelAccessor serverWorld, DifficultyInstance difficultyInstance, MobSpawnType spawnReason, SpawnGroupData livingEntityData, CompoundTag compoundNBT, CallbackInfoReturnable<SpawnGroupData> cir){
        SpawnGroupData ageableData = this.finalizeAgeableSpawn(livingEntityData);
        this.setHuntCooldown(getHuntInterval());
        cir.setReturnValue(super.finalizeSpawn(serverWorld, difficultyInstance, spawnReason, ageableData, compoundNBT));
    }

    @Inject(at = @At("HEAD"), method = "mobInteract", cancellable = true)
    private void handleAnimalInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir){
        InteractionResult animalInteractResult = this.animalInteract(this, player, hand);
        if(animalInteractResult.consumesAction()){
            this.setPersistenceRequired();
            cir.setReturnValue(animalInteractResult);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "pickUpItem", cancellable = true)
    private void customPickUpItem(ItemEntity itemEntity, CallbackInfo ci){
        this.handlePickUpItem(this, itemEntity);
        ci.cancel();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataParameter) {
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
    public void killed(ServerLevel serverWorld, LivingEntity killedEntity) {
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
    public <T extends Mob & IAgeable> T getBreedOffspring(ServerLevel serverWorld, T ageable) {
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
    public <T extends Mob & IDevourer> boolean canEat(T devourer, ItemStack stack) {
        return stack.getItem().isEdible()
                && this.getEatCooldown() <= 0
                && this.isFood(stack);
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
    public void usePlayerItem(Player player, ItemStack stack) {
        if(this.isFood(stack)){
            this.playSound(this.getEatingSound(stack), 1.0F, 1.0F);
            if(stack.isEdible()) {
                this.heal(stack.getItem().getFoodProperties().getNutrition());
                AptitudeHelper.addEatEffect(stack, this.level, this);
            }
        }
        if (!player.abilities.instabuild) {
            stack.shrink(1);
        }
    }

    @Override
    public <T extends Mob & IAnimal> boolean canAcceptFood(T animal, ItemStack stack) {
        return this.isHungry(this);
    }

    @Override
    public SoundEvent getPlayingSound() {
        return SoundEvents.DOLPHIN_PLAY;
    }
}
