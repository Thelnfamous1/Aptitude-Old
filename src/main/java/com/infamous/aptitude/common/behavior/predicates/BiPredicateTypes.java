package com.infamous.aptitude.common.behavior.predicates;

import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BiPredicateTypes {

    private static final DeferredRegister<BiPredicateType<?>> BIPREDICATE_TYPES = DeferredRegister.create((Class<BiPredicateType<?>>)(Class)BiPredicateType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<BiPredicateType<?>>> BIPREDICATE_TYPE_REGISTRY = BIPREDICATE_TYPES.makeRegistry("bipredicate_types", () ->
            new RegistryBuilder<BiPredicateType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("BiPredicateType Added: " + obj.getRegistryName().toString() + " ")
            ).setDefaultKey(new ResourceLocation(Aptitude.MOD_ID, "always_true"))
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
                return (le, le1) -> {
                    if(le instanceof Mob mob){
                        double distanceToSqr = mob.distanceToSqr(le1.getX(), le1.getY(), le1.getZ());
                        return distanceToSqr <= mob.getMeleeAttackRangeSqr(le1);
                    }
                    return false;
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_IS_WITHIN_PROJECTILE_ATTACK_RANGE = register("entity_is_within_projectile_attack_range",
            jsonObject -> {
                int projectileAttackRangeBuffer = GsonHelper.getAsInt(jsonObject, "projectile_attack_range_buffer", 0);
                return (le, le1) -> {
                    if(le instanceof Mob mob){
                        Item item = mob.getMainHandItem().getItem();
                        if (item instanceof ProjectileWeaponItem projectileweaponitem) {
                            if (mob.canFireProjectileWeapon((ProjectileWeaponItem)item)) {
                                int projectileRange = projectileweaponitem.getDefaultProjectileRange() - projectileAttackRangeBuffer;
                                return mob.closerThan(le1, (double)projectileRange);
                            }
                        }
                    }
                    return false;
                };
            });

    public static final RegistryObject<BiPredicateType<BiPredicate<LivingEntity, LivingEntity>>> ENTITY_IS_WITHIN_ATTACK_RANGE = register("entity_is_within_attack_range",
            jsonObject -> {
                int projectileAttackRangeBuffer = GsonHelper.getAsInt(jsonObject, "projectile_attack_range_buffer", 0);
                return (le, le1) -> {
                    if(le instanceof Mob mob){
                        Item item = mob.getMainHandItem().getItem();
                        if (item instanceof ProjectileWeaponItem projectileweaponitem) {
                            if (mob.canFireProjectileWeapon((ProjectileWeaponItem)item)) {
                                int projectileRange = projectileweaponitem.getDefaultProjectileRange() - projectileAttackRangeBuffer;
                                return mob.closerThan(le1, (double)projectileRange);
                            }
                        }
                        double distanceToSqr = mob.distanceToSqr(le1.getX(), le1.getY(), le1.getZ());
                        return distanceToSqr <= mob.getMeleeAttackRangeSqr(le1);
                    }
                    return false;
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
