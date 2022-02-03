package com.infamous.aptitude.common.behavior.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.infamous.aptitude.common.behavior.consumer.ConsumerType;
import com.infamous.aptitude.common.behavior.consumer.ConsumerTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.function.Consumer;
import java.util.function.Predicate;

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
}
