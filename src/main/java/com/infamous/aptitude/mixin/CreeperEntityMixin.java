package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.util.AptitudePredicates;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Creeper.class)
public abstract class CreeperEntityMixin extends Monster {
    private int addedAvoidReplacementsCounter;

    protected CreeperEntityMixin(EntityType<? extends Monster> p_i48553_1_, Level p_i48553_2_) {
        super(p_i48553_1_, p_i48553_2_);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void replaceGoals(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.goalSelector && priority == 3 && goal instanceof AvoidEntityGoal && this.addedAvoidReplacementsCounter < 2){
            if(this.addedAvoidReplacementsCounter < 1){
                goalSelector.addGoal(priority, new AvoidEntityGoal<>(this, LivingEntity.class, 6.0F, 1.0D, 1.2D,
                        AptitudePredicates.CREEPERS_AVOID_PREDICATE));
            }
            this.addedAvoidReplacementsCounter++;
        } else{
            goalSelector.addGoal(priority, goal);
        }
    }
}
