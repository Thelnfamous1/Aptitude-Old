package com.infamous.aptitude.common.behavior.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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

    public static final RegistryObject<FunctionType<Function<? extends Entity, Vec3>>> ENTITY_POSITION_VECTOR = register("entity_position_vector",
            jsonObject -> {
                return e -> {
                    return e.position();
                };
            });

    public static final RegistryObject<FunctionType<Function<BlockPos, Vec3>>> BLOCK_POSITION_VECTOR = register("block_position_vector",
            jsonObject -> {
                return blockPos -> {
                    return Vec3.atBottomCenterOf(blockPos);
                };
            });



    public static final RegistryObject<FunctionType<Function<LivingEntity, Optional<? extends LivingEntity>>>> RETRIEVE_TARGET_FROM_MEMORY_CHECK_IGNORED = register("retrieve_target_from_memory_check_ignored",
            jsonObject -> {
                return le -> {
                    Brain<?> brain = le.getBrain();
                    JsonObject addContextObj = GsonHelper.getAsJsonObject(jsonObject, "addContext");
                    MemoryModuleType<?> memoryType = BehaviorHelper.parseMemoryType(addContextObj, "type");

                    Set<EntityType<?>> ignoredTypes = new HashSet<>();
                    if(addContextObj.has("ignored_types")){
                        if(addContextObj.get("ignored_types").isJsonArray()){
                            JsonArray ignoredTypesArr = GsonHelper.getAsJsonArray(addContextObj, "ignored_types");
                            ignoredTypesArr.forEach(jsonElement -> {
                                if(jsonElement.isJsonObject()){
                                    JsonObject elementObj = jsonElement.getAsJsonObject();
                                    Tags.IOptionalNamedTag<EntityType<?>> tag = BehaviorHelper.parseEntityTypeTag(elementObj, "tag");
                                    ignoredTypes.addAll(tag.getValues());
                                } else{
                                    EntityType<?> entityType = BehaviorHelper.parseEntityType(jsonElement);
                                    ignoredTypes.add(entityType);
                                }
                            });
                        }
                    }

                    if(brain.hasMemoryValue(memoryType)){
                        return brain.getMemory(memoryType).map(LivingEntity.class::cast).filter(target -> !ignoredTypes.contains(target.getType()));
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

    public static FunctionType<?> getFunctionType(ResourceLocation ftLocation) {
        FunctionType<?> value = FUNCTION_TYPE_REGISTRY.get().getValue(ftLocation);
        Aptitude.LOGGER.info("Attempting to get function type {}, got {}", ftLocation, value.getRegistryName());
        return value;
    }
}
