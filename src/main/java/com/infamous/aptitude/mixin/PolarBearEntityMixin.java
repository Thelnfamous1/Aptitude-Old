package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.entity.IAnimal;
import com.infamous.aptitude.common.entity.IDevourer;
import com.infamous.aptitude.common.entity.IPredator;
import com.infamous.aptitude.common.util.AptitudeHelper;
import com.infamous.aptitude.common.util.AptitudePredicates;
import com.infamous.aptitude.server.goal.target.HuntGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PolarBear.class)
public abstract class PolarBearEntityMixin extends Animal implements IPredator, IDevourer {

    private boolean addedRandomWalkingReplacements;
    private boolean addedNearestAttackableReplacements;

    @Shadow public abstract boolean isFood(ItemStack p_70877_1_);

    private int ticksSinceEaten;
    private int eatCooldown;
    private int huntCooldown;

    protected PolarBearEntityMixin(EntityType<? extends Animal> p_i48568_1_, Level p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Inject(at = @At("RETURN"), method = "isFood", cancellable = true)
    private void checkFoodTag(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(AptitudePredicates.POLAR_BEAR_FOOD_PREDICATE.test(stack));
    }

    @Inject(at = @At("RETURN"), method = "finalizeSpawn")
    private void onFinalizedSpawn(ServerLevelAccessor p_213386_1_, DifficultyInstance p_213386_2_, MobSpawnType p_213386_3_, SpawnGroupData p_213386_4_, CompoundTag p_213386_5_, CallbackInfoReturnable<SpawnGroupData> cir){
        this.setHuntCooldown(this.getHuntInterval());
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void onAboutToAddGoal(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.goalSelector && priority == 5 && goal instanceof RandomStrollGoal && !this.addedRandomWalkingReplacements){
            goalSelector.addGoal(priority, new WaterAvoidingRandomStrollGoal(this, 1.0D));
            this.addedRandomWalkingReplacements = true;
        } else if(goalSelector == this.targetSelector && priority == 4 && goal instanceof NearestAttackableTargetGoal && !this.addedNearestAttackableReplacements){
            goalSelector.addGoal(priority, new HuntGoal<>(this, LivingEntity.class, 10, true, true, AptitudePredicates.POLAR_BEAR_PREY_PREDICATE));
            this.addedNearestAttackableReplacements = true;
        }
        else {
            goalSelector.addGoal(priority, goal);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.devourerAiStep(this);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.isFood(itemstack) && this.isHungry(this)) {
            return super.mobInteract(player, hand);
        } else{
            return InteractionResult.PASS;
        }
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        this.handlePickUpItem(this, itemEntity);
    }

    @Override
    public boolean canTakeItem(ItemStack stack) {
        EquipmentSlot slotForItem = Mob.getEquipmentSlotForItem(stack);
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
    public void killed(ServerLevel serverWorld, LivingEntity killedEntity) {
        super.killed(serverWorld, killedEntity);
        this.onHuntedPrey(killedEntity);
    }

    @Override
    public void usePlayerItem(Player player, InteractionHand hand, ItemStack stack) {
        if(this.isFood(stack)){
            this.playSound(this.getEatingSound(stack), 1.0F, 1.0F);
            if(stack.isEdible()) {
                this.heal(stack.getItem().getFoodProperties().getNutrition());
                AptitudeHelper.addEatEffect(stack, this.level, this);
            }
        }
        super.usePlayerItem(player, hand, stack);
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
    public <T extends Mob & IDevourer> boolean canEat(T devourer, ItemStack stack) {
        return stack.getItem().isEdible()
                && this.isHungry(devourer)
                && this.getEatCooldown() <= 0
                && this.isFood(stack);
    }

    @Override
    public void onFinishedEating() {
        if(!this.level.isClientSide && this.getAge() == IAnimal.ADULT_AGE && this.canFallInLove()){
            this.setInLove(null);
        }

        if (this.isBaby()) {
            this.ageUp((int)((float)(-this.getAge() / 20) * 0.1F), true);
            this.level.broadcastEntityEvent(this, (byte) FINISHED_EATING_ID);
        }
    }

    @Override
    public boolean isPrey(LivingEntity living) {
        return AptitudePredicates.POLAR_BEAR_PREY_PREDICATE.test(living);
    }
}
