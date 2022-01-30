package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.CustomServerAiStepEvent;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Shadow protected abstract void customServerAiStep();

    @Redirect(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;customServerAiStep()V"))
    private void fireCustomServerAiStepEvent(Mob mob){
        if(!MinecraftForge.EVENT_BUS.post(new CustomServerAiStepEvent(mob))){
            this.customServerAiStep();
        }
    }
}
