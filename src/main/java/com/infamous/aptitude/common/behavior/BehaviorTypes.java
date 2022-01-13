package com.infamous.aptitude.common.behavior;

import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class BehaviorTypes {

    private static final DeferredRegister<BehaviorType<?>> BEHAVIOR_TYPES = DeferredRegister.create(AptitudeRegistries.BEHAVIOR_TYPES, Aptitude.MOD_ID);

    public static final RegistryObject<BehaviorType<Behavior<?>>> START_ATTACKING = register("start_attacking", (jsonObject) -> {
        Predicate<Mob> canAttackPredicate = mob -> true;
        Function<Mob, Optional<? extends LivingEntity>> targetFinderFunction = mob -> mob.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
        return new StartAttacking<>(canAttackPredicate, targetFinderFunction);
    });

    private static <U extends Behavior<?>> RegistryObject<BehaviorType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return BEHAVIOR_TYPES.register(name, () -> new BehaviorType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        BEHAVIOR_TYPES.register(bus);
    }
}
