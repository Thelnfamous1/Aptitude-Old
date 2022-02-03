package com.infamous.aptitude.common.behavior.consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import com.infamous.aptitude.common.behavior.util.ConsumerHelper;
import com.infamous.aptitude.common.behavior.util.PredicateHelper;
import com.infamous.aptitude.mixin.LivingEntityAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConsumerTypes {

    private static final DeferredRegister<ConsumerType<?>> CONSUMER_TYPES = DeferredRegister.create((Class<ConsumerType<?>>)(Class)ConsumerType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<ConsumerType<?>>> CONSUMER_TYPE_REGISTRY = CONSUMER_TYPES.makeRegistry("consumer_types", () ->
            new RegistryBuilder<ConsumerType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("ConsumerType Added: " + obj.getRegistryName().toString() + " ")
            ).setDefaultKey(new ResourceLocation(Aptitude.MOD_ID, "nothing"))
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
                Long expireTime = GsonHelper.getAsLong(jsonObject, "expire_time", Long.MAX_VALUE);

                return livingEntity -> {
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
                Predicate<LivingEntity> updatePredicate = jsonObject.has("update_predicate") ?
                        PredicateHelper.parsePredicate(jsonObject, "update_predicate", "type") :
                        le -> true;

                Consumer<LivingEntity> onChanged = jsonObject.has("on_changed_callback") ?
                        ConsumerHelper.parseConsumer(jsonObject, "on_changed_callback", "type") :
                        le -> {};

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
                        ResourceLocation etLocation = ForgeRegistries.ENTITIES.getKey(livingEntity.getType());
                        List<Activity> rotatingActivities = Aptitude.brainManager.getRotatingActivities(etLocation);

                        Brain<?> brain = livingEntity.getBrain();
                        Activity prevActivity = brain.getActiveNonCoreActivity().orElse((Activity)null);
                        brain.setActiveActivityToFirstValid(rotatingActivities);
                        Activity currActivity = brain.getActiveNonCoreActivity().orElse((Activity)null);

                        // ON CHANGED
                        if (prevActivity != currActivity) {
                            onChanged.accept(livingEntity);

                            /*
                            if (prevActivity == Activity.FIGHT && currActivity != Activity.FIGHT) {
                                brain.setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, 2400L);
                            }
                             */
                        }
                    }

                    // ADDITIONAL CALLBACKS
                    additionalCallbacks.forEach(consumer -> consumer.accept(livingEntity));

                    /*
                    // RIDING
                    boolean babyRidingBaby = livingEntity.isBaby() && livingEntity.getVehicle() instanceof LivingEntity mount && mount.isBaby();
                    if (!brain.hasMemoryValue(MemoryModuleType.RIDE_TARGET) && babyRidingBaby) {
                        livingEntity.stopRiding();
                    }

                    // CELEBRATION
                    if (!brain.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) {
                        brain.eraseMemory(MemoryModuleType.DANCING);
                    }
                    //livingEntity.setDancing(brain.hasMemoryValue(MemoryModuleType.DANCING));
                     */
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
        Aptitude.LOGGER.info("Attempting to get consumer type {}, got {}", ctLocation, value.getRegistryName());
        return value;
    }
}
