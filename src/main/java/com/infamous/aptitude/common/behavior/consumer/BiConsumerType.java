package com.infamous.aptitude.common.behavior.consumer;

import com.google.gson.JsonObject;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class BiConsumerType<U extends BiConsumer<?, ?>> extends ForgeRegistryEntry<BiConsumerType<?>> {

    private final Function<JsonObject, U> jsonFactory;

    public BiConsumerType(Function<JsonObject, U> factoryIn) {
        this.jsonFactory = factoryIn;
    }

    public U fromJson(JsonObject jsonObject) {
        return this.jsonFactory.apply(jsonObject);
    }

    @SuppressWarnings("unchecked")
    public <X extends BiConsumer<?, ?>> BiConsumerType<X> cast()
    {
        return (BiConsumerType<X>)this;
    }
}
