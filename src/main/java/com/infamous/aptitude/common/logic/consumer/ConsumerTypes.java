package com.infamous.aptitude.common.logic.consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import com.infamous.aptitude.common.behavior.util.ConsumerHelper;
import com.infamous.aptitude.common.behavior.util.FunctionHelper;
import com.infamous.aptitude.common.behavior.util.PredicateHelper;
import com.infamous.aptitude.mixin.LivingEntityAccessor;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.*;
import java.util.function.*;

public class ConsumerTypes {

    private static final DeferredRegister<ConsumerType<?>> CONSUMER_TYPES = DeferredRegister.create((Class<ConsumerType<?>>)(Class)ConsumerType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<ConsumerType<?>>> CONSUMER_TYPE_REGISTRY = CONSUMER_TYPES.makeRegistry("consumer_types", () ->
            new RegistryBuilder<ConsumerType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("ConsumerType Added: " + obj.getRegistryName().toString() + " ")
            )
    );

    public static final RegistryObject<ConsumerType<Consumer<?>>> NOTHING = register("nothing",
            jsonObject -> {
                return o -> {};
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> UPDATE_AGGRESSION_FLAG = register("update_aggression_flag",
            jsonObject -> {
                Predicate<LivingEntity> predicate = PredicateHelper.parsePredicate(jsonObject, "predicate", "type");
                return livingEntity -> {
                    if(livingEntity instanceof Mob mob){
                        mob.setAggressive(predicate.test(mob));
                    }
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> SET_BOOLEAN_MEMORY = register("set_boolean_memory",
            jsonObject -> {
                MemoryModuleType<Boolean> memoryType = BehaviorHelper.parseMemoryType(jsonObject, "memory");
                boolean value = GsonHelper.getAsBoolean(jsonObject, "value");
                Function<LivingEntity, Long> expireTimeFunction = BehaviorHelper.parseExpireTimeFunction(jsonObject, "expire_time", "type");

                return livingEntity -> {
                    long expireTime = expireTimeFunction.apply(livingEntity);
                    livingEntity.getBrain().setMemoryWithExpiry(memoryType, value, expireTime);
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> PLAY_ACTIVITY_SOUND = register("play_activity_sound",
            jsonObject -> {
                JsonArray playFirstValidArr = GsonHelper.getAsJsonArray(jsonObject, "play_first_valid");
                Map<Predicate<LivingEntity>, SoundEvent> predicateToSoundMap = new LinkedHashMap<>();
                playFirstValidArr.forEach(jsonElement -> {
                    JsonObject elementObj = jsonElement.getAsJsonObject();
                    Predicate<LivingEntity> predicate = PredicateHelper.parsePredicate(elementObj, "predicate", "type");
                    SoundEvent soundEvent = BehaviorHelper.parseSoundEventString(elementObj, "sound_event");
                    predicateToSoundMap.put(predicate, soundEvent);
                });

                return livingEntity -> {
                    for(Map.Entry<Predicate<LivingEntity>, SoundEvent> entry : predicateToSoundMap.entrySet()){
                        if(entry.getKey().test(livingEntity)){
                            livingEntity.playSound(entry.getValue(), ((LivingEntityAccessor)livingEntity).callGetSoundVolume(), livingEntity.getVoicePitch());
                            break;
                        }
                    }
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> UPDATE_ACTIVITY = register("update_activity",
            jsonObject -> {
                Predicate<LivingEntity> updatePredicate = PredicateHelper.parsePredicateOrDefault(jsonObject, "update_predicate", "type", le -> true);
                Consumer<LivingEntity> onChanged = ConsumerHelper.parseConsumerOrDefault(jsonObject, "on_changed_callback", "type", le -> {});
                BiConsumer<LivingEntity, Activity> handleActivityChange = ConsumerHelper.parseBiConsumerOrDefault(jsonObject, "handle_activity_change", "type", (le, a) -> {});

                List<Consumer<LivingEntity>> additionalCallbacks = new ArrayList<>();
                if(jsonObject.has("additional_callbacks")){
                    JsonArray additionalCallbacksArr = GsonHelper.getAsJsonArray(jsonObject, "additional_callbacks");
                    additionalCallbacksArr.forEach(jsonElement -> {
                        JsonObject elementObj = jsonElement.getAsJsonObject();
                        Consumer<LivingEntity> consumer = ConsumerHelper.parseConsumer(elementObj, "type");
                        additionalCallbacks.add(consumer);
                    });
                }

                return livingEntity -> {
                    if(updatePredicate.test(livingEntity)){
                        ResourceLocation etLocation = livingEntity.getType().getRegistryName();
                        List<Activity> rotatingActivities = Aptitude.brainManager.getRotatingActivities(etLocation);

                        Brain<?> brain = livingEntity.getBrain();
                        Activity prevActivity = brain.getActiveNonCoreActivity().orElse((Activity)null);
                        brain.setActiveActivityToFirstValid(rotatingActivities);
                        Activity currActivity = brain.getActiveNonCoreActivity().orElse((Activity)null);

                        // ON CHANGED
                        if (prevActivity != currActivity) {
                            onChanged.accept(livingEntity);
                            handleActivityChange.accept(livingEntity, prevActivity);

                            /*
                            if (prevActivity == Activity.FIGHT && currActivity != Activity.FIGHT) {
                                brain.setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, 2400L);
                            }
                             */
                        }
                    }

                    // ADDITIONAL CALLBACKS
                    additionalCallbacks.forEach(consumer -> consumer.accept(livingEntity));
                };
    });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> ENTITY_FINISH_ADMIRING_AND_MAYBE_BARTER = register("entity_finish_admiring_and_maybe_barter",
            jsonObject -> {
                Predicate<LivingEntity> canBarter = PredicateHelper.parsePredicateOrDefault(jsonObject, "can_barter", "type", le -> !le.isBaby());
                EquipmentSlot admireItemSlot = BehaviorHelper.parseEquipmentSlotOrDefault(jsonObject, "admire_item_slot", EquipmentSlot.OFFHAND);
                Ingredient barterCurrency = Ingredient.fromJson(jsonObject.get("barter_currency"));
                boolean doBarter = GsonHelper.getAsBoolean(jsonObject, "do_barter", true);

                String barteringLootTableString = GsonHelper.getAsString(jsonObject, "bartering_loot_table", "");
                ResourceLocation barteringLootTable = new ResourceLocation(barteringLootTableString);

                EquipmentSlot swapToSlot = BehaviorHelper.parseEquipmentSlotOrDefault(jsonObject, "swap_to_slot", EquipmentSlot.MAINHAND);
                Ingredient lovedItems = Ingredient.fromJson(jsonObject.get("loved_items"));

                return livingEntity -> {
                    ItemStack admireItem = livingEntity.getItemBySlot(admireItemSlot);
                    livingEntity.setItemSlot(admireItemSlot, ItemStack.EMPTY);
                    if (canBarter.test(livingEntity)) {
                        boolean isBarterCurrency = barterCurrency.test(admireItem);
                        if (isBarterCurrency && doBarter) {
                            BehaviorHelper.throwItems(livingEntity, BehaviorHelper.getBarterResponseItems(livingEntity, barteringLootTable));
                        } else {
                            boolean equippedItem = livingEntity instanceof Mob mob && mob.equipItemIfPossible(admireItem);
                            if (!equippedItem) {
                                BehaviorHelper.putInInventory(livingEntity, admireItem);
                            }
                        }
                    } else {
                        boolean equippedItem = livingEntity instanceof Mob mob && mob.equipItemIfPossible(admireItem);
                        if (!equippedItem) {
                            ItemStack swapToItem = livingEntity.getItemBySlot(swapToSlot);
                            if (lovedItems.test(swapToItem)) {
                                BehaviorHelper.putInInventory(livingEntity, swapToItem);
                            } else {
                                BehaviorHelper.throwItems(livingEntity, Collections.singletonList(swapToItem));
                            }

                            livingEntity.setItemSlot(swapToSlot, admireItem);
                            if(livingEntity instanceof Mob mob){
                                mob.setGuaranteedDrop(swapToSlot);
                                mob.setPersistenceRequired();
                            }
                        }
                    }
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> ENTITY_STOP_RIDING = register("entity_stop_riding",
            jsonObject -> {
                Predicate<LivingEntity> predicate = PredicateHelper.parsePredicate(jsonObject, "predicate", "type");
                return rider -> {
                    if (predicate.test(rider)) {
                        rider.stopRiding();
                    }
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> ENTITY_UPDATE_CELEBRATION_FLAG = register("entity_update_celebration_flag",
            jsonObject -> {
                Predicate<LivingEntity> predicate = PredicateHelper.parsePredicate(jsonObject, "predicate", "type");

                return livingEntity -> {
                    boolean shouldCelebrate = predicate.test(livingEntity);
                    if(livingEntity instanceof Piglin piglin){
                        piglin.setDancing(shouldCelebrate);
                    } else if(livingEntity instanceof Raider raider){
                        raider.setCelebrating(shouldCelebrate);
                    }
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<?>>> PREDICATED_CONSUMER = register("predicated_consumer",
            jsonObject -> {
                Predicate<Object> predicate = PredicateHelper.parsePredicate(jsonObject, "predicate", "type");
                Consumer<Object> consumer = ConsumerHelper.parseConsumer(jsonObject, "consumer", "type");
                Consumer<Object> defaultConsumer = ConsumerHelper.parseConsumerOrDefault(jsonObject, "default", "type", o -> {});
                return o -> {
                    if(predicate.test(o)) {
                        consumer.accept(o);
                    } else{
                        defaultConsumer.accept(o);
                    }
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> ENTITY_USE_DEFAULT_ACTIVITY = register("entity_use_default_activity",
            jsonObject -> {
                return livingEntity -> {
                    livingEntity.getBrain().useDefaultActivity();
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<?>>> ALL_OF_CONSUMER = register("all_of_consumer",
            jsonObject -> {
                List<Consumer<Object>> consumers = ConsumerHelper.parseConsumers(jsonObject, "consumers", "type");
                return o -> {
                    consumers.forEach(consumer -> consumer.accept(o));
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> ENTITY_SET_EQUIPMENT_IN_SLOT = register("entity_set_equipment_in_slot",
            jsonObject -> {
            EquipmentSlot equipmentSlot = BehaviorHelper.parseEquipmentSlot(jsonObject, "slot");
            ItemStack equipment = ShapedRecipe.itemStackFromJson(jsonObject.getAsJsonObject("equipment"));

                return livingEntity -> {
                    livingEntity.setItemSlot(equipmentSlot, equipment);
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> ENTITY_SET_CAN_PICK_UP_LOOT = register("entity_set_can_pick_up_loot",
            jsonObject -> {
                boolean canPickUpLoot = GsonHelper.getAsBoolean(jsonObject, "can_pick_up_loot", true);
                return livingEntity -> {
                    if(livingEntity instanceof Mob mob) mob.setCanPickUpLoot(canPickUpLoot);
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> ENTITY_SET_CAN_OPEN_DOORS = register("entity_set_can_open_doors",
            jsonObject -> {
                boolean canOpenDoors = GsonHelper.getAsBoolean(jsonObject, "can_open_doors", true);
                return livingEntity -> {
                    if(livingEntity instanceof Mob mob && GoalUtils.hasGroundPathNavigation(mob)){
                        ((GroundPathNavigation)mob.getNavigation()).setCanOpenDoors(canOpenDoors);
                    }
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> SET_GLOBAL_POSITION_MEMORY = register("entity_set_global_position_memory",
            jsonObject -> {
                MemoryModuleType<GlobalPos> memoryType = BehaviorHelper.parseMemoryType(jsonObject, "memory");
                Function<LivingEntity, GlobalPos> globalPosFunction = FunctionHelper.parseFunction(jsonObject, "global_position_function", "type");
                Function<LivingEntity, Long> expireTimeFunction = BehaviorHelper.parseExpireTimeFunction(jsonObject, "expire_time", "type");

                return livingEntity -> {
                    long expireTime = expireTimeFunction.apply(livingEntity);
                    GlobalPos value = globalPosFunction.apply(livingEntity);
                    livingEntity.getBrain().setMemoryWithExpiry(memoryType, value, expireTime);
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> ENTITY_SET_ANGRY_AT_FROM_MEMORY = register("entity_set_angry_at_from_memory",
            jsonObject -> {
                Function<LivingEntity, Optional<? extends LivingEntity>> fromMemoryFunction = FunctionHelper.parseFunction(jsonObject, "from_memory_function", "type");
                MemoryModuleType<UUID> angryAtMemory = BehaviorHelper.parseMemoryTypeOrDefault(jsonObject, "angry_at_memory", MemoryModuleType.ANGRY_AT);
                Function<LivingEntity, Long> expireTimeFunc = BehaviorHelper.parseExpireTimeFunction(jsonObject, "expire_time", "type");
                return angry -> {
                    Optional<? extends LivingEntity> fromMemory = fromMemoryFunction.apply(angry);
                    if(fromMemory.isPresent()){
                        long expireTime = expireTimeFunc.apply(angry);
                        angry.getBrain().setMemoryWithExpiry(angryAtMemory, fromMemory.get().getUUID(), expireTime);
                    }
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> ENTITY_ERASE_MEMORY = register("entity_erase_memory",
            jsonObject -> {
                MemoryModuleType<?> memoryType = BehaviorHelper.parseMemoryType(jsonObject, "memory_type");
                return livingEntity -> {
                    livingEntity.getBrain().eraseMemory(memoryType);
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> ENTITY_APPLY_CONSUMER_TO_LIST_OF_ENTITIES = register("entity_apply_consumer_to_list_of_entities",
            jsonObject -> {
                Function<LivingEntity, List<LivingEntity>> retrievalFunction = FunctionHelper.parseFunction(jsonObject, "retrieval_function", "type");
                Consumer<LivingEntity> consumer = ConsumerHelper.parseConsumer(jsonObject, "consumer", "type");
                return le -> {
                    List<LivingEntity> entities = retrievalFunction.apply(le);
                    entities.forEach(consumer);
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> ENTITY_APPLY_BICONSUMER_TO_RETRIEVED_ENTITY = register("entity_apply_biconsumer_to_retrieved_entity",
            jsonObject -> {
                Function<LivingEntity, Optional<LivingEntity>> retrievalFunction = FunctionHelper.parseFunction(jsonObject, "retrieval_function", "type");
                BiConsumer<LivingEntity, LivingEntity> consumer = ConsumerHelper.parseBiConsumer(jsonObject, "biconsumer", "type");
                return le -> {
                    Optional<LivingEntity> retrievedEntity = retrievalFunction.apply(le);
                    retrievedEntity.ifPresent(e -> consumer.accept(le, e));
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<?>>> CUSTOM_CONSUMER = register("custom_consumer",
            jsonObject -> {
                String locationString = GsonHelper.getAsString(jsonObject, "location");
                ResourceLocation location = new ResourceLocation(locationString);
                return Aptitude.customLogicManager.getConsumer(location);
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> ENTITY_SET_PERSISTENCE_REQUIRED = register("entity_set_persistence_required",
            jsonObject -> {
                return le -> {
                    if(le instanceof Mob mob) mob.setPersistenceRequired();
                };
            });

    private static <U extends Consumer<?>> RegistryObject<ConsumerType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return CONSUMER_TYPES.register(name, () -> new ConsumerType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        CONSUMER_TYPES.register(bus);
    }

    public static ConsumerType<?> getConsumerType(ResourceLocation ctLocation) {
        ConsumerType<?> value = CONSUMER_TYPE_REGISTRY.get().getValue(ctLocation);
        if(value == null) Aptitude.LOGGER.error("Failed to get ConsumerType {}", ctLocation);
        return value;
    }
}
