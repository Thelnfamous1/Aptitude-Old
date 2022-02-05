package com.infamous.aptitude.common.behavior.predicates;

import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.FunctionHelper;
import com.infamous.aptitude.common.behavior.util.MeleeAttackHelper;
import com.infamous.aptitude.common.behavior.util.PredicateHelper;
import com.infamous.aptitude.common.behavior.util.RangedAttackHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Optional;
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


    private static <U extends BiPredicate<?, ?>> RegistryObject<BiPredicateType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return BIPREDICATE_TYPES.register(name, () -> new BiPredicateType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        BIPREDICATE_TYPES.register(bus);
    }

    public static BiPredicateType<?> getBiPredicateType(ResourceLocation bptLocation) {
        BiPredicateType<?> value = BIPREDICATE_TYPE_REGISTRY.get().getValue(bptLocation);
        Aptitude.LOGGER.info("Attempting to get bipredicate type {}, got {}", bptLocation, value.getRegistryName());
        return value;
    }
}
