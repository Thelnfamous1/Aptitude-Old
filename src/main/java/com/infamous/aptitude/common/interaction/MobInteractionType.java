package com.infamous.aptitude.common.interaction;

import com.google.gson.JsonObject;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;

public class MobInteractionType extends ForgeRegistryEntry<MobInteractionType> {

    private final Function<JsonObject, MobInteraction> jsonFactory;

    public MobInteractionType(Function<JsonObject, MobInteraction> factoryIn) {
        this.jsonFactory = factoryIn;
    }

    public MobInteraction fromJson(JsonObject jsonObject) {
        MobInteraction.WrapMode wrapMode = MobInteractionHelper.parseWrapMode(jsonObject, "wrap_mode");
        MobInteraction mobInteraction = this.jsonFactory.apply(jsonObject);
        return MobInteractionHelper.wrap(mobInteraction, wrapMode);
    }
}
