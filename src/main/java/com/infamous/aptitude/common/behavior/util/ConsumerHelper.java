package com.infamous.aptitude.common.behavior.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.infamous.aptitude.common.behavior.consumer.BiConsumerType;
import com.infamous.aptitude.common.behavior.consumer.BiConsumerTypes;
import com.infamous.aptitude.common.behavior.consumer.ConsumerType;
import com.infamous.aptitude.common.behavior.consumer.ConsumerTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ConsumerHelper {
    public static <U> Consumer<U> parseConsumer(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject consumerObj = GsonHelper.getAsJsonObject(jsonObject, memberName);
        return parseConsumer(consumerObj, typeMemberName);
    }

    public static <U> Consumer<U> parseConsumer(JsonObject jsonObject, String typeMemberName){
        ConsumerType<?> consumerType = parseConsumerType(jsonObject, typeMemberName);
        return (Consumer<U>) consumerType.fromJson(jsonObject);
    }

    public static ConsumerType<?> parseConsumerType(JsonObject jsonObject, String memberName){
        String consumerTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation ctLocation = new ResourceLocation(consumerTypeString);
        ConsumerType<?> consumerType = ConsumerTypes.getConsumerType(ctLocation);
        if(consumerType == null) throw new JsonParseException("Invalid consumer type: " + consumerTypeString);
        return consumerType;
    }

    public static <U> Consumer<U> parseConsumerOrDefault(JsonObject jsonObject, String memberName, String typeMemberName, Consumer<U> defaultConsumer) {
        if(jsonObject.has(memberName)) return parseConsumer(jsonObject, memberName, typeMemberName);
        return defaultConsumer;
    }


    public static <T, U> BiConsumer<T, U> parseBiConsumer(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject biConsumerObj = GsonHelper.getAsJsonObject(jsonObject, memberName);
        return parseBiConsumer(biConsumerObj, typeMemberName);
    }

    public static <T, U> BiConsumer<T, U> parseBiConsumer(JsonObject jsonObject, String typeMemberName){
        BiConsumerType<?> biConsumerType = parseBiConsumerType(jsonObject, typeMemberName);
        return (BiConsumer<T, U>) biConsumerType.fromJson(jsonObject);
    }

    public static BiConsumerType<?> parseBiConsumerType(JsonObject jsonObject, String memberName){
        String biConsumerTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation bctLocation = new ResourceLocation(biConsumerTypeString);
        BiConsumerType<?> biConsumerType = BiConsumerTypes.getBiConsumerType(bctLocation);
        if(biConsumerType == null) throw new JsonParseException("Invalid biconsumer type: " + biConsumerTypeString);
        return biConsumerType;
    }

    public static <T, U> BiConsumer<T, U> parseBiConsumerOrDefault(JsonObject jsonObject, String memberName, String typeMemberName, BiConsumer<T, U> defaultBiConsumer) {
        if(jsonObject.has(memberName)) return parseBiConsumer(jsonObject, memberName, typeMemberName);
        return defaultBiConsumer;
    }
}