package com.infamous.aptitude.common.behavior.custom.sensor;

import com.infamous.aptitude.Aptitude;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class AptitudeSensorTypes {

    private static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, Aptitude.MOD_ID);

    public static final RegistryObject<SensorType<PigSpecificSensor>> PIG_SPECIFIC_SENSOR = register("pig_specific_sensor", PigSpecificSensor::new);

    public static final RegistryObject<SensorType<ZombieSpecificSensor>> ZOMBIE_SPECIFIC_SENSOR = register("zombie_specific_sensor", ZombieSpecificSensor::new);
    public static final RegistryObject<SensorType<HuskSpecificSensor>> HUSK_SPECIFIC_SENSOR = register("husk_specific_sensor", HuskSpecificSensor::new);

    public static final RegistryObject<SensorType<AptitudeNearestItemSensor>> NEAREST_ITEMS = register("nearest_items", AptitudeNearestItemSensor::new);

    private static <U extends Sensor<?>> RegistryObject<SensorType<U>> register(String name, Supplier<U> constructor) {
        return SENSOR_TYPES.register(name, () -> new SensorType<>(constructor));
    }

    public static void register(IEventBus bus){
        SENSOR_TYPES.register(bus);
    }
}
