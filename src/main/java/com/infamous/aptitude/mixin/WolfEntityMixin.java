package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.util.AptitudePredicates;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(Wolf.class)
public abstract class WolfEntityMixin extends TamableAnimal {

    private boolean addedAvoidReplacements;

    @Final
    @Shadow
    @Mutable
    public static Predicate<LivingEntity> PREY_SELECTOR;

    static {
        PREY_SELECTOR = AptitudePredicates.WOLF_PREY_PREDICATE;
    }

    private boolean addedNearestAttackableReplacements;
    private boolean addedNonTamedReplacements;

    protected WolfEntityMixin(EntityType<? extends TamableAnimal> p_i48574_1_, Level p_i48574_2_) {
        super(p_i48574_1_, p_i48574_2_);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void replaceGoals(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.goalSelector && priority == 3 && goal instanceof AvoidEntityGoal && !this.addedAvoidReplacements){
            goalSelector.addGoal(priority, new AvoidEntityGoal<>(this, LivingEntity.class, 24.0F, 1.5D, 1.5D,
                    living -> AptitudePredicates.WOLVES_AVOID_PREDICATE.test(living) && !this.isTame()));
            this.addedAvoidReplacements = true;
        } else if(goalSelector == this.targetSelector && priority == 5 && goal instanceof NonTameRandomTargetGoal && !this.addedNonTamedReplacements){
            goalSelector.addGoal(priority, new NonTameRandomTargetGoal<>(this, LivingEntity.class, false,
                    AptitudePredicates.WOLF_PREY_PREDICATE));
            this.addedNonTamedReplacements = true;
        } else if(goalSelector == this.targetSelector && priority == 7 && goal instanceof NearestAttackableTargetGoal && !this.addedNearestAttackableReplacements){
            goalSelector.addGoal(priority, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, false, false,
                    AptitudePredicates.WOLF_DEFEND_PREDICATE));
            this.addedNearestAttackableReplacements = true;
        } else{
            goalSelector.addGoal(priority, goal);
        }
    }
}
