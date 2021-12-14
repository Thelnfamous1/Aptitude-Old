package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.util.AptitudeHelper;
import com.infamous.aptitude.common.util.AptitudePredicates;
import com.infamous.aptitude.server.goal.misc.AptitudeTemptGoal;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Chicken.class)
public abstract class ChickenEntityMixin extends Animal {

    private boolean addedTemptReplacements;

    protected ChickenEntityMixin(EntityType<? extends Animal> p_i48568_1_, Level p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void onAboutToAddGoal(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.goalSelector && priority == 3 && goal instanceof TemptGoal && !this.addedTemptReplacements){
            goalSelector.addGoal(priority, new AptitudeTemptGoal(this, 1.0D, AptitudePredicates.CHICKEN_FOOD_PREDICATE, false));
            this.addedTemptReplacements = true;
        } else {
            goalSelector.addGoal(priority, goal);
        }
    }

    @Inject(at = @At("RETURN"), method = "isFood", cancellable = true)
    private void checkFoodTag(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(AptitudePredicates.CHICKEN_FOOD_PREDICATE.test(stack));
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
}
