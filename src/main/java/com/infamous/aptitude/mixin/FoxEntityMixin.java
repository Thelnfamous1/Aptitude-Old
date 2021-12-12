package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.entity.IAnimal;
import com.infamous.aptitude.common.entity.IDevourer;
import com.infamous.aptitude.common.util.AptitudeHelper;
import com.infamous.aptitude.common.util.AptitudePredicates;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@Mixin(Fox.class)
public abstract class FoxEntityMixin extends Animal {

    @Final
    @Shadow
    private static EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_0;

    @Final
    @Shadow
    private static EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_1;

    @Final
    @Shadow
    @Mutable
    private static Predicate<Entity> STALKABLE_PREY;

    static {
        STALKABLE_PREY = AptitudePredicates.FOXES_CAN_STALK;
    }

    private final NonNullList<UUID> trustedUUIDs = NonNullList.create();

    @Shadow
    private Goal landTargetGoal;
    @Shadow
    private Goal fishTargetGoal;

    private int addedAvoidReplacementsCounter;

    @Shadow public abstract boolean isFood(ItemStack p_70877_1_);

    @Shadow protected abstract boolean isDefending();

    protected FoxEntityMixin(EntityType<? extends Animal> p_i48568_1_, Level p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void replaceGoals(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.goalSelector && priority == 4 && goal instanceof AvoidEntityGoal && this.addedAvoidReplacementsCounter < 3){
            if(this.addedAvoidReplacementsCounter == 1){
                goalSelector.addGoal(priority, new AvoidEntityGoal<>(this, LivingEntity.class, 8.0F, 1.6D, 1.4D,
                        livingEntity -> AptitudePredicates.FOXES_AVOID_PREDICATE.test(livingEntity) && !this.isDefending()));
            }
            this.addedAvoidReplacementsCounter++;
        } else{
            goalSelector.addGoal(priority, goal);
        }
    }

    @Inject(at = @At("RETURN"), method = "registerGoals")
    private void setCustomTargetGoals(CallbackInfo ci){
        this.landTargetGoal = new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, false, false, AptitudePredicates.FOXES_HUNT_ON_LAND);
        this.fishTargetGoal = new NearestAttackableTargetGoal<>(this, LivingEntity.class, 20, false, false, AptitudePredicates.FOXES_HUNT_IN_WATER);
    }

    @Inject(at = @At("HEAD"), method = "usePlayerItem")
    private void healWithFood(Player player, ItemStack stack, CallbackInfo ci){
        if(this.isFood(stack) && stack.isEdible()){
            this.heal(stack.getItem().getFoodProperties().getNutrition());
            AptitudeHelper.addEatEffect(stack, this.level, this);
        }
    }

    @Inject(at = @At("RETURN"), method = "isFood", cancellable = true)
    private void checkFoodTag(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(AptitudePredicates.FOX_FOOD_PREDICATE.test(stack));
    }

    @Inject(at = @At("HEAD"), method = "getTrustedUUIDs", cancellable = true)
    private void getExtendedList(CallbackInfoReturnable<List<UUID>> cir){
        cir.setReturnValue(this.trustedUUIDs);
    }

    /**
     * @author Thelnfamous1
     * @reason Allows foxes to store more than 2 "trusted" uuids
     */
    @Overwrite
    private void addTrustedUUID(@Nullable UUID uuid){
        if (!this.entityData.get(DATA_TRUSTED_ID_0).isPresent()) {
            this.entityData.set(DATA_TRUSTED_ID_0, Optional.ofNullable(uuid));
        } else if(!this.entityData.get(DATA_TRUSTED_ID_1).isPresent()){
            this.entityData.set(DATA_TRUSTED_ID_1, Optional.ofNullable(uuid));
        }

        if(uuid != null){
            this.trustedUUIDs.add(uuid);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;finishUsingItem(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;"),
            method = "aiStep")
    private void finishEat(CallbackInfo ci){
        this.onFinishedEating();
    }

    @Inject(at = @At("HEAD"), method = "handleEntityEvent", cancellable = true)
    private void handleFinishedEating(byte eventId, CallbackInfo ci){
        if(eventId == IDevourer.FINISHED_EATING_ID){
            this.onFinishedEating();
            ci.cancel();
        }
    }

    private void onFinishedEating() {
        if(!this.level.isClientSide && this.getAge() == IAnimal.ADULT_AGE && this.canFallInLove()){
            this.setInLove(null);
        }

        if (this.isBaby()) {
            this.ageUp((int)((float)(-this.getAge() / 20) * 0.1F), true);
            this.level.broadcastEntityEvent(this, (byte) IDevourer.FINISHED_EATING_ID);
        }
    }
}
