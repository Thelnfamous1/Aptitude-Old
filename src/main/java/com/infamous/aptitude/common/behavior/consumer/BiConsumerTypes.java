package com.infamous.aptitude.common.behavior.consumer;

import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.*;

public class BiConsumerTypes {

    private static final DeferredRegister<BiConsumerType<?>> BICONSUMER_TYPES = DeferredRegister.create((Class<BiConsumerType<?>>)(Class)BiConsumerType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<BiConsumerType<?>>> BICONSUMER_TYPE_REGISTRY = BICONSUMER_TYPES.makeRegistry("biconsumer_types", () ->
            new RegistryBuilder<BiConsumerType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("BiConsumerType Added: " + obj.getRegistryName().toString() + " ")
            )
    );

    public static final RegistryObject<BiConsumerType<BiConsumer<?, ?>>> NOTHING = register("nothing",
            jsonObject -> {
                return (o, o1) -> {};
            });


    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, Boolean>>> ENTITY_SET_CHARGING_CROSSBOW = register("entity_set_charging_crossbow",
            jsonObject -> {
                return (le, b) -> {
                    if(le instanceof CrossbowAttackMob crossbowAttackMob){
                        crossbowAttackMob.setChargingCrossbow(b);
                    }
                };
            });


    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, LivingEntity>>> ENTITY_PERFORM_CROSSBOW_ATTACK = register("entity_perform_crossbow_attack",
            jsonObject -> {
                float shootingPower = GsonHelper.getAsFloat(jsonObject, "shooting_power", 1.6F);
                int baseInaccuracy = GsonHelper.getAsInt(jsonObject, "base_inaccuracy", 14);
                int difficultyScale = GsonHelper.getAsInt(jsonObject, "difficulty_scale", 4);
                return (shooter, target) -> {
                    InteractionHand weaponHoldingHand = ProjectileUtil.getWeaponHoldingHand(shooter, item -> item instanceof CrossbowItem);
                    ItemStack itemInHand = shooter.getItemInHand(weaponHoldingHand);
                    if (shooter.isHolding(is -> is.getItem() instanceof CrossbowItem)) {
                        RangedAttackHelper.peformCrossbowShooting(shooter.level, shooter, target, weaponHoldingHand, itemInHand, shootingPower, (float)(baseInaccuracy - shooter.level.getDifficulty().getId() * difficultyScale));
                    }

                    shooter.setNoActionTime(0); // onCrossbowAttackPerformed
                };
            });


    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, ItemEntity>>> ENTITY_PICK_UP_WANTED_ITEM = register("entity_pick_up_wanted_item",
            jsonObject -> {
                Predicate<ItemStack> takesAll = PredicateHelper.parsePredicateOrDefault(jsonObject, "takes_all", "type", stack -> false);

                Predicate<ItemStack> lovedItems = PredicateHelper.parsePredicateOrDefault(jsonObject, "loved_items", "type", stack -> false);
                MemoryModuleType<Integer> timeTryingToReachAdmireItem = BehaviorHelper.parseMemoryType(jsonObject, "time_trying_to_reach_wanted_item_memory");
                MemoryModuleType<Boolean> admiringItem = BehaviorHelper.parseMemoryType(jsonObject, "handling_item_memory");
                long admiringItemTime = GsonHelper.getAsLong(jsonObject, "handling_item_time", 120L);

                Predicate<ItemStack> foodItems = PredicateHelper.parsePredicateOrDefault(jsonObject, "food_items", "type", stack -> false);
                MemoryModuleType<Boolean> ateRecently = BehaviorHelper.parseMemoryType(jsonObject, "consumed_item_memory");
                long ateRecentlyTime = GsonHelper.getAsLong(jsonObject, "consumed_item_time", 200L);

                return (le, ie) -> {
                    le.onItemPickup(ie);
                    BehaviorHelper.stopWalking(le);
                    ItemStack pickedUp;
                    if (takesAll.test(ie.getItem())) {
                        le.take(ie, ie.getItem().getCount());
                        pickedUp = ie.getItem();
                        ie.discard();
                    } else {
                        le.take(ie, 1);
                        pickedUp = BehaviorHelper.removeOneItemFromItemEntity(ie);
                    }

                    if (lovedItems.test(pickedUp)) {
                        le.getBrain().eraseMemory(timeTryingToReachAdmireItem);
                        BehaviorHelper.holdInOffhand(le, pickedUp);
                        le.getBrain().setMemoryWithExpiry(admiringItem, true, admiringItemTime);
                    } else {
                        if (foodItems.test(pickedUp) && !le.getBrain().hasMemoryValue(ateRecently)) {
                            le.getBrain().setMemoryWithExpiry(ateRecently, true, ateRecentlyTime);
                        } else {
                            boolean equippedItem = le instanceof Mob mob && mob.equipItemIfPossible(pickedUp);
                            if (!equippedItem) {
                                BehaviorHelper.putInInventory(le, pickedUp);
                            }
                        }
                    }
                };
            });


    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, ItemEntity>>> ENTITY_PICK_UP_EQUIPMENT = register("entity_pick_up_equipment",
            jsonObject -> {
                return (le, ie) -> {
                    ItemStack itemstack = ie.getItem();
                    if (le instanceof Mob mob && mob.equipItemIfPossible(itemstack)) {
                        le.onItemPickup(ie);
                        le.take(ie, itemstack.getCount());
                        ie.discard();
                    }
                };
            });

    public static final RegistryObject<BiConsumerType<BiConsumer<?, ?>>> BICONSUMER_OF_CONSUMERS = register("biconsumer_of_consumers",
            jsonObject -> {
                Consumer<Object> consumerForFirst = ConsumerHelper.parseConsumerOrDefault(jsonObject, "consumer_for_first", "type", o -> {});
                Consumer<Object> consumerForSecond = ConsumerHelper.parseConsumerOrDefault(jsonObject, "consumer_for_second", "type", o -> {});

                return (o, o1) -> {
                    consumerForFirst.accept(o);
                    consumerForSecond.accept(o1);
                };
            });

    public static final RegistryObject<BiConsumerType<BiConsumer<?, ?>>> BIPREDICATED_BICONSUMER = register("bipredicated_biconsumer",
            jsonObject -> {
                BiPredicate<Object, Object> biPredicate = PredicateHelper.parseBiPredicate(jsonObject, "bipredicate", "type");
                BiConsumer<Object, Object> biConsumer = ConsumerHelper.parseBiConsumer(jsonObject, "biconsumer", "type");
                BiConsumer<Object, Object> defaultBiConsumer = ConsumerHelper.parseBiConsumerOrDefault(jsonObject, "default", "type", (o, o1) -> {});


                return (o, o1) -> {
                    if(biPredicate.test(o, o1)){
                        biConsumer.accept(o, o1);
                    } else{
                        defaultBiConsumer.accept(o, o1);
                    }
                };
            });

    public static final RegistryObject<BiConsumerType<BiConsumer<?, ?>>> ALL_OF_BICONSUMER = register("all_of_biconsumer",
            jsonObject -> {
                List<BiConsumer<Object, Object>> biConsumers = ConsumerHelper.parseBiConsumers(jsonObject, "biconsumers", "type");

                return (o, o1) -> {
                    for(BiConsumer<Object, Object> biConsumer : biConsumers){
                        biConsumer.accept(o, o1);
                    }
                };
            });

    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, LivingEntity>>> ENTITY_SET_ANGRY_AT = register("entity_set_angry_at",
            jsonObject -> {
                MemoryModuleType<UUID> angryAtMemory = BehaviorHelper.parseMemoryTypeOrDefault(jsonObject, "angry_at_memory", MemoryModuleType.ANGRY_AT);
                Function<LivingEntity, Long> expireTimeFunc = BehaviorHelper.parseExpireTimeFunction(jsonObject, "expire_time", "type");
                return (angry, target) -> {
                    long expireTime = expireTimeFunc.apply(angry);
                    angry.getBrain().setMemoryWithExpiry(angryAtMemory, target.getUUID(), expireTime);
                };
            });

    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, LivingEntity>>> ENTITY_APPLY_BICONSUMER_TO_LIST_OF_ENTITIES = register("entity_apply_biconsumer_to_list_of_entities",
            jsonObject -> {
                Function<LivingEntity, List<LivingEntity>> retrievalFunction = FunctionHelper.parseFunction(jsonObject, "retrieval_function", "type");
                BiConsumer<LivingEntity, LivingEntity> biConsumer = ConsumerHelper.parseBiConsumer(jsonObject, "biconsumer", "type");
                return (le, le1) -> {
                    List<LivingEntity> entities = retrievalFunction.apply(le);
                    entities.forEach(e -> biConsumer.accept(e, le1));
                };
            });

    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, LivingEntity>>> ENTITY_APPLY_BICONSUMER_TO_NEAREST_TARGET = register("entity_apply_biconsumer_to_nearest_target",
            jsonObject -> {
                Function<LivingEntity, Optional<LivingEntity>> retrievalFunction = FunctionHelper.parseFunction(jsonObject, "retrieval_function", "type");
                BiConsumer<LivingEntity, LivingEntity> biConsumer = ConsumerHelper.parseBiConsumer(jsonObject, "biconsumer", "type");
                return (le, target) -> {
                    Optional<LivingEntity> retrievedTarget = retrievalFunction.apply(le);
                    LivingEntity nearestTarget = BehaviorUtils.getNearestTarget(le, retrievedTarget, target);
                    BiPredicate<LivingEntity, LivingEntity> nearestBetterThanRetrieved = PredicateHelper.parseBiPredicateOrDefault(jsonObject, "nearest_better_than_retrieved", "type", (retrieved, nearest) -> true);
                    if (retrievedTarget.isEmpty() || nearestBetterThanRetrieved.test(retrievedTarget.get(), nearestTarget)) {
                        biConsumer.accept(le, nearestTarget);
                    }
                };
            });

    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, LivingEntity>>> ENTITY_SET_ENTITY_MEMORY = register("entity_set_entity_memory",
            jsonObject -> {
                MemoryModuleType<LivingEntity> memoryType = BehaviorHelper.parseMemoryType(jsonObject, "memory_type");
                Function<LivingEntity, Long> expireTimeFunction = BehaviorHelper.parseExpireTimeFunction(jsonObject, "expire_time", "type");
                return (le, target) -> {
                    long expireTime = expireTimeFunction.apply(le);
                    le.getBrain().setMemoryWithExpiry(memoryType, target, expireTime);
                };
            });

    public static final RegistryObject<BiConsumerType<BiConsumer<?, ?>>> CUSTOM_BICONSUMER = register("custom_biconsumer",
            jsonObject -> {
                String locationString = GsonHelper.getAsString(jsonObject, "location");
                ResourceLocation location = new ResourceLocation(locationString);
                return Aptitude.customLogicManager.getBiConsumer(location);
            });


    private static <U extends BiConsumer<?, ?>> RegistryObject<BiConsumerType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return BICONSUMER_TYPES.register(name, () -> new BiConsumerType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        BICONSUMER_TYPES.register(bus);
    }

    public static BiConsumerType<?> getBiConsumerType(ResourceLocation bctLocation) {
        BiConsumerType<?> value = BICONSUMER_TYPE_REGISTRY.get().getValue(bctLocation);
        if(value == null) Aptitude.LOGGER.error("Failed to get BiConsumerType {}", bctLocation);
        return value;
    }
}
