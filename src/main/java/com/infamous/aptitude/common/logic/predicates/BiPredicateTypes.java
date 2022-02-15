package com.infamous.aptitude.common.logic.predicates;

import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.*;
import com.infamous.aptitude.common.util.ReflectionHelper;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.*;

public class BiPredicateTypes {

    private static final DeferredRegister<BiPredicateType<?>> BIPREDICATE_TYPES = DeferredRegister.create((Class<BiPredicateType<?>>)(Class)BiPredicateType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<BiPredicateType<?>>> BIPREDICATE_TYPE_REGISTRY = BIPREDICATE_TYPES.makeRegistry("bipredicate_types", () ->
            new RegistryBuilder<BiPredicateType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("BiPredicateType Added: " + obj.getRegistryName().toString() + " ")
            )
    );

    public static final RegistryObject<BiPredicateType<BiPredicate<?, ?>>> ALWAYS_TRUE = register("always_true",
            jsonObject -> {
                return (o1, o2) -> true;
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<?, ?>>> ALWAYS_FALSE = register("always_false",
            jsonObject -> {
                return (o1, o2) -> false;
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_IS_ATTACKABLE = register("entity_is_attackable",
            jsonObject -> {
                return Sensor::isEntityAttackable;
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_IS_ATTACKABLE_IGNORING_LINE_OF_SIGHT = register("entity_is_attackable_ignoring_line_of_sight",
            jsonObject -> {
                return Sensor::isEntityAttackableIgnoringLineOfSight;
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_IS_TARGETABLE = register("entity_is_targetable",
            jsonObject -> {
                return Sensor::isEntityTargetable;
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_WITHIN_DISTANCE = register("entity_within_distance",
            jsonObject -> {
                double distance = GsonHelper.getAsDouble(jsonObject, "distance", 0);

                return (le, le1) -> le.closerThan(le1, distance);
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<BlockPos, BlockPos>>> BLOCK_POSITION_WITHIN_DISTANCE = register("block_position_within_distance",
            jsonObject -> {
                double distance = GsonHelper.getAsDouble(jsonObject, "distance", 0);

                return (bp, bp1) -> bp.closerThan(bp1, distance);
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_IS_WITHIN_MELEE_ATTACK_RANGE = register("entity_is_within_melee_attack_range",
            jsonObject -> {
                Predicate<LivingEntity> meleeWeaponPredicate = PredicateHelper.parsePredicateOrDefault(jsonObject, "melee_weapon_predicate", "type",
                        le -> true);
                BiFunction<LivingEntity, LivingEntity, Double> getMeleeAttackRangeSqr = FunctionHelper.parseBiFunctionOrDefault(jsonObject, "get_melee_attack_range_sqr", "type",
                        MeleeAttackHelper::getDefaultMeleeAttackRangeSqr);

                return (attacker, target) -> {
                    if(meleeWeaponPredicate.test(attacker)){
                        double distanceToSqr = attacker.distanceToSqr(target.getX(), target.getY(), target.getZ());
                        return distanceToSqr <= getMeleeAttackRangeSqr.apply(attacker, target);
                    }
                    return false;
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_IS_WITHIN_PROJECTILE_ATTACK_RANGE = register("entity_is_within_projectile_attack_range",
            jsonObject -> {
                Predicate<LivingEntity> projectileWeaponPredicate = PredicateHelper.parsePredicateOrDefault(jsonObject, "projectile_weapon_predicate", "type",
                        le -> le.isHolding(stack -> stack.getItem() instanceof ProjectileWeaponItem));
                int defaultProjectileRange = GsonHelper.getAsInt(jsonObject, "default_projectile_range", 8);
                int projectileAttackRangeBuffer = GsonHelper.getAsInt(jsonObject, "projectile_attack_range_buffer", 0);
                int projectileRange = defaultProjectileRange - projectileAttackRangeBuffer;

                return (attacker, target) -> {
                    return projectileWeaponPredicate.test(attacker) && attacker.closerThan(target, (double)projectileRange);
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_IS_WITHIN_ATTACK_RANGE = register("entity_is_within_attack_range",
            jsonObject -> {
                BiPredicate<LivingEntity, LivingEntity> isWithinProjectileAttackRange = PredicateHelper.parseBiPredicateOrDefault(jsonObject, "is_within_projectile_attack_range", "type", (le, le1) -> false);
                BiPredicate<LivingEntity, LivingEntity> isWithinMeleeAttackRange = PredicateHelper.parseBiPredicateOrDefault(jsonObject, "is_within_melee_attack_range", "type", MeleeAttackHelper::isWithinMeleeAttackRangeDefault);

                return (le, le1) -> {
                    return isWithinProjectileAttackRange.test(le, le1) || isWithinMeleeAttackRange.test(le, le1);
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<?, ?>>> ALL_OF_BIPREDICATE = register("all_of_bipredicate",
            jsonObject -> {
                List<BiPredicate<Object, Object>> biPredicates = PredicateHelper.parseBiPredicates(jsonObject, "bipredicates", "type");

                return (o, o1) -> {
                    for(BiPredicate<Object, Object> biPredicate : biPredicates){
                        if(!biPredicate.test(o, o1)){
                            return false;
                        }
                    }
                    return true;
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<?, ?>>> ANY_OF_BIPREDICATE = register("any_of_bipredicate",
            jsonObject -> {
                List<BiPredicate<Object, Object>> biPredicates = PredicateHelper.parseBiPredicates(jsonObject, "bipredicates", "type");

                return (o, o1) -> {
                    for(BiPredicate<Object, Object> biPredicate : biPredicates){
                        if(biPredicate.test(o, o1)){
                            return true;
                        }
                    }
                    return false;
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<?, ?>>> NEGATE_BIPREDICATE = register("negate_bipredicate",
            jsonObject -> {
                BiPredicate<Object, Object> biPredicate = PredicateHelper.parseBiPredicate(jsonObject, "bipredicate", "type");

                return biPredicate.negate();
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_MATCHES_ENTITY_FROM_MEMORY = register("entity_matches_entity_from_memory",
            jsonObject -> {
                Function<LivingEntity, Optional<? extends LivingEntity>> retrievalFunction = FunctionHelper.parseFunction(jsonObject, "retrieval_function", "type");
                return (le, le1) -> {
                    Optional<? extends LivingEntity> result = retrievalFunction.apply(le);
                    return result.filter(e -> le1 == e).isPresent();
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, Entity>>> ENTITY_WANTS_TO_STOP_RIDING_MOUNT = register("entity_wants_to_stop_riding_mount",
            jsonObject -> {
                boolean canRideAdults = GsonHelper.getAsBoolean(jsonObject, "can_ride_adults", true);
                boolean ignoreHurtRider = GsonHelper.getAsBoolean(jsonObject, "ignore_hurt_rider", false);
                boolean ignoreHurtMount = GsonHelper.getAsBoolean(jsonObject, "ignore_hurt_mount", false);
                boolean canRideSameType = GsonHelper.getAsBoolean(jsonObject, "can_ride_same_type", false);

                return (rider, vehicle) -> {
                    if (vehicle instanceof LivingEntity mount) {
                        boolean ridingAdult = !mount.isBaby() && !canRideAdults;
                        boolean mountDead = !mount.isAlive();
                        boolean riderHurt = rider.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY) && !ignoreHurtRider;
                        boolean mountHurt = mount.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY) && !ignoreHurtMount;
                        boolean notRidingInTower = mount.getType() == rider.getType() && mount.getVehicle() == null && !canRideSameType;
                        return ridingAdult || mountDead || riderHurt || mountHurt || notRidingInTower;
                    } else {
                        return false;
                    }
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_TARGET_IS_TYPE = register("entity_target_is_type",
            jsonObject -> {
                EntityTypePredicate entityTypePredicate = EntityTypePredicate.fromJson(jsonObject.get("entity_type_predicate"));
                return (attacker, target) -> {
                    return entityTypePredicate.matches(target.getType());
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, ?>>> ENTITY_GAME_TIME_SEEDED_RANDOM_FLOAT_CHANCE = register("entity_game_time_seeded_random_float_chance",
            jsonObject -> {
                float randomChance = GsonHelper.getAsFloat(jsonObject, "random_chance");

                return (livingEntity, o) -> {
                    return (new Random(livingEntity.level.getGameTime())).nextFloat() < randomChance;
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<?, ?>>> BIPREDICATE_OF_PREDICATES = register("bipredicate_of_predicates",
            jsonObject -> {
                Predicate<Object> predicateForFirst = PredicateHelper.parsePredicateOrDefault(jsonObject, "predicate_for_first", "type", o -> true);
                Predicate<Object> predicateForSecond = PredicateHelper.parsePredicateOrDefault(jsonObject, "predicate_for_second", "type", o -> true);

                return (o1, o2) -> {
                    return predicateForFirst.test(o1) && predicateForSecond.test(o2);
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, ItemStack>>> ENTITY_CAN_ADD_ITEM_TO_INVENTORY = register("entity_can_add_item_to_inventory",
            jsonObject -> {

                return (livingEntity, itemStack) -> {
                    return livingEntity instanceof InventoryCarrier ic && ic.getInventory() instanceof SimpleContainer sc && sc.canAddItem(itemStack);
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, ItemStack>>> ENTITY_WANTS_TO_PICK_UP_ITEM = register("entity_wants_to_pick_up_item",
            jsonObject -> {

                return (livingEntity, itemStack) -> {
                    return livingEntity instanceof Mob mob && mob.wantsToPickUp(itemStack);
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, ItemStack>>> ENTITY_CAN_REPLACE_CURRENT_ITEM = register("entity_can_replace_current_item",
            jsonObject -> {

                return (livingEntity, itemStack) -> {
                    return livingEntity instanceof Mob mob && ReflectionHelper.reflectCanReplaceCurrentItem(mob, itemStack);
                };
            });



    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_OTHER_TARGET_MUCH_FURTHER_AWAY_THAN_CURRENT_ATTACK_TARGET = register("entity_other_target_much_further_away_than_current_attack_target",
            jsonObject -> {
                double extraDistanceAllowed = GsonHelper.getAsDouble(jsonObject, "extra_distance_allowed", 0);

                return (attacker, target) -> {
                    return BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(attacker, target, extraDistanceAllowed);
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_APPLY_BIPREDICATE_TO_RETRIEVED_ENTITY = register("entity_apply_bipredicate_to_retrieved_entity",
            jsonObject -> {
                Function<LivingEntity, Optional<LivingEntity>> retrievalFunction = FunctionHelper.parseFunction(jsonObject, "retrieval_function", "type");
                BiPredicate<LivingEntity, LivingEntity> biPredicate = PredicateHelper.parseBiPredicate(jsonObject, "bipredicate", "type");

                return (le, le1) -> {
                    Optional<LivingEntity> retrievedEntity = retrievalFunction.apply(le);
                    return retrievedEntity.filter(livingEntity -> biPredicate.test(livingEntity, le1)).isPresent();
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_IS_SAME_TYPE_AS = register("entity_is_same_type_as",
            jsonObject -> {
                return (le, le1) -> {
                    return le.getType() == le1.getType();
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_IS_ENTITY = register("entity_is_entity",
            jsonObject -> {
                return (le, le1) -> {
                    return le == le1;
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<?, ?>>> CUSTOM_BIPREDICATE = register("custom_bipredicate",
            jsonObject -> {
                String locationString = GsonHelper.getAsString(jsonObject, "location");
                ResourceLocation location = new ResourceLocation(locationString);
                return Aptitude.customLogicManager.getBiPredicate(location);
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_CAN_ATTACK = register("entity_can_attack",
            jsonObject -> {
                return LivingEntity::canAttack;
            });


    private static <U extends BiPredicate<?, ?>> RegistryObject<BiPredicateType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return BIPREDICATE_TYPES.register(name, () -> new BiPredicateType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        BIPREDICATE_TYPES.register(bus);
    }

    public static BiPredicateType<?> getBiPredicateType(ResourceLocation bptLocation) {
        BiPredicateType<?> value = BIPREDICATE_TYPE_REGISTRY.get().getValue(bptLocation);
        if(value == null) Aptitude.LOGGER.error("Failed to get BiPredicateType {}", bptLocation);
        return value;
    }
}
