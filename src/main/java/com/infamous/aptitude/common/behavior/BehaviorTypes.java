package com.infamous.aptitude.common.behavior;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.custom.behavior.AptitudeMeleeAttack;
import com.infamous.aptitude.common.behavior.custom.behavior.AptitudeSetWalkTargetFromAttackTargetIfTargetOutOfReach;
import com.infamous.aptitude.common.behavior.custom.behavior.AptitudeStartAdmiringItemIfSeen;
import com.infamous.aptitude.common.behavior.custom.behavior.AptitudeStopHoldingItemIfNoLongerAdmiring;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import com.infamous.aptitude.common.behavior.util.ConsumerHelper;
import com.infamous.aptitude.common.behavior.util.FunctionHelper;
import com.infamous.aptitude.common.behavior.util.PredicateHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Optional;
import java.util.function.*;

public class BehaviorTypes {

    private static final DeferredRegister<BehaviorType<?>> BEHAVIOR_TYPES = DeferredRegister.create((Class<BehaviorType<?>>) (Class) BehaviorType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<BehaviorType<?>>> BEHAVIOR_TYPE_REGISTRY = BEHAVIOR_TYPES.makeRegistry("behavior_types", () ->
            new RegistryBuilder<BehaviorType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("BehaviorType Added: " + obj.getRegistryName().toString() + " ")
            ).setDefaultKey(new ResourceLocation(Aptitude.MOD_ID, "do_nothing"))
    );

    public static final RegistryObject<BehaviorType<DoNothing>> DO_NOTHING = register("do_nothing", (jsonObject) -> {
        Pair<Integer, Integer> baseBehaviorDuration = BehaviorHelper.parseBaseBehaviorDuration(jsonObject);

        return new DoNothing(baseBehaviorDuration.getFirst(), baseBehaviorDuration.getSecond());
    });

    public static final RegistryObject<BehaviorType<MoveToTargetSink>> MOVE_TO_TARGET_SINK = register("move_to_target_sink", (jsonObject -> {
        Pair<Integer, Integer> baseBehaviorDuration = BehaviorHelper.parseBaseBehaviorDuration(jsonObject);

        return new MoveToTargetSink(baseBehaviorDuration.getFirst(), baseBehaviorDuration.getSecond());
    }));

    public static final RegistryObject<BehaviorType<LookAtTargetSink>> LOOK_AT_TARGET_SINK = register("look_at_target_sink", (jsonObject -> {
        Pair<Integer, Integer> baseBehaviorDuration = BehaviorHelper.parseBaseBehaviorDuration(jsonObject);


        return new LookAtTargetSink(baseBehaviorDuration.getFirst(), baseBehaviorDuration.getSecond());
    }));

    public static final RegistryObject<BehaviorType<BecomePassiveIfMemoryPresent>> BECOME_PASSIVE_IF_MEMORY_PRESENT = register("become_passive_if_memory_present", (jsonObject -> {
        MemoryModuleType<?> memoryType = BehaviorHelper.parseMemoryType(jsonObject, "memoryType");
        int pacifyDuration = GsonHelper.getAsInt(jsonObject, "pacifyDuration", 0);

        return new BecomePassiveIfMemoryPresent(memoryType, pacifyDuration);
    }));

    public static final RegistryObject<BehaviorType<AnimalMakeLove>> ANIMAL_MAKE_LOVE = register("animal_make_love", (jsonObject -> {
        EntityType<? extends Animal> partnerType = BehaviorHelper.parseEntityType(jsonObject, "partnerType");
        float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);

        return new AnimalMakeLove(partnerType, speedModifier);
    }));

    public static final RegistryObject<BehaviorType<SetWalkTargetAwayFrom<?>>> SET_WALK_TARGET_AWAY_FROM = register("set_walk_target_away_from", (jsonObject -> {
        MemoryModuleType<?> walkAwayFromMemory = BehaviorHelper.parseMemoryType(jsonObject, "walkAwayFromMemory");
        float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);
        int desiredDistance = GsonHelper.getAsInt(jsonObject, "desiredDistance", 0);
        boolean ignoreWalkTarget = GsonHelper.getAsBoolean(jsonObject, "ignoreWalkTarget", true);
        Function<?, Vec3> toPosition = FunctionHelper.parseFunction(jsonObject, "toPosition", "type");
        return new SetWalkTargetAwayFrom(walkAwayFromMemory, speedModifier, desiredDistance, ignoreWalkTarget, toPosition);
    }));

    public static final RegistryObject<BehaviorType<StartAttacking<?>>> START_ATTACKING = register("start_attacking", (jsonObject) -> {
        Predicate<LivingEntity> canAttackPredicate = PredicateHelper.parsePredicateOrDefault(jsonObject, "canAttackPredicate", "type", le -> true);
        Function<LivingEntity, Optional<? extends LivingEntity>> targetFinderFunction = FunctionHelper.parseFunction(jsonObject, "targetFinderFunction", "type");

        return new StartAttacking(canAttackPredicate, targetFinderFunction);
    });

    public static final RegistryObject<BehaviorType<RunIf<?>>> RUN_IF = register("run_if", (jsonObject) -> {
        Predicate<?> predicate = PredicateHelper.parsePredicate(jsonObject, "predicate", "type");
        Behavior<?> behavior = BehaviorHelper.parseBehavior(jsonObject, "wrappedBehavior", "type");
        boolean checkWhileRunningAlso = GsonHelper.getAsBoolean(jsonObject, "checkWhileRunningAlso", false);

        return new RunIf(predicate, behavior, checkWhileRunningAlso);
    });

    public static final RegistryObject<BehaviorType<RunSometimes<?>>> RUN_SOMETIMES = register("run_sometimes", (jsonObject) -> {
        Behavior<?> wrappedBehavior = BehaviorHelper.parseBehavior(jsonObject, "wrappedBehavior", "type");
        boolean resetTicks = GsonHelper.getAsBoolean(jsonObject, "resetTicks", false);
        UniformInt interval = BehaviorHelper.parseUniformInt(jsonObject, "interval");
        return new RunSometimes<>(wrappedBehavior, resetTicks, interval);
    });

    public static final RegistryObject<BehaviorType<SetEntityLookTarget>> SET_ENTITY_LOOK_TARGET = register("set_entity_look_target", (jsonObject) -> {
        Predicate<LivingEntity> predicate = PredicateHelper.parsePredicateOrDefault(jsonObject, "predicate", "type", le -> true);
        float maxDistSqr = GsonHelper.getAsFloat(jsonObject, "maxDistSqr", 0);
        return new SetEntityLookTarget(predicate, maxDistSqr);
    });

    public static final RegistryObject<BehaviorType<BabyFollowAdult<?>>> BABY_FOLLOW_ADULT = register("baby_follow_adult", (jsonObject) -> {
        UniformInt followRange = BehaviorHelper.parseUniformInt(jsonObject, "followRange");
        float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);
        return new BabyFollowAdult<>(followRange, speedModifier);
    });

    public static final RegistryObject<BehaviorType<RunOne<?>>> RUN_ONE = register("run_one", (jsonObject) -> {
        List<Pair<Behavior<?>, Integer>> behaviors = BehaviorHelper.parseWeightedBehaviors(jsonObject, "behaviors");
        return new RunOne(behaviors);
    });

    public static final RegistryObject<BehaviorType<RandomStroll>> RANDOM_STROLL = register("random_stroll", (jsonObject) -> {
        float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);
        int maxHorizontalDistance = GsonHelper.getAsInt(jsonObject, "maxHorizontalDistance", 10);
        int maxVerticalDistance = GsonHelper.getAsInt(jsonObject, "maxVerticalDistance", 7);
        boolean mayStrollFromWater = GsonHelper.getAsBoolean(jsonObject, "mayStrollFromWater", true);
        return new RandomStroll(speedModifier, maxHorizontalDistance, maxVerticalDistance, mayStrollFromWater);
    });

    public static final RegistryObject<BehaviorType<SetWalkTargetFromLookTarget>> SET_WALK_TARGET_FROM_LOOK_TARGET = register("set_walk_target_from_look_target", (jsonObject) -> {
        float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);
        int closeEnoughDistance = GsonHelper.getAsInt(jsonObject, "closeEnoughDistance", 0);
        return new SetWalkTargetFromLookTarget(speedModifier, closeEnoughDistance);
    });

    public static final RegistryObject<BehaviorType<AptitudeSetWalkTargetFromAttackTargetIfTargetOutOfReach>> SET_WALK_TARGET_FROM_ATTACK_TARGET_IF_TARGET_OUT_OF_REACH = register("set_walk_target_from_attack_target_if_target_out_of_reach", (jsonObject) -> {
        Function<LivingEntity, Float> speedModifier = FunctionHelper.parseFunction(jsonObject, "speedModifier", "type");
        BiPredicate<LivingEntity, LivingEntity> isWithinAttackRange = PredicateHelper.parseBiPredicate(jsonObject, "isWithinAttackRange", "type");

        return new AptitudeSetWalkTargetFromAttackTargetIfTargetOutOfReach(speedModifier, isWithinAttackRange);
    });

    public static final RegistryObject<BehaviorType<AptitudeMeleeAttack>> MELEE_ATTACK = register("melee_attack", (jsonObject) -> {
        int cooldownBetweenAttacks = GsonHelper.getAsInt(jsonObject, "cooldownBetweenAttacks", 20);
        Predicate<LivingEntity> isHoldingUsableProjectileWeapon = PredicateHelper.parsePredicate(jsonObject, "isHoldingUsableProjectileWeapon", "type");
        BiPredicate<LivingEntity, LivingEntity> isWithinMeleeAttackRange = PredicateHelper.parseBiPredicate(jsonObject, "isWithinMeleeAttackRange", "type");
        return new AptitudeMeleeAttack(cooldownBetweenAttacks, isHoldingUsableProjectileWeapon, isWithinMeleeAttackRange);
    });

    public static final RegistryObject<BehaviorType<StopAttackingIfTargetInvalid<?>>> STOP_ATTACKING_IF_TARGET_INVALID = register("stop_attacking_if_target_invalid", (jsonObject) -> {
        Predicate<LivingEntity> stopAttackingWhen = PredicateHelper.parsePredicateOrDefault(jsonObject, "stopAttackingWhen", "type", le -> false);
        Consumer<LivingEntity> onTargetErased = ConsumerHelper.parseConsumerOrDefault(jsonObject, "onTargetErased", "type", le -> {});

        return new StopAttackingIfTargetInvalid(stopAttackingWhen, onTargetErased);
    });


    public static final RegistryObject<BehaviorType<EraseMemoryIf<?>>> ERASE_MEMORY_IF = register("erase_memory_if", (jsonObject) -> {
        Predicate<LivingEntity> predicate = PredicateHelper.parsePredicate(jsonObject, "predicate", "type");
        MemoryModuleType<?> memoryType = BehaviorHelper.parseMemoryType(jsonObject, "memoryType");

        return new EraseMemoryIf<>(predicate, memoryType);
    });

    public static final RegistryObject<BehaviorType<InteractWithDoor>> INTERACT_WITH_DOOR = register("interact_with_door",
            jsonObject -> {
                return new InteractWithDoor();
            });

    public static final RegistryObject<BehaviorType<CopyMemoryWithExpiry<?, ?>>> COPY_MEMORY_WITH_EXPIRY = register("copy_memory_with_expiry",
            jsonObject -> {
            Predicate<?> predicate = PredicateHelper.parsePredicate(jsonObject, "predicate", "type");
            MemoryModuleType<?> sourceMemory = BehaviorHelper.parseMemoryType(jsonObject, "sourceMemory");
            MemoryModuleType<?> targetMemory = BehaviorHelper.parseMemoryType(jsonObject, "targetMemory");
            UniformInt durationOfCopy = BehaviorHelper.parseUniformInt(jsonObject, "durationOfCopy");
                return new CopyMemoryWithExpiry(predicate, sourceMemory, targetMemory, durationOfCopy);
            });

    public static final RegistryObject<BehaviorType<AptitudeStopHoldingItemIfNoLongerAdmiring>> STOP_HOLDING_ITEM_IF_NO_LONGER_ADMIRING = register("stop_holding_item_if_no_longer_admiring",
            jsonObject -> {
                Predicate<LivingEntity> shouldStopHoldingItem = PredicateHelper.parsePredicate(jsonObject, "shouldStopHoldingItem", "type");
                Consumer<LivingEntity> stopHoldingItem = ConsumerHelper.parseConsumer(jsonObject, "stopHoldingItem", "type");
                return new AptitudeStopHoldingItemIfNoLongerAdmiring(shouldStopHoldingItem, stopHoldingItem);
            });

    public static final RegistryObject<BehaviorType<AptitudeStartAdmiringItemIfSeen>> START_ADMIRING_ITEM_IF_SEEN = register("start_admiring_item_if_seen",
            jsonObject -> {
                Ingredient lovedItems = Ingredient.fromJson(jsonObject.get("loved_items"));
                int admireDuration = GsonHelper.getAsInt(jsonObject, "admireDuration", 0);
                return new AptitudeStartAdmiringItemIfSeen(lovedItems, admireDuration);
            });

    public static final RegistryObject<BehaviorType<StartCelebratingIfTargetDead>> START_CELEBRATING_IF_TARGET_DEAD = register("start_celebrating_if_target_dead",
            jsonObject -> {
                int celebrateDuration = GsonHelper.getAsInt(jsonObject, "celebrateDuration");
                BiPredicate<LivingEntity, LivingEntity> dancePredicate = (le, le1) -> true;
                return new StartCelebratingIfTargetDead(celebrateDuration, dancePredicate);
            });

    public static final RegistryObject<BehaviorType<StopBeingAngryIfTargetDead<?>>> STOP_BEING_ANGRY_IF_TARGET_DEAD = register("stop_being_angry_if_target_dead",
            jsonObject -> {
                return new StopBeingAngryIfTargetDead<>();
            });

    public static final RegistryObject<BehaviorType<SetLookAndInteract>> SET_LOOK_AND_INTERACT = register("set_look_and_interact",
            jsonObject -> {
                EntityType<?> type = BehaviorHelper.parseEntityType(jsonObject, "entity_type");
                int interactionRange = GsonHelper.getAsInt(jsonObject, "interaction_range", 0);
                Predicate<LivingEntity> selfFilter = PredicateHelper.parsePredicateOrDefault(jsonObject, "selfFilter", "type", le -> true);
                Predicate<LivingEntity> targetFilter = PredicateHelper.parsePredicateOrDefault(jsonObject, "targetFilter", "type", le -> true);
                return new SetLookAndInteract(type, interactionRange, selfFilter, targetFilter);
            });

    public static final RegistryObject<BehaviorType<InteractWith<?, ?>>> INTERACT_WITH = register("interact_with",
            jsonObject -> {
                EntityType<?> type = BehaviorHelper.parseEntityType(jsonObject, "entity_type");
                int interactionRange = GsonHelper.getAsInt(jsonObject, "interaction_range", 0);
                Predicate<LivingEntity> selfFilter = PredicateHelper.parsePredicateOrDefault(jsonObject, "selfFilter", "type", le -> true);
                Predicate<LivingEntity> targetFilter = PredicateHelper.parsePredicateOrDefault(jsonObject, "targetFilter", "type", le -> true);
                MemoryModuleType<LivingEntity> memory = BehaviorHelper.parseMemoryType(jsonObject, "memory");
                float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);
                int maxDist = GsonHelper.getAsInt(jsonObject, "maxDist", 0);
                return new InteractWith(type, interactionRange, selfFilter, targetFilter, memory, speedModifier, maxDist);
            });

    public static final RegistryObject<BehaviorType<StrollToPoi>> STROLL_TO_POI = register("stroll_to_poi",
            jsonObject -> {
                MemoryModuleType<GlobalPos> memoryType = BehaviorHelper.parseMemoryType(jsonObject, "memoryType");
                float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);
                int closeEnoughDist = GsonHelper.getAsInt(jsonObject, "closeEnoughDist", 0);
                int maxDistanceFromPoi = GsonHelper.getAsInt(jsonObject, "maxDistanceFromPoi", 0);
                return new StrollToPoi(memoryType, speedModifier, closeEnoughDist, maxDistanceFromPoi);
            });

    public static final RegistryObject<BehaviorType<StrollAroundPoi>> STROLL_AROUND_POI = register("stroll_around_poi",
            jsonObject -> {
                MemoryModuleType<GlobalPos> memoryType = BehaviorHelper.parseMemoryType(jsonObject, "memoryType");
                float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);
                int maxDistanceFromPoi = GsonHelper.getAsInt(jsonObject, "maxDistanceFromPoi", 0);
                return new StrollAroundPoi(memoryType, speedModifier, maxDistanceFromPoi);
            });

    private static <U extends Behavior<?>> RegistryObject<BehaviorType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return BEHAVIOR_TYPES.register(name, () -> new BehaviorType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        BEHAVIOR_TYPES.register(bus);
    }

    public static BehaviorType<?> getBehaviorType(ResourceLocation name) {
        BehaviorType<?> value = BEHAVIOR_TYPE_REGISTRY.get().getValue(name);
        Aptitude.LOGGER.info("Attempting to get behavior type {}, got {}", name, value.getRegistryName());
        return value;
    }
}
