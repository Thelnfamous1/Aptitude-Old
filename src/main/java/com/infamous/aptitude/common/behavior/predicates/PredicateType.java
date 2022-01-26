package com.infamous.aptitude.common.behavior.predicates;

import com.google.gson.JsonObject;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;
import java.util.function.Predicate;

public class PredicateType<U extends Predicate<?>> extends ForgeRegistryEntry<PredicateType<?>> {

    private final Function<JsonObject, U> jsonFactory;

    public PredicateType(Function<JsonObject, U> factoryIn) {
        this.jsonFactory = factoryIn;
    }

    public U fromJson(JsonObject jsonObject) {
        return this.jsonFactory.apply(jsonObject);
    }

    @SuppressWarnings("unchecked")
    public <X extends Predicate<?>> PredicateType<X> cast()
    {
        return (PredicateType<X>)this;
    }
}
