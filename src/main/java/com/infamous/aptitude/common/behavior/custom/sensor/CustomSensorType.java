package com.infamous.aptitude.common.behavior.custom.sensor;

import com.google.gson.JsonObject;
import com.infamous.aptitude.common.behavior.custom.JsonFriendly;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.function.Supplier;

public class CustomSensorType<U extends Sensor<?> & JsonFriendly<U>> extends SensorType<U> {

    public CustomSensorType(Supplier<U> factory) {
        super(factory);
    }

    public U createWithJson(JsonObject jsonObject){
        return this.create().fromJson(jsonObject);
    }
}
