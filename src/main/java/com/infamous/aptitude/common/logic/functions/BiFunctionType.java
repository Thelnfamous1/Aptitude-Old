package com.infamous.aptitude.common.logic.functions;

import com.google.gson.JsonObject;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.BiFunction;
import java.util.function.Function;


public class BiFunctionType<U extends BiFunction<?, ?, ?>> extends ForgeRegistryEntry<BiFunctionType<?>> {

    private final Function<JsonObject, U> jsonFactory;

    public BiFunctionType(Function<JsonObject, U> factoryIn) {
        this.jsonFactory = factoryIn;
    }

    public U fromJson(JsonObject jsonObject) {
        return this.jsonFactory.apply(jsonObject);
    }

    @SuppressWarnings("unchecked")
    public <X extends BiFunction<?, ?, ?>> BiFunctionType<X> cast()
    {
        return (BiFunctionType<X>)this;
    }
}
