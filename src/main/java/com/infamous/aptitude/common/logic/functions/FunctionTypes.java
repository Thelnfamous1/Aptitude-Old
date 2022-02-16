package com.infamous.aptitude.common.logic.functions;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import com.infamous.aptitude.common.behavior.util.FunctionHelper;
import com.infamous.aptitude.common.behavior.util.PredicateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FunctionTypes {

    private static final DeferredRegister<FunctionType<?>> FUNCTION_TYPES = DeferredRegister.create((Class<FunctionType<?>>)(Class)FunctionType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<FunctionType<?>>> FUNCTION_TYPE_REGISTRY = FUNCTION_TYPES.makeRegistry("function_types", () ->
            new RegistryBuilder<FunctionType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("FunctionType Added: " + obj.getRegistryName().toString() + " ")
            )
    );

    public static final RegistryObject<FunctionType<Function<?, Optional<?>>>> EMPTY_OPTIONAL = register("empty_optional",
            jsonObject -> {
                return o -> Optional.empty();
            });

    public static final RegistryObject<FunctionType<Function<? extends Entity, Vec3>>> ENTITY_POSITION_VECTOR = register("entity_position_vector",
            jsonObject -> {
                return e -> e.position();
            });

    public static final RegistryObject<FunctionType<Function<BlockPos, Vec3>>> BLOCK_POSITION_VECTOR = register("block_position_vector",
            jsonObject -> {
                return blockPos -> Vec3.atBottomCenterOf(blockPos);
            });

    public static final RegistryObject<FunctionType<Function<LivingEntity, Optional<? extends LivingEntity>>>> RETRIEVE_ENTITY_FROM_MEMORY = register("retrieve_entity_from_memory",
            jsonObject -> {
                JsonObject addContextObj = GsonHelper.getAsJsonObject(jsonObject, "addContext");
                MemoryModuleType<? extends LivingEntity> memoryType = BehaviorHelper.parseMemoryType(addContextObj, "memory_type");
                Predicate<LivingEntity> filterPredicate = PredicateHelper.parsePredicateOrDefault(addContextObj, "filter_predicate", "type", le -> true);
                BiPredicate<LivingEntity, LivingEntity> filterBiPredicate = PredicateHelper.parseBiPredicateOrDefault(addContextObj, "filter_bipredicate", "type", (le1, le2) -> true);

                return le -> {
                    Brain<?> brain = le.getBrain();
                    if(!brain.checkMemory(memoryType, MemoryStatus.REGISTERED)){
                        return Optional.empty();
                    }
                    return brain.getMemory(memoryType).filter(filterPredicate).filter(e -> filterBiPredicate.test(le, e));


                };
            });

    public static final RegistryObject<FunctionType<Function<LivingEntity, Optional<? extends LivingEntity>>>> RETRIEVE_ENTITY_FROM_UUID_MEMORY = register("retrieve_entity_from_uuid_memory",
            jsonObject -> {
                JsonObject addContextObj = GsonHelper.getAsJsonObject(jsonObject, "addContext");
                MemoryModuleType<UUID> memoryType = BehaviorHelper.parseMemoryType(addContextObj, "memory_type");

                Predicate<LivingEntity> filterPredicate = PredicateHelper.parsePredicateOrDefault(addContextObj, "filter_predicate", "type", le -> true);
                BiPredicate<LivingEntity, LivingEntity> filterBiPredicate = PredicateHelper.parseBiPredicateOrDefault(addContextObj, "filter_bipredicate", "type", (le1, le2) -> true);


                return le -> BehaviorUtils.getLivingEntityFromUUIDMemory(le, memoryType).filter(filterPredicate).filter(e -> filterBiPredicate.test(le, e));
            });

    public static final RegistryObject<FunctionType<Function<LivingEntity, Optional<? extends LivingEntity>>>> RETRIEVE_ENTITY_FROM_VISIBLE_ENTITIES_MEMORY = register("retrieve_entity_from_visible_entities_memory",
            jsonObject -> {
                JsonObject addContextObj = GsonHelper.getAsJsonObject(jsonObject, "addContext");
                MemoryModuleType<NearestVisibleLivingEntities> memoryType = BehaviorHelper.parseMemoryType(addContextObj, "memory_type");
                Predicate<LivingEntity> filterPredicate = PredicateHelper.parsePredicateOrDefault(addContextObj, "filter_predicate", "type", le -> true);
                BiPredicate<LivingEntity, LivingEntity> filterBiPredicate = PredicateHelper.parseBiPredicateOrDefault(addContextObj, "filter_bipredicate", "type", (le1, le2) -> true);


                return le -> {
                    Brain<?> brain = le.getBrain();
                    Predicate<LivingEntity> jointFilterPredicate = e -> filterPredicate.test(e) && filterBiPredicate.test(le, e);
                    if(!brain.checkMemory(memoryType, MemoryStatus.REGISTERED)){
                        return Optional.empty();
                    }
                    return brain.getMemory(memoryType).orElse(NearestVisibleLivingEntities.empty()).findClosest(jointFilterPredicate);
                };
            });

    public static final RegistryObject<FunctionType<Function<LivingEntity, Optional<? extends LivingEntity>>>> RETRIEVE_FIRST_VALID_ENTITY = register("retrieve_first_valid_entity",
            jsonObject -> {
                List<Function<LivingEntity, Optional<? extends LivingEntity>>> functions = FunctionHelper.parseFunctions(jsonObject, "functions", "type");
                return livingEntity -> {
                    for(Function<LivingEntity, Optional<? extends LivingEntity>> function : functions){
                        Optional<? extends LivingEntity> retrievedEntity = function.apply(livingEntity);
                        if(retrievedEntity.isPresent()) return retrievedEntity;
                    }
                    return Optional.empty();
                };
            });

    public static final RegistryObject<FunctionType<Function<Object, Object>>> PREDICATED_FUNCTION = register("predicated_function",
            jsonObject -> {
                Predicate<Object> predicate = PredicateHelper.parsePredicate(jsonObject, "predicate", "type");
                Function<Object, Object> function = FunctionHelper.parseFunction(jsonObject, "function", "type");
                Function<Object, Object> defaultFunction = FunctionHelper.parseFunction(jsonObject, "default", "type");
                return o -> {
                    if(predicate.test(o)){
                        return function.apply(o);
                    }
                    return defaultFunction.apply(o);
                };
            });

    public static final RegistryObject<FunctionType<Function<LivingEntity, Float>>> GET_FLOAT = register("get_float",
            jsonObject -> {
                float value = GsonHelper.getAsFloat(jsonObject, "value", 0);
                return livingEntity -> {
                    return value;
                };
            });

    public static final RegistryObject<FunctionType<Function<LivingEntity, Long>>> ENTITY_GET_LONG_FROM_UNIFORM_INT = register("entity_get_long_from_uniform_int",
            jsonObject -> {
                UniformInt uniformInt = BehaviorHelper.parseUniformInt(jsonObject, "uniform_int");
                return livingEntity -> {
                    return (long) uniformInt.sample(livingEntity.level.random);
                };
            });

    public static final RegistryObject<FunctionType<Function<LivingEntity, GlobalPos>>> ENTITY_GET_GLOBAL_POSITION_FROM_SELF = register("entity_get_global_position_from_self",
            jsonObject -> {
                return livingEntity -> {
                    return GlobalPos.of(livingEntity.level.dimension(), livingEntity.blockPosition());
                };
            });

    public static final RegistryObject<FunctionType<Function<LivingEntity, List<LivingEntity>>>> ENTITY_RETRIEVE_LIST_OF_ENTITIES_FROM_MEMORY = register("entity_retrieve_list_of_entities_from_memory",
            jsonObject -> {
                MemoryModuleType<List<LivingEntity>> memoryType = BehaviorHelper.parseMemoryType(jsonObject, "memory_type");
                return le -> {
                    Brain<?> brain = le.getBrain();
                    if(!brain.checkMemory(memoryType, MemoryStatus.REGISTERED)){
                        return ImmutableList.of();
                    }
                    return brain.getMemory(memoryType).orElse(ImmutableList.of());
                };
            });

    public static final RegistryObject<FunctionType<Function<?, ?>>> CUSTOM_FUNCTION = register("custom_function",
            jsonObject -> {
                String locationString = GsonHelper.getAsString(jsonObject, "location");
                ResourceLocation location = new ResourceLocation(locationString);
                return Aptitude.customLogicManager.getFunction(location);
            });

    public static final RegistryObject<FunctionType<Function<LivingEntity, Integer>>> ENTITY_GET_INTEGER_FROM_UNIFORM_INT = register("entity_get_integer_from_uniform_int",
            jsonObject -> {
                UniformInt uniformInt = BehaviorHelper.parseUniformInt(jsonObject, "uniform_int");
                return livingEntity -> {
                    return uniformInt.sample(livingEntity.level.random);
                };
            });

    private static <U extends Function<?, ?>> RegistryObject<FunctionType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return FUNCTION_TYPES.register(name, () -> new FunctionType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        FUNCTION_TYPES.register(bus);
    }

    public static FunctionType<?> getFunctionType(ResourceLocation ftLocation) {
        FunctionType<?> value = FUNCTION_TYPE_REGISTRY.get().getValue(ftLocation);
        if(value == null) Aptitude.LOGGER.error("Failed to get FunctionType {}", ftLocation);
        return value;
    }
}
