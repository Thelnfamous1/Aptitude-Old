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
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoxEntity.class)
public abstract class FoxEntityMixin extends AnimalEntity {

    @Shadow public abstract boolean isFood(ItemStack p_70877_1_);

    protected FoxEntityMixin(EntityType<? extends AnimalEntity> p_i48568_1_, World p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Inject(at = @At("HEAD"), method = "usePlayerItem")
    private void healWithFood(PlayerEntity player, ItemStack stack, CallbackInfo ci){
        if(this.isFood(stack) && stack.isEdible()){
            this.heal(stack.getItem().getFoodProperties().getNutrition());
            AptitudeHelper.addEatEffect(stack, this.level, this);
        }
    }

    @Inject(at = @At("RETURN"), method = "isFood", cancellable = true)
    private void checkFoodTag(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(AptitudePredicates.FOX_FOOD_PREDICATE.test(stack));
    }
}
