package com.infamous.aptitude.common.logic.predicates;

import com.google.gson.JsonObject;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class BiPredicateType<U extends BiPredicate<?, ?>> extends ForgeRegistryEntry<BiPredicateType<?>> {

    private final Function<JsonObject, U> jsonFactory;

    public BiPredicateType(Function<JsonObject, U> factoryIn) {
        this.jsonFactory = factoryIn;
    }

    public U fromJson(JsonObject jsonObject) {
        return this.jsonFactory.apply(jsonObject);
    }

    @SuppressWarnings("unchecked")
    public <X extends BiPredicate<?, ?>> BiPredicateType<X> cast()
    {
        return (BiPredicateType<X>)this;
    }
}
