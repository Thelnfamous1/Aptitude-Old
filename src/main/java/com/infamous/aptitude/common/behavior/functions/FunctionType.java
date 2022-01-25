package com.infamous.aptitude.common.behavior.functions;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class FunctionType<U extends Function<?, ?>> extends ForgeRegistryEntry<FunctionType<?>> {

    private final Function<JsonObject, U> jsonFactory;

    public FunctionType(Function<JsonObject, U> factoryIn) {
        this.jsonFactory = factoryIn;
    }

    public U fromJson(JsonObject jsonObject) {
        return this.jsonFactory.apply(jsonObject);
    }
}
