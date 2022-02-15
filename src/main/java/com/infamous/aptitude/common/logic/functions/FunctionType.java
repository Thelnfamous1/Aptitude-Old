package com.infamous.aptitude.common.logic.functions;

import com.google.gson.JsonObject;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;

public class FunctionType<U extends Function<?, ?>> extends ForgeRegistryEntry<FunctionType<?>> {

    private final Function<JsonObject, U> jsonFactory;

    public FunctionType(Function<JsonObject, U> factoryIn) {
        this.jsonFactory = factoryIn;
    }

    public U fromJson(JsonObject jsonObject) {
        return this.jsonFactory.apply(jsonObject);
    }

    @SuppressWarnings("unchecked")
    public <X extends Function<?, ?>> FunctionType<X> cast()
    {
        return (FunctionType<X>)this;
    }
}
