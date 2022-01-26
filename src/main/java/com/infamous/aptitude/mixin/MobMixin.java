package com.infamous.aptitude.mixin;

import com.google.common.collect.ImmutableList;
import com.infamous.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    @Shadow public abstract void setAggressive(boolean p_21562_);

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;customServerAiStep()V", shift = At.Shift.AFTER), method = "serverAiStep")
    private void handleBrainStep(CallbackInfo ci){
        if(this.getType() == EntityType.PIG){
            String name = this.getTypeName().getString();
            this.level.getProfiler().push("aptitude." + name + "Brain");
            this.getBrainCast().tick((ServerLevel) this.level, this.cast());
            this.level.getProfiler().pop();
            this.updateActivity();
        }
    }

    private <E extends Mob> void updateActivity() {
        Brain<E> brain = this.getBrainCast();
        ResourceLocation etLocation = ForgeRegistries.ENTITIES.getKey(this.getType());
        List<Activity> activities = Aptitude.brainManager.getPrioritizedBehaviorsByActivity(etLocation).keySet().stream().toList();
        brain.setActiveActivityToFirstValid(activities);
        this.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    private <E extends Mob> Brain<E> getBrainCast() {
        return (Brain<E>) this.getBrain();
    }

    private Mob cast() {
        return (Mob) (Object) this;
    }
}
