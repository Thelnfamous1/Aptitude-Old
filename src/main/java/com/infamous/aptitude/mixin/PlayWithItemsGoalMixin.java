package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.entity.IDevourer;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.passive.DolphinEntity$PlayWithItemsGoal")
public abstract class PlayWithItemsGoalMixin<T extends MobEntity & IDevourer> extends Goal {

    @SuppressWarnings("ShadowTarget")
    @Shadow
    private DolphinEntity this$0;

    @Shadow
    private int cooldown;

    @SuppressWarnings("unchecked")
    @Inject(at = @At("RETURN"), method = "canUse", cancellable = true)
    private void checkHoldingFood(CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue() && this.this$0 instanceof IDevourer){
            IDevourer eatsFood = (IDevourer) this.this$0;
            ItemStack itemBySlot = this.this$0.getItemBySlot(eatsFood.getSlotForFood());
            boolean canEatFood = eatsFood.canEat(((T) this.this$0), itemBySlot);
            cir.setReturnValue(!canEatFood);
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    private void dontDropFoodOnTick(CallbackInfo cir){
        if(this.this$0 instanceof IDevourer){
            IDevourer eatsFood = (IDevourer) this.this$0;
            ItemStack itemBySlot = this.this$0.getItemBySlot(eatsFood.getSlotForFood());
            boolean canEatFood = eatsFood.canEat(((T) this.this$0), itemBySlot);
            if(canEatFood){
                cir.cancel();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(at = @At("HEAD"), method = "stop", cancellable = true)
    private void dontDropFoodWhenStopped(CallbackInfo ci){
        if(this.this$0 instanceof IDevourer){
            IDevourer eatsFood = (IDevourer) this.this$0;
            ItemStack itemBySlot = this.this$0.getItemBySlot(eatsFood.getSlotForFood());
            boolean canEatFood = eatsFood.canEat(((T) this.this$0), itemBySlot);

            if(canEatFood){
                this.cooldown = this.this$0.tickCount + this.this$0.getRandom().nextInt(100);
                ci.cancel();
            }
        }
    }
}
