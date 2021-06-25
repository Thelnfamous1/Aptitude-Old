package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.util.AptitudePredicates;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends MonsterEntity {
    private int addedAvoidReplacementsCounter;

    protected CreeperEntityMixin(EntityType<? extends MonsterEntity> p_i48553_1_, World p_i48553_2_) {
        super(p_i48553_1_, p_i48553_2_);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V"),
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
