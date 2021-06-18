package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.util.AptitudeHelper;
import com.infamous.aptitude.common.util.AptitudePredicates;
import com.infamous.aptitude.server.goal.misc.AptitudeTemptGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StriderEntity.class)
public abstract class StriderEntityMixin extends AnimalEntity {

    private boolean addedTemptReplacements;
    private AptitudeTemptGoal customTemptGoal;

    protected StriderEntityMixin(EntityType<? extends AnimalEntity> p_i48568_1_, World p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void onAboutToAddGoal(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.goalSelector && priority == 3 && goal instanceof TemptGoal && !this.addedTemptReplacements){
            this.customTemptGoal = new AptitudeTemptGoal(this, 1.4D, AptitudePredicates.STRIDER_TEMPT_PREDICATE, false);
            goalSelector.addGoal(priority, customTemptGoal);
            this.addedTemptReplacements = true;
        } else {
            goalSelector.addGoal(priority, goal);
        }
    }

    @Inject(at = @At("RETURN"), method = "isBeingTempted", cancellable = true)
    private void isBeingCustomTempted(CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(this.customTemptGoal != null && this.customTemptGoal.isRunning());
    }

    @Inject(at = @At("RETURN"), method = "isFood", cancellable = true)
    private void checkFoodTag(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(AptitudePredicates.STRIDER_FOOD_PREDICATE.test(stack));
    }

    @Override
    public void usePlayerItem(PlayerEntity player, ItemStack stack) {
        if(this.isFood(stack)){
            this.playSound(this.getEatingSound(stack), 1.0F, 1.0F);
            if(stack.isEdible()) {
                this.heal(stack.getItem().getFoodProperties().getNutrition());
                AptitudeHelper.addEatEffect(stack, this.level, this);
            }
        }
        super.usePlayerItem(player, stack);
    }
}
