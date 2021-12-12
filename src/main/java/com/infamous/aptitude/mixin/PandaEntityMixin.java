package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.entity.IAnimal;
import com.infamous.aptitude.common.entity.IDevourer;
import com.infamous.aptitude.common.util.AptitudeHelper;
import com.infamous.aptitude.common.util.AptitudePredicates;
import com.infamous.aptitude.server.goal.misc.AptitudeTemptGoal;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Panda.class)
public abstract class PandaEntityMixin extends Animal {

    @Final
    @Shadow
    @Mutable
    private static Predicate<ItemEntity> PANDA_ITEMS;

    static {
        PANDA_ITEMS = AptitudePredicates.PANDA_ITEMS;
    }

    private boolean addedTemptReplacements;

    protected PandaEntityMixin(EntityType<? extends Animal> p_i48568_1_, Level p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V"),
            method = "registerGoals")
    private void onAboutToAddGoal(GoalSelector goalSelector, int priority, Goal goal){
        if(goalSelector == this.goalSelector && priority == 4 && goal instanceof TemptGoal && !this.addedTemptReplacements){
            goalSelector.addGoal(priority, new AptitudeTemptGoal(this, 1.0D, AptitudePredicates.PANDA_FOOD_PREDICATE, false));
            this.addedTemptReplacements = true;
        } else {
            goalSelector.addGoal(priority, goal);
        }
    }

    @Inject(at = @At("RETURN"), method = "isFood", cancellable = true)
    private void checkFoodTag(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(AptitudePredicates.PANDA_FOOD_PREDICATE.test(stack));
    }

    @Inject(at = @At("RETURN"), method = "isFoodOrCake", cancellable = true)
    private void checkFoodOrCakeTag(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(AptitudePredicates.PANDA_FOOD_OR_CAKE_PREDICATE.test(stack));
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/PandaEntity;setItemSlot(Lnet/minecraft/inventory/EquipmentSlotType;Lnet/minecraft/item/ItemStack;)V"),
            method = "handleEating")
    private void setInLoveModeAfterEating(CallbackInfo ci){
        this.onFinishedEating();
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

    @Override
    public void handleEntityEvent(byte eventId) {
        if(eventId == IDevourer.FINISHED_EATING_ID){
            this.onFinishedEating();
        } else{
            super.handleEntityEvent(eventId);
        }
    }

    @Override
    public void usePlayerItem(Player player, ItemStack stack) {
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
