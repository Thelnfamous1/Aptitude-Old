package com.infamous.aptitude;

import com.infamous.aptitude.common.behavior.BehaviorTypes;
import com.infamous.aptitude.common.interaction.MobInteractionTypes;
import com.infamous.aptitude.common.manager.brain.BrainManager;
import com.infamous.aptitude.common.logic.consumer.BiConsumerTypes;
import com.infamous.aptitude.common.logic.consumer.ConsumerTypes;
import com.infamous.aptitude.common.behavior.custom.memory.AptitudeMemoryModuleTypes;
import com.infamous.aptitude.common.behavior.custom.sensor.AptitudeSensorTypes;
import com.infamous.aptitude.common.logic.functions.BiFunctionTypes;
import com.infamous.aptitude.common.logic.functions.FunctionTypes;
import com.infamous.aptitude.common.logic.predicates.BiPredicateTypes;
import com.infamous.aptitude.common.logic.predicates.PredicateTypes;
import com.infamous.aptitude.common.manager.custom.CustomLogicManager;
import com.infamous.aptitude.common.manager.selector.SelectorManager;
import com.infamous.aptitude.common.manager.base.BaseAIManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Aptitude.MOD_ID)
public class Aptitude
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "aptitude";
    public static CustomLogicManager customLogicManager;
    public static BrainManager brainManager;
    public static SelectorManager selectorManager;
    public static BaseAIManager baseAIManager;

    public Aptitude() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        PredicateTypes.register(modEventBus);
        BiPredicateTypes.register(modEventBus);

        FunctionTypes.register(modEventBus);
        BiFunctionTypes.register(modEventBus);

        ConsumerTypes.register(modEventBus);
        BiConsumerTypes.register(modEventBus);

        MobInteractionTypes.register(modEventBus);

        BehaviorTypes.register(modEventBus);

        AptitudeMemoryModuleTypes.register(modEventBus);
        AptitudeSensorTypes.register(modEventBus);
    }
}
