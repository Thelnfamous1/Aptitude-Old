package com.infamous.aptitude.mixin;

import com.google.common.collect.Sets;
import com.infamous.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    @Shadow public abstract void setAggressive(boolean p_21562_);

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;customServerAiStep()V", shift = At.Shift.AFTER), method = "serverAiStep")
    private void handleBrainStep(CallbackInfo ci){
        if(this.getType() == EntityType.PIG){
            String name = this.getTypeName().getString().toLowerCase(Locale.ROOT);
            this.level.getProfiler().push("aptitude." + name + "Brain");

            Aptitude.LOGGER.info("Checking memory values for {} before ticking brain:", this);
            this.getBrainCast().getMemories().forEach((mt, mv) -> Aptitude.LOGGER.info("Brain has value {} for memory {}", mv, mt));

            this.getBrainCast().tick((ServerLevel) this.level, this.cast());

            Aptitude.LOGGER.info("Checking memory values for {} after ticking brain:", this);
            this.getBrainCast().getMemories().forEach((mt, mv) -> Aptitude.LOGGER.info("Brain has value {} for memory {}", mv, mt));
            Aptitude.LOGGER.info("Memory check complete for {}", this);

            this.level.getProfiler().pop();
            this.updateActivity();
        }
    }

    private <E extends Mob> void updateActivity() {
        Brain<E> brain = this.getBrainCast();
        ResourceLocation etLocation = ForgeRegistries.ENTITIES.getKey(this.getType());
        List<Activity> rotatingActivities = Aptitude.brainManager.getRotatingActivities(etLocation);

        //brain.getActiveActivities().forEach(a -> Aptitude.LOGGER.info("Was running activity: {}", a));
        //brain.getRunningBehaviors().forEach(b -> Aptitude.LOGGER.info("Running behavior: {}", b));

        brain.setActiveActivityToFirstValid(rotatingActivities);
        this.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));

        //brain.getActiveActivities().forEach(a -> Aptitude.LOGGER.info("Now running activity: {}", a));
    }

    private <E extends Mob> Brain<E> getBrainCast() {
        return (Brain<E>) this.getBrain();
    }

    private Mob cast() {
        return (Mob) (Object) this;
    }
}
