package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.CustomServerAiStepEvent;
import com.infamous.aptitude.common.MobPickUpLootEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Shadow protected abstract void customServerAiStep();

    @Redirect(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Mob;wantsToPickUp(Lnet/minecraft/world/item/ItemStack;)Z"
            ),
            method = "aiStep")
    private boolean fireMobPickUpLootEvent(Mob mob, ItemStack stack){
        if(!MinecraftForge.EVENT_BUS.post(new MobPickUpLootEvent(mob, stack))){
            return false;
        }
        return mob.wantsToPickUp(stack);
    }

    @Redirect(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;customServerAiStep()V"))
    private void fireCustomServerAiStepEvent(Mob mob){
        if(!MinecraftForge.EVENT_BUS.post(new CustomServerAiStepEvent(mob))){
            this.customServerAiStep();
        }
    }
}
