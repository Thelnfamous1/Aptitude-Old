package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.util.AptitudePredicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(WolfEntity.class)
public abstract class WolfEntityMixin extends TameableEntity {

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

    protected WolfEntityMixin(EntityType<? extends TameableEntity> p_i48574_1_, World p_i48574_2_) {
        super(p_i48574_1_, p_i48574_2_);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void replaceGoals(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.goalSelector && priority == 3 && goal instanceof AvoidEntityGoal && !this.addedAvoidReplacements){
            goalSelector.addGoal(priority, new AvoidEntityGoal<>(this, LivingEntity.class, 24.0F, 1.5D, 1.5D,
                    living -> AptitudePredicates.WOLVES_AVOID_PREDICATE.test(living) && !this.isTame()));
            this.addedAvoidReplacements = true;
        } else if(goalSelector == this.targetSelector && priority == 5 && goal instanceof NonTamedTargetGoal && !this.addedNonTamedReplacements){
            goalSelector.addGoal(priority, new NonTamedTargetGoal<>(this, LivingEntity.class, false,
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
