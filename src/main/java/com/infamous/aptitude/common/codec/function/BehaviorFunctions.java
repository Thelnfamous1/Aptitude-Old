package com.infamous.aptitude.common.codec.function;

import com.infamous.aptitude.common.codec.state.BlockPredicate;
import com.infamous.aptitude.common.codec.state.ConstantPredicate;
import com.infamous.aptitude.common.codec.state.StatePredicate;
import com.infamous.aptitude.common.codec.state.StatePredicateSerializer;
import net.minecraft.resources.ResourceLocation;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.codec.RegistryDispatcher;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

// some basic state predicates
public class BehaviorFunctions
{
    // the cast here is needed to compile this on eclipse,
    // intellij is fine without it
    public static final RegistryDispatcher<StatePredicateSerializer<?>, StatePredicate> DISPATCHER = RegistryDispatcher.<StatePredicateSerializer<?>, StatePredicate>makeDispatchForgeRegistry(
            FMLJavaModLoadingContext.get().getModEventBus(),
            StatePredicateSerializer.class,
            new ResourceLocation(Aptitude.MOD_ID, "behavior_functions"),
            builder -> builder.disableSaving().disableSync());

    // remember to subscribe this to the mod bus
    public static final DeferredRegister<StatePredicateSerializer<?>> STATE_PREDICATE_SERIALIZERS = DISPATCHER.makeDeferredRegister(Aptitude.MOD_ID);

    public static final RegistryObject<StatePredicateSerializer<ConstantPredicate>> ALWAYS_TRUE = STATE_PREDICATE_SERIALIZERS.register("always_true", () -> new StatePredicateSerializer<>(ConstantPredicate.ALWAYS_TRUE_CODEC));
    public static final RegistryObject<StatePredicateSerializer<ConstantPredicate>> ALWAYS_FALSE = STATE_PREDICATE_SERIALIZERS.register("always_false", () -> new StatePredicateSerializer<>(ConstantPredicate.ALWAYS_FALSE_CODEC));
    public static final RegistryObject<StatePredicateSerializer<BlockPredicate>> BLOCK_SERIALIZER = STATE_PREDICATE_SERIALIZERS.register("block", () -> new StatePredicateSerializer<>(BlockPredicate.CODEC));

}
