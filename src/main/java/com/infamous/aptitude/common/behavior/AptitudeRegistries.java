package com.infamous.aptitude.common.behavior;

import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class AptitudeRegistries {
    public static final IForgeRegistry<BehaviorType<?>> BEHAVIOR_TYPES = RegistryManager.ACTIVE.getRegistry(BehaviorType.class);
    public static final IForgeRegistry<InteractionType<?>> INTERACTION_TYPES = RegistryManager.ACTIVE.getRegistry(InteractionType.class);

}
