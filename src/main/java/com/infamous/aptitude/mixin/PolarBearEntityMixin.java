package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.entity.IAnimal;
import com.infamous.aptitude.common.entity.IDevourer;
import com.infamous.aptitude.common.entity.IPredator;
import com.infamous.aptitude.common.util.AptitudeResources;
import com.infamous.aptitude.server.goal.AptitudeHurtByTargetGoal;
import com.infamous.aptitude.server.goal.AptitudePlayWithItemsGoal;
import com.infamous.aptitude.server.goal.AptitudeTemptGoal;
import com.infamous.aptitude.server.goal.HuntGoal;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(PolarBearEntity.class)
public abstract class PolarBearEntityMixin extends AnimalEntity implements IPredator, IDevourer {

    @Shadow public abstract boolean isFood(ItemStack p_70877_1_);

    private static final Predicate<LivingEntity> PREY_PREDICATE = living -> living.getType().is(AptitudeResources.POLAR_BEARS_HUNT);
    private static final Predicate<ItemStack> FOOD_PREDICATE = stack -> stack.getItem().is(AptitudeResources.POLAR_BEARS_EAT);
    private static final Predicate<ItemEntity> ALLOWED_ITEMS = (itemEntity) -> !itemEntity.hasPickUpDelay() && itemEntity.isAlive();

    private int ticksSinceEaten;
    private int eatCooldown;
    private int huntCooldown;

    protected PolarBearEntityMixin(EntityType<? extends AnimalEntity> p_i48568_1_, World p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void onConstructed(EntityType<? extends PolarBearEntity> p_i50249_1_, World p_i50249_2_, CallbackInfo ci){
        this.setCanPickUpLoot(true);
    }

    @Inject(at = @At("RETURN"), method = "isFood", cancellable = true)
    private void checkFoodTag(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(FOOD_PREDICATE.test(stack));
    }

    @Inject(at = @At("RETURN"), method = "finalizeSpawn")
    private void onFinalizedSpawn(IServerWorld p_213386_1_, DifficultyInstance p_213386_2_, SpawnReason p_213386_3_, ILivingEntityData p_213386_4_, CompoundNBT p_213386_5_, CallbackInfoReturnable<ILivingEntityData> cir){
        this.setHuntCooldown(this.getHuntInterval());
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void onAboutToAddGoal(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.goalSelector && goal instanceof RandomWalkingGoal){
            this.goalSelector.addGoal(priority, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        } else {
            goalSelector.addGoal(priority, goal);
        }
    }

    @Inject(at = @At("HEAD"), method = "registerGoals")
    private void addCustomGoals(CallbackInfo ci){
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.25D));
        this.goalSelector.addGoal(3, new AptitudeTemptGoal(this, 1.25D, FOOD_PREDICATE, false));
        this.goalSelector.addGoal(8, new AptitudePlayWithItemsGoal<>(this, ALLOWED_ITEMS));
        this.targetSelector.addGoal(4, new HuntGoal<>(this, LivingEntity.class, 10, true, true, PREY_PREDICATE));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.eatsFoodAiStep(this);
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.isFood(itemstack) && this.isHungry(this)) {
            return super.mobInteract(player, hand);
        } else{
            return ActionResultType.PASS;
        }
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        this.handlePickUpItem(this, itemEntity);
    }

    @Override
    public boolean canTakeItem(ItemStack stack) {
        EquipmentSlotType slotForItem = MobEntity.getEquipmentSlotForItem(stack);
        if (!this.getItemBySlot(slotForItem).isEmpty()) {
            return false;
        } else {
            return slotForItem == this.getSlotForFood() && super.canTakeItem(stack);
        }
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
    public void handleEntityEvent(byte eventId) {
        if(eventId == FINISHED_EATING_ID){
            this.onFinishedEating();
        } else{
            super.handleEntityEvent(eventId);
        }
    }

    @Override
    public void killed(ServerWorld serverWorld, LivingEntity killedEntity) {
        super.killed(serverWorld, killedEntity);
        this.onHuntedPrey(killedEntity);
    }

    @Override
    public SoundEvent getSpitOutItemSound() {
        return SoundEvents.POLAR_BEAR_AMBIENT;
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
    public int getHuntCooldown() {
        return this.huntCooldown;
    }

    @Override
    public void setHuntCooldown(int huntCooldown) {
        this.huntCooldown = huntCooldown;
    }

    @Override
    public <T extends MobEntity & IDevourer> boolean canEat(T eatsFood, ItemStack stack) {
        return IDevourer.super.canEat(eatsFood, stack) && this.isFood(stack);
    }

    @Override
    public void onFinishedEating() {
        IDevourer.super.onFinishedEating();

        if(!this.level.isClientSide && this.getAge() == IAnimal.ADULT_AGE && this.canFallInLove()){
            this.setInLove(null);
        }

        if (this.isBaby()) {
            this.ageUp((int)((float)(-this.getAge() / 20) * 0.1F), true);
            this.level.broadcastEntityEvent(this, (byte) FINISHED_EATING_ID);
        }
    }

    @Override
    public void usePlayerItem(PlayerEntity player, ItemStack stack) {
        if(this.isFood(stack)){
            this.setEatCooldown(this.getEatInterval());
            this.playSound(this.getEatingSound(stack), 1.0F, 1.0F);
        }
        super.usePlayerItem(player, stack);
    }

    @Override
    public boolean isPrey(LivingEntity living) {
        return PREY_PREDICATE.test(living);
    }
}
