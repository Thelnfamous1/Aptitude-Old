package com.infamous.aptitude.common.behavior;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.infamous.aptitude.Aptitude;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class BehaviorTypes {

    private static final DeferredRegister<BehaviorType<?>> BEHAVIOR_TYPES = DeferredRegister.create(AptitudeRegistries.BEHAVIOR_TYPES, Aptitude.MOD_ID);

    public static final RegistryObject<BehaviorType<Behavior<?>>> MOVE_TO_TARGET_SINK = register("move_to_target_sink", (jsonObject -> {
        Pair<Integer, Integer> baseBehaviorDuration = BehaviorHelper.parseBaseBehaviorDuration(jsonObject);

        return new MoveToTargetSink(baseBehaviorDuration.getFirst(), baseBehaviorDuration.getSecond());
    }));

    public static final RegistryObject<BehaviorType<Behavior<?>>> LOOK_AT_TARGET_SINK = register("look_at_target_sink", (jsonObject -> {
        Pair<Integer, Integer> baseBehaviorDuration = BehaviorHelper.parseBaseBehaviorDuration(jsonObject);


        return new LookAtTargetSink(baseBehaviorDuration.getFirst(), baseBehaviorDuration.getSecond());
    }));

    public static final RegistryObject<BehaviorType<Behavior<?>>> BECOME_PASSIVE_IF_MEMORY_PRESENT = register("become_passive_if_memory_present", (jsonObject -> {
        MemoryModuleType<?> memoryType = BehaviorHelper.parseMemoryType(jsonObject, "memoryType");
        int pacifyDuration = GsonHelper.getAsInt(jsonObject, "pacifyDuration", 0);

        return new BecomePassiveIfMemoryPresent(memoryType, pacifyDuration);
    }));

    public static final RegistryObject<BehaviorType<Behavior<?>>> ANIMAL_MAKE_LOVE = register("animal_make_love", (jsonObject -> {
        EntityType<?> partnerType = BehaviorHelper.parseEntityType(jsonObject, "partnerType");
        float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);

        try{
            return new AnimalMakeLove((EntityType<? extends Animal>) partnerType, speedModifier);
        } catch (ClassCastException e){
            throw new JsonParseException("Invalid entity type for AnimalMakeLove: " + partnerType);
        }
    }));

    public static final RegistryObject<BehaviorType<Behavior<?>>> SET_WALK_TARGET_AWAY_FROM = register("set_walk_target_away_from", (jsonObject -> {
        MemoryModuleType<?> walkAwayFromMemory = BehaviorHelper.parseMemoryType(jsonObject, "walkAwayFromMemory");
        float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);
        int desiredDistance = GsonHelper.getAsInt(jsonObject, "desiredDistance", 0);
        boolean ignoreWalkTarget = GsonHelper.getAsBoolean(jsonObject, "ignoreWalkTarget", true);
        try{
            return SetWalkTargetAwayFrom.entity((MemoryModuleType<? extends Entity>) walkAwayFromMemory, speedModifier, desiredDistance, ignoreWalkTarget);
        } catch (ClassCastException e){
            try{
                return SetWalkTargetAwayFrom.pos((MemoryModuleType<BlockPos>) walkAwayFromMemory, speedModifier, desiredDistance, ignoreWalkTarget);
            } catch (ClassCastException e1){
                throw new JsonParseException("Invalid memory type for SetWalkTargetAwayFrom: " + walkAwayFromMemory);
            }
        }
    }));

    public static final RegistryObject<BehaviorType<Behavior<?>>> START_ATTACKING = register("start_attacking", (jsonObject) -> {
        JsonObject canAttackPredicateObj = GsonHelper.getAsJsonObject(jsonObject, "canAttackPredicate");
        JsonObject targetFinderFunctionObj = GsonHelper.getAsJsonObject(jsonObject, "targetFinderFunction");

        Predicate<Mob> canAttackPredicate = BehaviorPredicates.predicateFromJson(canAttackPredicateObj);
        Function<Mob, Optional<? extends LivingEntity>> targetFinderFunction = BehaviorFunctions.functionFromJson(targetFinderFunctionObj);

        return new StartAttacking<>(canAttackPredicate, targetFinderFunction);
    });

    /*
    public static final RegistryObject<BehaviorType<Behavior<?>>> RUN_IF = register("run_if", (jsonObject) -> {
        Predicate<LivingEntity> predicate = le -> true;
        Behavior<?> behavior = BehaviorHelper.parseBehavior(jsonObject, "wrappedBehavior", "type");
        boolean checkWhileRunningAlso = GsonHelper.getAsBoolean(jsonObject, "checkWhileRunningAlso", false);

        return new RunIf<>(predicate, behavior, checkWhileRunningAlso);
    });
     */

    public static final RegistryObject<BehaviorType<Behavior<?>>> RUN_SOMETIMES = register("run_sometimes", (jsonObject) -> {
        Behavior<?> wrappedBehavior = BehaviorHelper.parseBehavior(jsonObject, "wrappedBehavior", "type");
        boolean resetTicks = GsonHelper.getAsBoolean(jsonObject, "resetTicks", false);
        UniformInt interval = BehaviorHelper.parseUniformInt(jsonObject, "interval");
        return new RunSometimes<>(wrappedBehavior, resetTicks, interval);
    });

    public static final RegistryObject<BehaviorType<Behavior<?>>> SET_ENTITY_LOOK_TARGET = register("set_entity_look_target", (jsonObject) -> {
        float maxDistSqr = GsonHelper.getAsFloat(jsonObject, "maxDistSqr", 0);
        return new SetEntityLookTarget(maxDistSqr);
    });

    public static final RegistryObject<BehaviorType<Behavior<?>>> BABY_FOLLOW_ADULT = register("baby_follow_adult", (jsonObject) -> {
        UniformInt followRange = BehaviorHelper.parseUniformInt(jsonObject, "followRange");
        float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);
        return new BabyFollowAdult<>(followRange, speedModifier);
    });

    /*
    public static final RegistryObject<BehaviorType<Behavior<?>>> RUN_ONE = register("run_one", (jsonObject) -> {
        List<Pair<Behavior<?>, Integer>> behaviors = BehaviorHelper.parsePrioritizedBehaviors(jsonObject, "behaviors");
        return new RunOne<>(behaviors);
    });
     */

    public static final RegistryObject<BehaviorType<Behavior<?>>> RANDOM_STROLL = register("random_stroll", (jsonObject) -> {
        float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);
        int maxHorizontalDistance = GsonHelper.getAsInt(jsonObject, "maxHorizontalDistance", 10);
        int maxVerticalDistance = GsonHelper.getAsInt(jsonObject, "maxVerticalDistance", 7);
        boolean mayStrollFromWater = GsonHelper.getAsBoolean(jsonObject, "mayStrollFromWater", true);
        return new RandomStroll(speedModifier, maxHorizontalDistance, maxVerticalDistance, mayStrollFromWater);
    });

    public static final RegistryObject<BehaviorType<Behavior<?>>> SET_WALK_TARGET_FROM_LOOK_TARGET = register("set_walk_target_from_look_target", (jsonObject) -> {
        float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);
        int closeEnoughDistance = GsonHelper.getAsInt(jsonObject, "closeEnoughDistance", 0);
        return new SetWalkTargetFromLookTarget(speedModifier, closeEnoughDistance);
    });

    public static final RegistryObject<BehaviorType<Behavior<?>>> DO_NOTHING = register("do_nothing", (jsonObject) -> {
        Pair<Integer, Integer> baseBehaviorDuration = BehaviorHelper.parseBaseBehaviorDuration(jsonObject);

        return new DoNothing(baseBehaviorDuration.getFirst(), baseBehaviorDuration.getSecond());
    });

    public static final RegistryObject<BehaviorType<Behavior<?>>> SET_WALK_TARGET_FROM_ATTACK_TARGET_IF_TARGET_OUT_OF_REACH = register("set_walk_target_from_attack_target_if_target_out_of_reach", (jsonObject) -> {
        float speedModifier = BehaviorHelper.parseSpeedModifier(jsonObject);

        return new SetWalkTargetFromAttackTargetIfTargetOutOfReach(speedModifier);
    });

    public static final RegistryObject<BehaviorType<Behavior<?>>> MELEE_ATTACK = register("melee_attack", (jsonObject) -> {
        int cooldownBetweenAttacks = GsonHelper.getAsInt(jsonObject, "cooldownBetweenAttacks", 20);

        return new MeleeAttack(cooldownBetweenAttacks);
    });

    public static final RegistryObject<BehaviorType<Behavior<?>>> STOP_ATTACKING_IF_TARGET_INVALID = register("stop_attacking_if_target_invalid", (jsonObject) -> {
        return new StopAttackingIfTargetInvalid<>();
    });

    /*
    public static final RegistryObject<BehaviorType<Behavior<?>>> ERASE_MEMORY_IF = register("erase_memory_if", (jsonObject) -> {
        Predicate<LivingEntity> predicate = le -> true;
        MemoryModuleType<?> memoryType = BehaviorHelper.parseMemoryType(jsonObject, "memoryType");

        return new EraseMemoryIf<>(predicate, memoryType);
    });
     */

    private static <U extends Behavior<?>> RegistryObject<BehaviorType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return BEHAVIOR_TYPES.register(name, () -> new BehaviorType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        BEHAVIOR_TYPES.register(bus);
    }
}
