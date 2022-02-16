package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.CustomServerAiStepEvent;
import com.infamous.aptitude.common.MobPickUpLootEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Shadow protected abstract void customServerAiStep();

    @Shadow public abstract boolean wantsToPickUp(ItemStack p_21546_);

    @Inject(at = @At("RETURN"), method = "createMobAttributes")
    private static void addAttackDamage(CallbackInfoReturnable<AttributeSupplier.Builder> cir){
        cir.getReturnValue().add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @Redirect(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/ItemEntity;hasPickUpDelay()Z"
            ),
            method = "aiStep")
    private boolean fireMobPickUpLootEvent(ItemEntity itemEntity){
        if(!itemEntity.hasPickUpDelay()){ // no pick up delay
            // return whether or not was cancelled, will be negated
            return MinecraftForge.EVENT_BUS.post(new MobPickUpLootEvent((Mob) (Object) this, itemEntity));
        }
        return true; // has pick up delay
    }

    @Redirect(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;customServerAiStep()V"))
    private void fireCustomServerAiStepEvent(Mob mob){
        if(!MinecraftForge.EVENT_BUS.post(new CustomServerAiStepEvent(mob))){ // not cancelled
            this.customServerAiStep();
        }
    }
}
