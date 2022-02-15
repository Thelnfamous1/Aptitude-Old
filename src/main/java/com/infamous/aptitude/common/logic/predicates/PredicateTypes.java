package com.infamous.aptitude.common.logic.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import com.infamous.aptitude.common.behavior.util.PredicateHelper;
import com.infamous.aptitude.common.util.ReflectionHelper;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.ToolAction;
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

public class PredicateTypes {

    private static final DeferredRegister<PredicateType<?>> PREDICATE_TYPES = DeferredRegister.create((Class<PredicateType<?>>)(Class)PredicateType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<PredicateType<?>>> PREDICATE_TYPE_REGISTRY = PREDICATE_TYPES.makeRegistry("predicate_types", () ->
            new RegistryBuilder<PredicateType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("PredicateType Added: " + obj.getRegistryName().toString() + " ")
            )
    );

    public static final RegistryObject<PredicateType<Predicate<?>>> ALWAYS_TRUE = register("always_true",
            jsonObject -> {
                return o -> true;
            });

    public static final RegistryObject<PredicateType<Predicate<?>>> ALWAYS_FALSE = register("always_false",
            jsonObject -> {
                return o -> false;
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

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> MEMORY_VALUE_CHECK = register("memory_value_check",
            jsonObject -> {
                JsonObject addContextObj = GsonHelper.getAsJsonObject(jsonObject, "addContext");
                MemoryModuleType<Object> memoryType = BehaviorHelper.parseMemoryType(addContextObj, "memory_type");
                Predicate<Object> filterPredicate = PredicateHelper.parsePredicateOrDefault(addContextObj, "filter_predicate", "type", o -> true);
                BiPredicate<LivingEntity, Object> filterBiPredicate = PredicateHelper.parseBiPredicateOrDefault(addContextObj, "filter_bipredicate", "type", (le, o) -> true);
                return le -> {
                    Predicate<Object> jointFilterPredicate = e -> filterPredicate.test(e) && filterBiPredicate.test(le, e);
                    Brain<?> brain = le.getBrain();
                    return brain.hasMemoryValue(memoryType) && brain.getMemory(memoryType).filter(jointFilterPredicate).isPresent();
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

                        if(!brain.checkMemory(otherType, MemoryStatus.REGISTERED)){
                            return false;
                        }

                        if(!brain.checkMemory(sameType, MemoryStatus.REGISTERED)){
                            return false;
                        }
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
                List<Predicate<Object>> predicates = PredicateHelper.parsePredicates(jsonObject, "predicates", "type");

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
                List<Predicate<Object>> predicates = PredicateHelper.parsePredicates(jsonObject, "predicates", "type");

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
                Predicate<Object> predicate = PredicateHelper.parsePredicate(jsonObject, "predicate", "type");

                return predicate.negate();
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_IS_HOLDING_CROSSBOW = register("entity_is_holding_crossbow",
            jsonObject -> {
                return livingEntity -> livingEntity.isHolding(is -> is.getItem() instanceof CrossbowItem);
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_IS_HOLDING_BOW = register("entity_is_holding_bow",
            jsonObject -> {
                return livingEntity -> livingEntity.isHolding(is -> is.getItem() instanceof BowItem);
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_IS_HOLDING_PROJECTILE_WEAPON = register("entity_is_holding_projectile_weapon",
            jsonObject -> {
                return livingEntity -> livingEntity.isHolding(is -> is.getItem() instanceof ProjectileWeaponItem);
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_HAS_EMPTY_SLOT = register("entity_has_empty_slot",
            jsonObject -> {
                EquipmentSlot equipmentSlot = BehaviorHelper.parseEquipmentSlot(jsonObject, "slot");
                return livingEntity -> livingEntity.getItemBySlot(equipmentSlot).isEmpty();
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_ITEM_IN_SLOT_IS = register("entity_item_in_slot_is",
            jsonObject -> {
                EquipmentSlot equipmentSlot = BehaviorHelper.parseEquipmentSlot(jsonObject, "slot");
                Ingredient ingredient = Ingredient.fromJson(jsonObject.get("item_in_slot_is"));
                return livingEntity -> ingredient.test(livingEntity.getItemBySlot(equipmentSlot));
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_ITEM_IN_SLOT_CAN_PERFORM_ACTION = register("entity_item_in_slot_can_perform_action",
            jsonObject -> {
                EquipmentSlot equipmentSlot = BehaviorHelper.parseEquipmentSlot(jsonObject, "slot");
                ToolAction toolAction = BehaviorHelper.parseToolAction(jsonObject);
                return livingEntity -> livingEntity.getItemBySlot(equipmentSlot).canPerformAction(toolAction);
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_IS_PASSENGER = register("entity_is_passenger",
            jsonObject -> {
                return Entity::isPassenger;
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_RIDING_VALID_MOUNT = register("entity_riding_valid_mount",
            jsonObject -> {
                Predicate<LivingEntity> riderPredicate = PredicateHelper.parsePredicate(jsonObject, "rider_predicate", "type");
                Predicate<LivingEntity> mountPredicate = PredicateHelper.parsePredicate(jsonObject, "mount_predicate", "type");


                return rider -> {
                    if (!riderPredicate.test(rider)) {
                        return false;
                    } else {
                        Entity vehicle = rider.getVehicle();
                        return vehicle instanceof LivingEntity mount && mountPredicate.test(mount);
                    }
                };
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_CAN_HUNT = register("entity_can_hunt",
            jsonObject -> {
                return livingEntity -> !(livingEntity instanceof AbstractPiglin piglin) || ReflectionHelper.reflectPiglinCanHunt(piglin);
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_CAN_BE_HUNTED = register("entity_can_be_hunted",
            jsonObject -> {
                return livingEntity -> !(livingEntity instanceof Hoglin hoglin) || hoglin.canBeHunted();
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_IS_CELEBRATING = register("entity_is_celebrating",
            jsonObject -> {
                return livingEntity -> livingEntity instanceof Piglin piglin && piglin.isDancing() || livingEntity instanceof Raider raider && raider.isCelebrating();
            });


    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_RANDOM_FLOAT_CHANCE = register("entity_random_float_chance",
            jsonObject -> {
                float randomChance = GsonHelper.getAsFloat(jsonObject, "random_chance");

                return livingEntity -> {
                    return livingEntity.level.getRandom().nextFloat() < randomChance;
                };
            });

    public static final RegistryObject<PredicateType<Predicate<ItemStack>>> ITEM_IS = register("item_is",
            jsonObject -> {

                return Ingredient.fromJson(jsonObject.get("is"));
            });


    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_BOOLEAN_GAME_RULE_CHECK = register("entity_boolean_game_rule_check",
            jsonObject -> {
                String gameRuleId = GsonHelper.getAsString(jsonObject, "game_rule_id");
                String gameRuleCategoryStr = GsonHelper.getAsString(jsonObject, "game_rule_category");
                GameRules.Category gameRuleCategory = GameRules.Category.valueOf(gameRuleCategoryStr.toUpperCase(Locale.ROOT));
                GameRules.Key<GameRules.BooleanValue> booleanValueKey = new GameRules.Key<>(gameRuleId, gameRuleCategory);

                return livingEntity -> {
                    return livingEntity.level.getGameRules().getBoolean(booleanValueKey);
                };
            });

    public static final RegistryObject<PredicateType<Predicate<LivingEntity>>> ENTITY_ACTIVE_ACTIVITY_CHECK = register("entity_active_activity_check",
            jsonObject -> {
                Activity activity = BehaviorHelper.parseActivity(jsonObject, "activity");
                return le -> {
                    return le.getBrain().isActive(activity);
                };
            });

    public static final RegistryObject<PredicateType<Predicate<?>>> CUSTOM_PREDICATE = register("custom_predicate",
            jsonObject -> {
                String locationString = GsonHelper.getAsString(jsonObject, "location");
                ResourceLocation location = new ResourceLocation(locationString);
                return Aptitude.customLogicManager.getPredicate(location);
            });

    private static <U extends Predicate<?>> RegistryObject<PredicateType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return PREDICATE_TYPES.register(name, () -> new PredicateType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        PREDICATE_TYPES.register(bus);
    }

    public static PredicateType<?> getPredicateType(ResourceLocation ptLocation) {
        PredicateType<?> value = PREDICATE_TYPE_REGISTRY.get().getValue(ptLocation);
        if(value == null) Aptitude.LOGGER.error("Failed to get PredicateType {}", ptLocation);
        return value;
    }
}
