package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.util.AptitudePredicates;
import com.infamous.aptitude.server.goal.misc.AptitudeTemptGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CowEntity.class)
public abstract class CowEntityMixin extends AnimalEntity {

    private boolean addedTemptReplacements;

    protected CowEntityMixin(EntityType<? extends AnimalEntity> p_i48568_1_, World p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void onAboutToAddGoal(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.goalSelector && priority == 3 && goal instanceof TemptGoal && !this.addedTemptReplacements){
            goalSelector.addGoal(priority, new AptitudeTemptGoal(this, 1.25D, AptitudePredicates.COW_FOOD_PREDICATE, false));
            this.addedTemptReplacements = true;
        } else {
            goalSelector.addGoal(priority, goal);
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return AptitudePredicates.COW_FOOD_PREDICATE.test(stack);
    }
}
