package com.infamous.aptitude.common.behavior.functions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FunctionTypes {

    private static final DeferredRegister<FunctionType<?>> FUNCTION_TYPES = DeferredRegister.create((Class<FunctionType<?>>)(Class)FunctionType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<FunctionType<?>>> FUNCTION_TYPE_REGISTRY = FUNCTION_TYPES.makeRegistry("function_types", () ->
            new RegistryBuilder<FunctionType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("FunctionType Added: " + obj.getRegistryName().toString() + " ")
            ).setDefaultKey(new ResourceLocation(Aptitude.MOD_ID, "self"))
    );

    public static final RegistryObject<FunctionType<Function<LivingEntity, LivingEntity>>> SELF = register("self",
            jsonObject -> {
                return livingEntity -> livingEntity;
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
                MemoryModuleType<? extends LivingEntity> memoryType = BehaviorHelper.parseMemoryType(addContextObj, "type");
                Predicate<LivingEntity> filterPredicate = addContextObj.has("filter_predicate") ?
                        BehaviorHelper.parsePredicate(addContextObj, "filter_predicate", "type") :
                        le -> true;

                return le -> {
                    Brain<?> brain = le.getBrain();
                    return brain.getMemory(memoryType).filter(filterPredicate);


                };
            });

    public static final RegistryObject<FunctionType<Function<LivingEntity, Optional<? extends LivingEntity>>>> RETRIEVE_ENTITY_FROM_UUID_MEMORY = register("retrieve_entity_from_uuid_memory",
            jsonObject -> {
                JsonObject addContextObj = GsonHelper.getAsJsonObject(jsonObject, "addContext");
                MemoryModuleType<UUID> memoryType = BehaviorHelper.parseMemoryType(addContextObj, "type");
                Predicate<LivingEntity> filterPredicate = addContextObj.has("filter_predicate") ?
                        BehaviorHelper.parsePredicate(addContextObj, "filter_predicate", "type") :
                        le -> true;

                return le -> BehaviorUtils.getLivingEntityFromUUIDMemory(le, memoryType).filter(filterPredicate);
            });

    public static final RegistryObject<FunctionType<Function<LivingEntity, Optional<? extends LivingEntity>>>> RETRIEVE_CLOSEST_VISIBLE_ENTITY = register("retrieve_closest_visible_entity",
            jsonObject -> {
                JsonObject addContextObj = GsonHelper.getAsJsonObject(jsonObject, "addContext");
                MemoryModuleType<NearestVisibleLivingEntities> memoryType = BehaviorHelper.parseMemoryType(addContextObj, "type");
                Predicate<LivingEntity> filterPredicate = addContextObj.has("filter_predicate") ?
                        BehaviorHelper.parsePredicate(addContextObj, "filter_predicate", "type") :
                        le -> true;

                return le -> {
                    Brain<?> brain = le.getBrain();
                    return brain.getMemory(memoryType).orElse(NearestVisibleLivingEntities.empty()).findClosest(filterPredicate);
                };
            });

    public static final RegistryObject<FunctionType<Function<LivingEntity, Optional<? extends LivingEntity>>>> RETRIEVE_FIRST_VALID_ENTITY = register("retrieve_first_valid_entity",
            jsonObject -> {

            });



    private static <U extends Function<?, ?>> RegistryObject<FunctionType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return FUNCTION_TYPES.register(name, () -> new FunctionType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        FUNCTION_TYPES.register(bus);
    }

    public static FunctionType<?> getFunctionType(ResourceLocation ftLocation) {
        FunctionType<?> value = FUNCTION_TYPE_REGISTRY.get().getValue(ftLocation);
        Aptitude.LOGGER.info("Attempting to get function type {}, got {}", ftLocation, value.getRegistryName());
        return value;
    }
}
