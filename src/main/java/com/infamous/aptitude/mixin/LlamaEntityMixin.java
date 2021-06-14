package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.entity.IAptitudeLlama;
import com.infamous.aptitude.common.entity.ISwitchCombatTask;
import com.infamous.aptitude.common.util.AptitudeResources;
import com.infamous.aptitude.server.goal.*;
import com.infamous.aptitude.server.goal.horse.LlamaHurtByTargetGoal;
import com.infamous.aptitude.server.goal.horse.SwitchableRearinglAttackGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LlamaEntity.class)
public abstract class LlamaEntityMixin extends AbstractHorseEntityMixin implements IAptitudeLlama, ISwitchCombatTask, IRangedAttackMob {
    //private static final Ingredient LLAMA_FOOD_ITEMS = Ingredient.of(AptitudeResources.LLAMAS_EAT);

    private static final int SPIT_INTERVAL = 40;

    @Shadow private boolean didSpit;

    protected LlamaEntityMixin(EntityType<? extends AnimalEntity> p_i48568_1_, World p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Shadow protected abstract void setDidSpit(boolean p_190714_1_);

    private int spitCooldown;

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void onAboutToAddGoal(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.goalSelector && goal instanceof PanicGoal){
            this.goalSelector.addGoal(priority, new SwitchableRearinglAttackGoal<>(this, 1.25D, true));
            this.goalSelector.addGoal(priority, new AptitudePanicGoal(this, 1.2D));
        } else if(goalSelector == this.goalSelector && goal instanceof RangedAttackGoal){
            this.goalSelector.addGoal(priority, new SwitchableRangedAttackGoal<>(this, 1.25D, SPIT_INTERVAL, 20.0F));
        } else if(goalSelector == this.targetSelector && goal instanceof HurtByTargetGoal){
            this.targetSelector.addGoal(priority, new LlamaHurtByTargetGoal<>(this));
        }
        else{
            goalSelector.addGoal(priority, goal);
        }
    }

    @Inject(at = @At("RETURN"), method = "spit")
    private void onFinishedSpit(LivingEntity p_190713_1_, CallbackInfo ci){
        this.setLlamaDidSpit(true);
    }

    @Override
    protected void checkAggressiveForCanEat(CallbackInfoReturnable<Boolean> cir) {
        super.checkAggressiveForCanEat(cir);
    }

    @Override
    protected void checkFoodTag(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(stack.getItem().is(AptitudeResources.LLAMAS_EAT));
    }

    @Override
    protected void handleEatingForFoodTag(PlayerEntity player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        int ageBoost = 0;
        int temperBoost = 0;
        float healAmount = 0.0F;
        boolean handled = false;
        Item item = stack.getItem();
        if (item.is(AptitudeResources.WHEAT_EQUIVALENTS)) {
            ageBoost = 10;
            temperBoost = 3;
            healAmount = 2.0F;
        } else if (item.is(AptitudeResources.HAY_EQUIVALENTS)) {
            ageBoost = 90;
            temperBoost = 6;
            healAmount = 10.0F;
        }

        if(this.isFood(stack)){
            if (!this.level.isClientSide && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
                handled = true;
                this.setInLove(player);
            }
        }

        if (this.getHealth() < this.getMaxHealth() && healAmount > 0.0F) {
            this.heal(healAmount);
            handled = true;
        }

        if (this.isBaby() && ageBoost > 0) {
            this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
            if (!this.level.isClientSide) {
                this.ageUp(ageBoost);
            }

            handled = true;
        }

        if (temperBoost > 0 && (handled || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
            handled = true;
            if (!this.level.isClientSide) {
                this.modifyTemper(temperBoost);
            }
        }

        if (handled) {
            this.eating();
        }

        cir.setReturnValue(handled);
    }

    @Override
    public boolean isRanged() {
        boolean withinMeleeRange = false;
        if(this.getTarget() != null){
            double attackReachSq = this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F + this.getTarget().getBbWidth();
            withinMeleeRange =  this.distanceToSqr(this.getTarget()) < attackReachSq;
        }
        return !withinMeleeRange && this.getSpitCooldown() <= 0;
    }

    @Override
    public boolean getDidSpit() {
        return this.didSpit;
    }

    @Override
    public void setLlamaDidSpit(boolean didSpit) {
        this.setDidSpit(didSpit);
        if(didSpit){
            this.setSpitCooldown(SPIT_INTERVAL);
        }
    }

    @Override
    public void playAngrySound() {
        SoundEvent soundevent = this.getAngrySoundRaw();
        if (soundevent != null) {
            this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    @Override
    public SoundEvent getAngrySoundRaw() {
        return SoundEvents.LLAMA_ANGRY;
    }

    @Override
    public int getSpitCooldown() {
        return this.spitCooldown;
    }

    @Override
    public void setSpitCooldown(int spitCooldown) {
        this.spitCooldown = spitCooldown;
    }
}
