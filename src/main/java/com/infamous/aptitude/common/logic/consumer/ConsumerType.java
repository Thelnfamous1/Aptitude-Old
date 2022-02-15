package com.infamous.aptitude.common.logic.consumer;

import com.google.gson.JsonObject;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Consumer;
import java.util.function.Function;

public class ConsumerType<U extends Consumer<?>> extends ForgeRegistryEntry<ConsumerType<?>> {

    private final Function<JsonObject, U> jsonFactory;

    public ConsumerType(Function<JsonObject, U> factoryIn) {
        this.jsonFactory = factoryIn;
    }

    public U fromJson(JsonObject jsonObject) {
        return this.jsonFactory.apply(jsonObject);
    }

    @SuppressWarnings("unchecked")
    public <X extends Consumer<?>> ConsumerType<X> cast()
    {
        return (ConsumerType<X>)this;
    }
}
