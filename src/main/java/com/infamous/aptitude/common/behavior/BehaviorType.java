package com.infamous.aptitude.common.behavior;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

public class BehaviorType<U extends Behavior<?>> extends ForgeRegistryEntry<BehaviorType<?>> {

    private final Function<JsonObject, U> jsonFactory;

    public BehaviorType(Function<JsonObject, U> factoryIn) {
        this.jsonFactory = factoryIn;
    }

    public U fromJson(JsonObject jsonObject) {
        return this.jsonFactory.apply(jsonObject);
    }

}
