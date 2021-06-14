package com.infamous.aptitude.mixin;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.entity.IAptitudeHorse;
import com.infamous.aptitude.common.entity.IRearable;
import com.infamous.aptitude.common.util.AptitudeResources;
import com.infamous.aptitude.server.goal.AptitudeHurtByTargetGoal;
import com.infamous.aptitude.server.goal.AptitudePanicGoal;
import com.infamous.aptitude.server.goal.horse.RearingAttackGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseEntityMixin extends AnimalEntity implements IAptitudeHorse, IRearable {

    private static final Ingredient FOOD_ITEMS_USING_TAG = Ingredient.of(AptitudeResources.HORSES_EAT);

    @Shadow @Nullable public abstract UUID getOwnerUUID();

    private static final int ANGRY_SOUND_INTERVAL = 40;
    private int angrySoundCooldown;

    protected AbstractHorseEntityMixin(EntityType<? extends AnimalEntity> p_i48568_1_, World p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void onAboutToAddPanicGoal(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.goalSelector && goal instanceof PanicGoal){
            this.goalSelector.addGoal(priority, new RearingAttackGoal<>(this, 1.25D, true));
            this.goalSelector.addGoal(priority, new AptitudePanicGoal(this, 1.2D));
        } else{
            goalSelector.addGoal(priority, goal);
        }
    }

    @Inject(at = @At("RETURN"), method = "registerGoals")
    private void addTargetGoals(CallbackInfo ci){
        this.targetSelector.addGoal(1, new AptitudeHurtByTargetGoal(this));
    }

    @Inject(at = @At("RETURN"), method = "isImmobile", cancellable = true)
    private void checkAggressiveForImmobility(CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(
                super.isImmobile() && this.isVehicle() && this.isSaddled()
                        || this.isEating()
                        || (this.isStanding() && !this.isAggressive()));
    }

    @Inject(at = @At("RETURN"), method = "canEatGrass", cancellable = true)
    protected void checkAggressiveForCanEat(CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(!this.isAggressive());
    }

    @Inject(at = @At("RETURN"), method = "isFood", cancellable = true)
    protected void checkFoodTag(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(FOOD_ITEMS_USING_TAG.test(stack));
    }

    @Inject(at = @At("HEAD"), method = "handleEating", cancellable = true)
    protected void handleEatingForFoodTag(PlayerEntity player, ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        boolean handled = false;
        float healAmount = 0.0F;
        int ageBoost = 0;
        int temperBoost = 0;
        Item item = stack.getItem();
        if (item.is(AptitudeResources.WHEAT_EQUIVALENTS)) {
            healAmount = 2.0F;
            ageBoost = 20;
            temperBoost = 3;
        } else if (item.is(AptitudeResources.SUGAR_EQUIVALENTS)) {
            healAmount = 1.0F;
            ageBoost = 30;
            temperBoost = 3;
        } else if (item.is(AptitudeResources.HAY_EQUIVALENTS)) {
            healAmount = 20.0F;
            ageBoost = 180;
        } else if (item.is(AptitudeResources.APPLE_EQUIVALENTS)) {
            healAmount = 3.0F;
            ageBoost = 60;
            temperBoost = 3;
        } else if (item.is(AptitudeResources.GOLDEN_CARROT_EQUIVALENTS)) {
            healAmount = 4.0F;
            ageBoost = 60;
            temperBoost = 5;
        } else if (item.is(AptitudeResources.GOLDEN_APPLE_EQUIVALENTS)) {
            healAmount = 10.0F;
            ageBoost = 240;
            temperBoost = 10;
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
        Aptitude.LOGGER.info("Handled eating for {}", this);
    }

    @Shadow
    public abstract int getMaxTemper();

    @Shadow
    public abstract int getTemper();

    @Shadow
    public abstract int modifyTemper(int j);

    @Shadow
    protected abstract void eating();

    @Shadow
    public abstract boolean isEating();

    @Shadow
    public abstract boolean isSaddled();

    @Override
    public Team getTeam() {
        if (this.isTamed()) {
            LivingEntity owner = this.getOwner();
            if (owner != null) {
                return owner.getTeam();
            }
        }

        return super.getTeam();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (this.isTamed()) {
            LivingEntity owner = this.getOwner();
            if (entity == owner) {
                return true;
            }

            if (owner != null) {
                return owner.isAlliedTo(entity);
            }
        }

        return super.isAlliedTo(entity);
    }

    @Override
    @Nullable
    public LivingEntity getOwner() {
        UUID uuid = this.getOwnerUUID();
        return uuid == null ? null : this.level.getPlayerByUUID(uuid);
    }

    @Shadow
    public abstract boolean isTamed();

    @Shadow protected abstract void stand();

    @Shadow public abstract void setStanding(boolean p_110219_1_);

    @Shadow public abstract boolean isStanding();

    @Shadow public abstract boolean isFood(ItemStack p_70877_1_);

    @Override
    public void playAngrySound() {
        if(this.getAngrySoundCooldown() <= 0){
            SoundEvent angrySound = this.getAngrySoundRaw();
            if (angrySound != null) {
                this.playSound(angrySound, this.getSoundVolume(), this.getVoicePitch());
            }
            this.setAngrySoundCooldown(ANGRY_SOUND_INTERVAL);
        }
    }

    @Override
    public SoundEvent getAngrySoundRaw() {
        return null;
    }

    @Override
    public void startRearing() {
        if(!this.isStanding()){
            this.stand();
        }
    }

    @Override
    public void stopRearing() {
        this.setStanding(false);
    }

    @Override
    public int getAngrySoundCooldown() {
        return this.angrySoundCooldown;
    }

    @Override
    public void setAngrySoundCooldown(int angrySoundCooldown) {
        this.angrySoundCooldown = angrySoundCooldown;
    }
}
