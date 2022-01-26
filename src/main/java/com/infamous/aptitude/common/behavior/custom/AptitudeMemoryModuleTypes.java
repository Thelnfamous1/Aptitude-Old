package com.infamous.aptitude.common.behavior.custom;

import com.infamous.aptitude.Aptitude;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Optional;

public class AptitudeMemoryModuleTypes {

    private static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, Aptitude.MOD_ID);
    public static final RegistryObject<MemoryModuleType<List<Pig>>> NEAREST_VISIBLE_ADULT_PIGS = register("nearest_visible_adult_pigs");

    public static final RegistryObject<MemoryModuleType<Integer>> VISIBLE_ADULT_PIG_COUNT = register("visible_adult_pig_count");

    private static <U> RegistryObject<MemoryModuleType<U>> register(String name, Codec<U> codec) {
        return MEMORY_MODULE_TYPES.register(name, () -> new MemoryModuleType<>(Optional.of(codec)));
    }

    private static <U> RegistryObject<MemoryModuleType<U>> register(String name) {
        return MEMORY_MODULE_TYPES.register(name, () -> new MemoryModuleType<>(Optional.empty()));
    }

    public static void register(IEventBus bus){
        MEMORY_MODULE_TYPES.register(bus);
    }
}
