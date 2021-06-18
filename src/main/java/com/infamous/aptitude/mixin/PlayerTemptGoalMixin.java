package com.infamous.aptitude.mixin;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.passive.TurtleEntity$PlayerTemptGoal")
public abstract class PlayerTemptGoalMixin extends Goal {

    @Final
    @Shadow
    private TurtleEntity turtle;

    @Inject(at = @At("RETURN"), method = "shouldFollowItem", cancellable = true)
    protected void shouldFollowItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.turtle.isFood(stack));
    }
}
