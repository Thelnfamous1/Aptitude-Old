package com.infamous.aptitude.common.behavior.functions;

import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.AptitudeRegistries;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.function.Function;

public class FunctionTypes {

    private static final DeferredRegister<FunctionType<?>> FUNCTION_TYPES = DeferredRegister.create(AptitudeRegistries.FUNCTION_TYPES, Aptitude.MOD_ID);

    public static final RegistryObject<FunctionType<Function<LivingEntity, Optional<? extends LivingEntity>>>> RETRIEVE_TARGET_FROM_MEMORY = register("retrieve_target_from_memory",
            jsonObject -> {
                return le -> {
                    Brain<?> brain = le.getBrain();
                    JsonObject addContextObj = GsonHelper.getAsJsonObject(jsonObject, "addContext");
                    MemoryModuleType<?> memoryType = BehaviorHelper.parseMemoryType(addContextObj, "type");
                    if(brain.hasMemoryValue(memoryType)){
                        return brain.getMemory(memoryType).map(LivingEntity.class::cast);
                    } else{
                        return Optional.empty();
                    }
                };
            });

    private static <U extends Function<?, ?>> RegistryObject<FunctionType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return FUNCTION_TYPES.register(name, () -> new FunctionType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        FUNCTION_TYPES.register(bus);
    }

}
