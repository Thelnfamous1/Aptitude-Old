package com.infamous.aptitude.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(Behavior.class)
public interface BehaviorAccessor<E extends LivingEntity> {

    @Accessor
    Map<MemoryModuleType<?>, MemoryStatus> getEntryCondition();

    @Invoker
    boolean callCanStillUse(ServerLevel serverLevel, E mob, long gameTime);

    @Invoker
    boolean callCheckExtraStartConditions(ServerLevel serverLevel, E mob);

    @Invoker
    void callStart(ServerLevel serverLevel, E mob, long gameTime);

    @Invoker
    void callTick(ServerLevel serverLevel, E mob, long gameTime);

    @Invoker
    void callStop(ServerLevel serverLevel, E mob, long gameTime);
}
