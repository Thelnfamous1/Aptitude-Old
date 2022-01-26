package com.infamous.aptitude.common.behavior;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.Collection;

public class PlaceholderBrain<E extends LivingEntity> extends Brain<E> {
    public PlaceholderBrain(Collection<? extends MemoryModuleType<?>> memoryTypes, Collection<? extends SensorType<? extends Sensor<? super E>>> sensorTypes) {
        super(memoryTypes, sensorTypes, ImmutableList.of(), () -> codec(memoryTypes, sensorTypes));
    }
}
