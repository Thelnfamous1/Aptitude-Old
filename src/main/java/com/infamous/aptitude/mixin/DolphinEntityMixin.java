package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.entity.IAgeable;
import com.infamous.aptitude.common.entity.IAnimal;
import com.infamous.aptitude.common.util.AptitudeResources;
import com.infamous.aptitude.server.goal.AptitudeHurtByTargetGoal;
import com.infamous.aptitude.server.goal.AptitudeTemptGoal;
import com.infamous.aptitude.server.goal.animal.AptitudeBreedGoal;
import com.infamous.aptitude.server.goal.animal.AptitudeFollowParentGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
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
public class DolphinEntityMixin extends WaterMobEntity implements IAnimal {
    private static final Ingredient FOOD_ITEMS = Ingredient.of(AptitudeResources.DOLPHINS_EAT);
    private static final DataParameter<Boolean> DATA_BABY_ID = EntityDataManager.defineId(DolphinEntity.class, DataSerializers.BOOLEAN);
    protected int age;
    protected int forcedAge;
    protected int forcedAgeTimer;
    private int inLove;
    private UUID loveCause;

    protected DolphinEntityMixin(EntityType<? extends WaterMobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void onAboutToAddGoal(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.targetSelector && goal instanceof HurtByTargetGoal){
            this.targetSelector.addGoal(priority, new AptitudeHurtByTargetGoal(this, GuardianEntity.class).setAlertOthers());
        } else{
            goalSelector.addGoal(priority, goal);
        }
    }

    @Inject(at = @At("HEAD"), method = "registerGoals")
    private void addAnimalGoals(CallbackInfo ci){
        /*
        Dolphins move around really fast, so we have to quadruple the parent/partner search distances to reduce search failures
         */
        this.goalSelector.addGoal(1, new AptitudeBreedGoal<>(this, 1.25D, 120, 8.0D, 3.0D));
        this.goalSelector.addGoal(3, new AptitudeTemptGoal(this, 1.25D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(4, new AptitudeFollowParentGoal<>(this, 1.25D, 8.0D, 3.0D));
    }

    @Inject(at = @At("RETURN"), method = "defineSynchedData")
    private void addAgeableData(CallbackInfo ci){
        this.entityData.define(DATA_BABY_ID, false);
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> dataParameter) {
        if (DATA_BABY_ID.equals(dataParameter)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(dataParameter);
    }

    @Inject(at = @At("RETURN"), method = "addAdditionalSaveData")
    private void addSaveData(CompoundNBT compoundNBT, CallbackInfo ci){
        this.addAnimalData(compoundNBT);
        this.addAgeableData(compoundNBT);
    }

    @Inject(at = @At("RETURN"), method = "readAdditionalSaveData")
    private void readSaveData(CompoundNBT compoundNBT, CallbackInfo ci){
        this.readAnimalData(compoundNBT);
        this.readAgeableData(compoundNBT);
    }

    @Inject(at = @At("HEAD"), method = "handleEntityEvent")
    private void handleBreed(byte eventId, CallbackInfo ci){
        if(eventId == IAnimal.LOVE_ID){
            this.handleBreedEvent(this);
        }
    }

    @Inject(at = @At("RETURN"), method = "finalizeSpawn", cancellable = true)
    private void finalize(IServerWorld serverWorld, DifficultyInstance difficultyInstance, SpawnReason spawnReason, ILivingEntityData livingEntityData, CompoundNBT compoundNBT, CallbackInfoReturnable<ILivingEntityData> cir){
        ILivingEntityData ageableData = this.finalizeAgeableSpawn(livingEntityData);
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
    protected void customServerAiStep() {
        this.animalCustomServerAiStep();
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.ageableAiStep(this);
        this.animalAiStep(this);
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
        return this.getAge() < IAgeable.ADULT_AGE;
    }

    @Override
    public void setBabyData(boolean isBaby) {
        this.entityData.set(DATA_BABY_ID, isBaby);
    }

    @Override
    public double getMyRidingOffset() {
        return IAnimal.RIDING_OFFSET;
    }

    @Nullable
    @Override
    public <T extends MobEntity & IAgeable> T getBreedOffspring(ServerWorld serverWorld, T ageable) {
        T dolphin = (T) EntityType.DOLPHIN.create(serverWorld);
        if(dolphin != null){
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
    public int getAge() {
        if (this.level.isClientSide) {
            return this.entityData.get(DATA_BABY_ID) ? -1 : 1;
        } else {
            return this.getAgeRaw();
        }
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
        return FOOD_ITEMS.test(stack);
    }
}
