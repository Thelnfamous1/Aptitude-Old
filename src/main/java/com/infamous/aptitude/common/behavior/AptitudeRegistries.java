package com.infamous.aptitude.common.behavior;

import com.infamous.aptitude.common.behavior.functions.FunctionType;
import com.infamous.aptitude.common.behavior.predicates.PredicateType;
import com.infamous.aptitude.common.interaction.InteractionType;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class AptitudeRegistries {
    public static final IForgeRegistry<BehaviorType<?>> BEHAVIOR_TYPES = RegistryManager.ACTIVE.getRegistry(BehaviorType.class);
    public static final IForgeRegistry<PredicateType<?>> PREDICATE_TYPES = RegistryManager.ACTIVE.getRegistry(PredicateType.class);
    public static final IForgeRegistry<FunctionType<?>> FUNCTION_TYPES = RegistryManager.ACTIVE.getRegistry(FunctionType.class);

    public static final IForgeRegistry<InteractionType<?>> INTERACTION_TYPES = RegistryManager.ACTIVE.getRegistry(InteractionType.class);

}
