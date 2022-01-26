package com.infamous.aptitude;

import com.infamous.aptitude.common.behavior.BehaviorTypes;
import com.infamous.aptitude.common.behavior.BrainManager;
import com.infamous.aptitude.common.behavior.custom.AptitudeMemoryModuleTypes;
import com.infamous.aptitude.common.behavior.custom.AptitudeSensorTypes;
import com.infamous.aptitude.common.behavior.functions.FunctionTypes;
import com.infamous.aptitude.common.behavior.predicates.PredicateTypes;
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
    public static BrainManager brainManager;

    public Aptitude() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        PredicateTypes.register(modEventBus);
        FunctionTypes.register(modEventBus);
        BehaviorTypes.register(modEventBus);
        AptitudeMemoryModuleTypes.register(modEventBus);
        AptitudeSensorTypes.register(modEventBus);
    }
}
