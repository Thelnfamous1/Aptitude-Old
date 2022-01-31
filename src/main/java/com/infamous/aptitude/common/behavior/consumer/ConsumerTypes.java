package com.infamous.aptitude.common.behavior.consumer;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.functions.FunctionType;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import com.infamous.aptitude.mixin.LivingEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConsumerTypes {

    private static final DeferredRegister<ConsumerType<?>> CONSUMER_TYPES = DeferredRegister.create((Class<ConsumerType<?>>)(Class)ConsumerType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<ConsumerType<?>>> CONSUMER_TYPE_REGISTRY = CONSUMER_TYPES.makeRegistry("consumer_types", () ->
            new RegistryBuilder<ConsumerType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("ConsumerType Added: " + obj.getRegistryName().toString() + " ")
            ).setDefaultKey(new ResourceLocation(Aptitude.MOD_ID, "nothing"))
    );

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> NOTHING = register("nothing",
            jsonObject -> {
                return livingEntity -> {};
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> HOGLINLIKE_UPDATE = register("hoglinlike_update",
            jsonObject -> {
                return livingEntity -> {
                    ResourceLocation etLocation = ForgeRegistries.ENTITIES.getKey(livingEntity.getType());
                    List<Activity> rotatingActivities = Aptitude.brainManager.getRotatingActivities(etLocation);

                    Brain<?> brain = livingEntity.getBrain();
                    Activity prevActivity = brain.getActiveNonCoreActivity().orElse((Activity)null);
                    brain.setActiveActivityToFirstValid(rotatingActivities);
                    Activity currActivity = brain.getActiveNonCoreActivity().orElse((Activity)null);

                    // ACTIVITY SOUND
                    if (prevActivity != currActivity) {
                        livingEntity.getBrain().getActiveNonCoreActivity().map((activity) -> {
                            if(activity == Activity.AVOID){
                                return SoundEvents.HOGLIN_RETREAT;
                            } else if (activity == Activity.FIGHT) {
                                return SoundEvents.HOGLIN_ANGRY;
                            } else if(livingEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT)){
                                return SoundEvents.HOGLIN_RETREAT;
                            } else{
                                return SoundEvents.HOGLIN_AMBIENT;
                            }
                        }).ifPresent(soundEvent -> livingEntity.playSound(soundEvent, ((LivingEntityAccessor)livingEntity).callGetSoundVolume(), livingEntity.getVoicePitch()));
                    }

                    // AGGRESSION
                    if(livingEntity instanceof Mob mob){
                        mob.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
                    }
                };
    });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> PIGLINLIKE_UPDATE = register("piglinlike_update",
            jsonObject -> {
                return livingEntity -> {
                    ResourceLocation etLocation = ForgeRegistries.ENTITIES.getKey(livingEntity.getType());
                    List<Activity> rotatingActivities = Aptitude.brainManager.getRotatingActivities(etLocation);

                    Brain<?> brain = livingEntity.getBrain();
                    Activity prevActivity = brain.getActiveNonCoreActivity().orElse((Activity)null);
                    brain.setActiveActivityToFirstValid(rotatingActivities);
                    Activity currActivity = brain.getActiveNonCoreActivity().orElse((Activity)null);

                    // ACTIVITY SOUND
                    if (prevActivity != currActivity) {
                        livingEntity.getBrain().getActiveNonCoreActivity().map((activity) -> {
                            boolean nearAvoidTarget = brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET) && brain.getMemory(MemoryModuleType.AVOID_TARGET).get().closerThan(livingEntity, 12.0D);
                            if (activity == Activity.FIGHT) {
                                return SoundEvents.PIGLIN_ANGRY;
                            } else if (activity == Activity.AVOID && nearAvoidTarget) {
                                return SoundEvents.PIGLIN_RETREAT;
                            } else if (activity == Activity.ADMIRE_ITEM) {
                                return SoundEvents.PIGLIN_ADMIRING_ITEM;
                            } else if (activity == Activity.CELEBRATE) {
                                return SoundEvents.PIGLIN_CELEBRATE;
                            } else if (livingEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM)) {
                                return SoundEvents.PIGLIN_JEALOUS;
                            } else if(livingEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT)){
                                return SoundEvents.PIGLIN_RETREAT;
                            } else{
                                return SoundEvents.PIGLIN_AMBIENT;
                            }
                        }).ifPresent(soundEvent -> livingEntity.playSound(soundEvent, ((LivingEntityAccessor)livingEntity).callGetSoundVolume(), livingEntity.getVoicePitch()));
                    }

                    // AGGRESSION
                    if(livingEntity instanceof Mob mob){
                        mob.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
                    }

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
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> AXOLOTLLIKE_UPDATE = register("axolotllike_update",
            jsonObject -> {
                return livingEntity -> {
                    ResourceLocation etLocation = ForgeRegistries.ENTITIES.getKey(livingEntity.getType());
                    List<Activity> rotatingActivities = Aptitude.brainManager.getRotatingActivities(etLocation);

                    Brain<?> brain = livingEntity.getBrain();
                    Activity prevActivity = brain.getActiveNonCoreActivity().orElse((Activity)null);
                    if (prevActivity != Activity.PLAY_DEAD) {
                        brain.setActiveActivityToFirstValid(rotatingActivities);
                        Activity currActivity = brain.getActiveNonCoreActivity().orElse((Activity) null);
                        if (prevActivity == Activity.FIGHT && currActivity != Activity.FIGHT) {
                            brain.setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, 2400L);
                        }
                    }
                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> GOATLIKE_UPDATE = register("goatlike_update",
            jsonObject -> {
                return livingEntity -> {
                    ResourceLocation etLocation = ForgeRegistries.ENTITIES.getKey(livingEntity.getType());
                    List<Activity> rotatingActivities = Aptitude.brainManager.getRotatingActivities(etLocation);

                    livingEntity.getBrain().setActiveActivityToFirstValid(rotatingActivities);

                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> ZOGLINLIKE_UPDATE = register("zoglinlike_update",
            jsonObject -> {
                return livingEntity -> {
                    JsonObject soundEventObj = GsonHelper.getAsJsonObject(jsonObject, "fight_sound");
                    SoundEvent fightSound = BehaviorHelper.parseSoundEventString(soundEventObj, "type");
                    float volume = GsonHelper.getAsFloat(soundEventObj, "volume", ((LivingEntityAccessor)livingEntity).callGetSoundVolume());
                    float pitch = GsonHelper.getAsFloat(soundEventObj, "pitch", livingEntity.getVoicePitch());

                    ResourceLocation etLocation = ForgeRegistries.ENTITIES.getKey(livingEntity.getType());
                    List<Activity> rotatingActivities = Aptitude.brainManager.getRotatingActivities(etLocation);

                    Brain<?> brain = livingEntity.getBrain();
                    Activity prevActivity = brain.getActiveNonCoreActivity().orElse((Activity)null);
                    brain.setActiveActivityToFirstValid(rotatingActivities);
                    Activity currActivity = brain.getActiveNonCoreActivity().orElse((Activity)null);

                    // ACTIVITY SOUND
                    if(prevActivity != currActivity){
                        livingEntity.getBrain().getActiveNonCoreActivity().map((activity) -> {
                            if(activity == Activity.FIGHT){
                                return fightSound;
                            } else{
                                return null;
                            }
                        }).ifPresent(soundEvent -> livingEntity.playSound(soundEvent, volume, pitch));
                    }

                    // AGGRESSION
                    if(livingEntity instanceof Mob mob){
                        mob.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
                    }

                };
            });

    public static final RegistryObject<ConsumerType<Consumer<LivingEntity>>> VILLAGERLIKE_UPDATE = register("villagerlike_update",
            jsonObject -> {
                return livingEntity -> {
                    /*
                    if (livingEntity.assignProfessionWhenSpawned) {
                        livingEntity.assignProfessionWhenSpawned = false;
                    }

                    if (!livingEntity.isTrading() && livingEntity.updateMerchantTimer > 0) {
                        --livingEntity.updateMerchantTimer;
                        if (livingEntity.updateMerchantTimer <= 0) {
                            if (livingEntity.increaseProfessionLevelOnUpdate) {
                                livingEntity.increaseMerchantCareer();
                                livingEntity.increaseProfessionLevelOnUpdate = false;
                            }

                            livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
                        }
                    }
                     */

                    Level level = livingEntity.level;
                    /*
                    if (livingEntity.lastTradedPlayer != null && level instanceof ServerLevel) {
                        ((ServerLevel) level).onReputationEvent(ReputationEventType.TRADE, livingEntity.lastTradedPlayer, livingEntity);
                        level.broadcastEntityEvent(livingEntity, (byte)14); // ParticleTypes.HAPPY_VILLAGER
                        livingEntity.lastTradedPlayer = null;
                    }
                     */

                    if (livingEntity instanceof Mob mob && !mob.isNoAi() && livingEntity.getRandom().nextInt(100) == 0) {
                        Raid raid = ((ServerLevel) level).getRaidAt(livingEntity.blockPosition());
                        if (raid != null && raid.isActive() && !raid.isOver()) {
                            level.broadcastEntityEvent(livingEntity, (byte)42); // ParticleTypes.SPLASH
                        }
                    }

                    /*
                    if (livingEntity.getVillagerData().getProfession() == VillagerProfession.NONE && livingEntity.isTrading()) {
                        livingEntity.stopTrading();
                    }
                     */
                };
            });

    private static <U extends Consumer<?>> RegistryObject<ConsumerType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return CONSUMER_TYPES.register(name, () -> new ConsumerType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        CONSUMER_TYPES.register(bus);
    }

    public static ConsumerType<?> getConsumerType(ResourceLocation ftLocation) {
        ConsumerType<?> value = CONSUMER_TYPE_REGISTRY.get().getValue(ftLocation);
        Aptitude.LOGGER.info("Attempting to get consumer type {}, got {}", ftLocation, value.getRegistryName());
        return value;
    }
}
