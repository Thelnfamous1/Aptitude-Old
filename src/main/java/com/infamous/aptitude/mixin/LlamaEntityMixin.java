package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.entity.ICanSpit;
import com.infamous.aptitude.common.entity.ISwitchCombatTask;
import com.infamous.aptitude.common.util.AptitudeHelper;
import com.infamous.aptitude.common.util.AptitudePredicates;
import com.infamous.aptitude.common.util.AptitudeResources;
import com.infamous.aptitude.server.goal.attack.SwitchableRangedAttackGoal;
import com.infamous.aptitude.server.goal.attack.SwitchableRearingAttackGoal;
import com.infamous.aptitude.server.goal.misc.AptitudePanicGoal;
import com.infamous.aptitude.server.goal.target.AptitudeDefendTargetGoal;
import com.infamous.aptitude.server.goal.target.CanSpitHurtByTargetGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Llama.class)
public abstract class LlamaEntityMixin extends AbstractHorseEntityMixin implements ICanSpit, ISwitchCombatTask, RangedAttackMob {
    //private static final Ingredient LLAMA_FOOD_ITEMS = Ingredient.of(AptitudeResources.LLAMAS_EAT);
    private static final Predicate<ItemStack> LLAMA_FOOD_PREDICATE = stack -> stack.is(AptitudeResources.LLAMAS_EAT);

    private static final int SPIT_INTERVAL = 40;

    private boolean addedPanicReplacements;
    private boolean addedRangedAttackReplacements;
    private boolean addedHurtByReplacements;
    private boolean addedDefendReplacements;

    private int spitCooldown;

    @Shadow private boolean didSpit;

    protected LlamaEntityMixin(EntityType<? extends Animal> p_i48568_1_, Level p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void onAboutToAddGoal(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.goalSelector && priority == 3 && goal instanceof PanicGoal && !this.addedPanicReplacements){
            goalSelector.addGoal(priority, new SwitchableRearingAttackGoal<>(this, 1.25D, true));
            goalSelector.addGoal(priority, new AptitudePanicGoal(this, 1.2D));
            this.addedPanicReplacements = true;
        } else if(goalSelector == this.goalSelector && priority == 3 && goal instanceof RangedAttackGoal && !this.addedRangedAttackReplacements){
            goalSelector.addGoal(priority, new SwitchableRangedAttackGoal<>(this, 1.25D, SPIT_INTERVAL, 20.0F));
            this.addedRangedAttackReplacements = true;
        } else if(goalSelector == this.targetSelector && priority == 1 && goal instanceof HurtByTargetGoal && !this.addedHurtByReplacements){
            goalSelector.addGoal(priority, new CanSpitHurtByTargetGoal<>(this));
            this.addedHurtByReplacements = true;
        } else if(goalSelector == this.targetSelector && priority == 2 && goal instanceof NearestAttackableTargetGoal && !this.addedDefendReplacements){
            goalSelector.addGoal(priority, new AptitudeDefendTargetGoal<>(this, LivingEntity.class, 16, false, true, AptitudePredicates.LLAMA_DEFEND_PREDICATE).setFollowDistanceFactor(0.25F));
            this.addedDefendReplacements = true;
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
        cir.setReturnValue(LLAMA_FOOD_PREDICATE.test(stack));
    }

    /**
     * @author Thelnfamous1
     * @reason Replacing hardcoded food checks with tags
     */
    @Overwrite
    protected boolean handleEating(Player player, ItemStack stack) {
        int ageBoost = 0;
        int temperBoost = 0;
        float healAmount = 0.0F;
        boolean handled = false;
        if (stack.is(AptitudeResources.WHEAT_EQUIVALENTS)) {
            ageBoost = 10;
            temperBoost = 3;
            healAmount = 2.0F;
        } else if (stack.is(AptitudeResources.HAY_EQUIVALENTS)) {
            ageBoost = 90;
            temperBoost = 6;
            healAmount = 10.0F;
        }

        if(stack.is(AptitudeResources.LLAMAS_BREED_WITH)){
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

        AptitudeHelper.addEatEffect(stack, this.level, this);

        return handled;
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
    public int getSpitCooldown() {
        return this.spitCooldown;
    }

    @Override
    public void setSpitCooldown(int spitCooldown) {
        this.spitCooldown = spitCooldown;
    }

    @Shadow protected abstract void setDidSpit(boolean p_190714_1_);
}
