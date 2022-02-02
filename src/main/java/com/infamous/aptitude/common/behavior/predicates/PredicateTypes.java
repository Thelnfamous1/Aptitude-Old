package com.infamous.aptitude.common.behavior.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PredicateTypes {

    private static final DeferredRegister<PredicateType<?>> PREDICATE_TYPES = DeferredRegister.create((Class<PredicateType<?>>)(Class)PredicateType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<PredicateType<?>>> PREDICATE_TYPE_REGISTRY = PREDICATE_TYPES.makeRegistry("predicate_types", () ->
            new RegistryBuilder<PredicateType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("PredicateType Added: " + obj.getRegistryName().toString() + " ")
            ).setDefaultKey(new ResourceLocation(Aptitude.MOD_ID, "always_true"))
    );

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ALWAYS_TRUE = register("always_true",
            jsonObject -> {
                return livingEntity -> true;
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ALWAYS_FALSE = register("always_false",
            jsonObject -> {
                return livingEntity -> false;
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> MEMORY_STATUS_CHECK = register("memory_status_check",
            jsonObject -> {
                JsonElement addContextObj = jsonObject.get("addContext");
                Map<MemoryModuleType<?>, MemoryStatus> memoriesToStatus = BehaviorHelper.parseMemoriesToStatus(addContextObj);
                return le -> {
                    Brain<?> brain = le.getBrain();
                    for(Map.Entry<MemoryModuleType<?>, MemoryStatus> entry : memoriesToStatus.entrySet()){
                        if(!brain.checkMemory(entry.getKey(), entry.getValue())){
                            return false;
                        }
                    }
                    return true;
                };
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ACTIVE_NON_CORE_ACTIVITY_CHECK = register("active_non_core_activity_check",
            jsonObject -> {
                Activity activity = BehaviorHelper.parseActivity(jsonObject, "activity");
                return le -> {
                    return le.getBrain()
                            .getActiveNonCoreActivity()
                            .map(activeNonCoreActivity -> activity == activeNonCoreActivity)
                            .isPresent();
                };
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> OUTNUMBERED_ADULT = register("outnumbered_adult",
            jsonObject -> {
                JsonObject addContextObj = GsonHelper.getAsJsonObject(jsonObject, "addContext");
                MemoryModuleType<?> sameType = BehaviorHelper.parseMemoryType(addContextObj, "sameType");
                MemoryModuleType<?> otherType = BehaviorHelper.parseMemoryType(addContextObj, "otherType");
                return le -> {
                    if (le.isBaby()) {
                        return false;
                    } else {
                        Brain<?> brain = le.getBrain();
                        Optional<?> otherTypeMemory = brain.getMemory(otherType);
                        Optional<?> sameTypeMemory = brain.getMemory(sameType);
                        int otherTypeCount = otherTypeMemory.map(Integer.class::cast).orElse(0);
                        int sameTypeCount = sameTypeMemory.map(Integer.class::cast).orElse(0) + 1;
                        return otherTypeCount > sameTypeCount;
                    }
                };
            }
    );

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> IS_BABY = register("is_baby",
            jsonObject -> {
                return LivingEntity::isBaby;
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> IS_ADULT = register("is_adult",
            jsonObject -> {
                return le -> !le.isBaby();
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_IS_TYPE = register("entity_is_type",
            jsonObject -> {
                EntityTypePredicate entityTypePredicate = EntityTypePredicate.fromJson(jsonObject.get("entity_type_predicate"));
                return le -> entityTypePredicate.matches(le.getType());
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_IS_HOLDING = register("entity_is_holding",
            jsonObject -> {
                Ingredient isHolding = Ingredient.fromJson(jsonObject.get("is_holding"));
                return livingEntity -> livingEntity.isHolding(isHolding);
            });

    public static final RegistryObject<PredicateType<Predicate<?>>> ALL_OF_PREDICATE = register("all_of_predicate",
            jsonObject -> {
                List<Predicate<Object>> predicates = BehaviorHelper.getPredicates(jsonObject, "predicates", "type");

                return o -> {
                    for(Predicate<Object> predicate : predicates){
                        if(!predicate.test(o)){
                            return false;
                        }
                    }
                    return true;
                };
            });

    public static final RegistryObject<PredicateType<Predicate<?>>> ANY_OF_PREDICATE = register("any_of_predicate",
            jsonObject -> {
                List<Predicate<Object>> predicates = BehaviorHelper.getPredicates(jsonObject, "predicates", "type");

                return o -> {
                    for(Predicate<Object> predicate : predicates){
                        if(predicate.test(o)){
                            return true;
                        }
                    }
                    return false;
                };
            });

    public static final RegistryObject<PredicateType<Predicate<?>>> NEGATE_PREDICATE = register("negate_predicate",
            jsonObject -> {
                Predicate<Object> predicate = BehaviorHelper.parsePredicate(jsonObject, "predicate", "type");

                return predicate.negate();
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> RETRIEVED_ENTITY_IS_ATTACKABLE = register("retrieved_entity_is_attackable",
            jsonObject -> {
                Function<LivingEntity, Optional<? extends LivingEntity>> retrievalFunction = BehaviorHelper.parseFunction(jsonObject, "retrieval_function", "type");

                return livingEntity -> {
                    Optional<? extends LivingEntity> target = retrievalFunction.apply(livingEntity);
                    return target.map(le -> Sensor.isEntityAttackable(livingEntity, le)).isPresent();
                };
            });

    private static <U extends Predicate<?>> RegistryObject<PredicateType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return PREDICATE_TYPES.register(name, () -> new PredicateType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        PREDICATE_TYPES.register(bus);
    }

    public static PredicateType<?> getPredicateType(ResourceLocation ptLocation) {
        PredicateType<?> value = PREDICATE_TYPE_REGISTRY.get().getValue(ptLocation);
        Aptitude.LOGGER.info("Attempting to get predicate type {}, got {}", ptLocation, value.getRegistryName());
        return value;
    }
}
