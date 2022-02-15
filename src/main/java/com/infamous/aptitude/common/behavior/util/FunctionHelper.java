package com.infamous.aptitude.common.behavior.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.infamous.aptitude.common.logic.functions.BiFunctionType;
import com.infamous.aptitude.common.logic.functions.BiFunctionTypes;
import com.infamous.aptitude.common.logic.functions.FunctionType;
import com.infamous.aptitude.common.logic.functions.FunctionTypes;
import com.infamous.aptitude.common.interaction.MobInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FunctionHelper {

    public static <T, R> Function<T, R> parseFunctionOrDefault(JsonObject jsonObject, String memberName, String typeMemberName, Function<T, R> defaultFunction){
        if(jsonObject.has(memberName)) return parseFunction(jsonObject, memberName, typeMemberName);
        return defaultFunction;
    }
    public static <T, R> Function<T, R> parseFunction(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject functionObj = GsonHelper.getAsJsonObject(jsonObject, memberName);
        FunctionType<?> predicateType = parseFunctionType(functionObj, typeMemberName);

        return (Function<T, R>) predicateType.fromJson(functionObj);
    }

    public static FunctionType<?> parseFunctionType(JsonObject jsonObject, String memberName){
        String functionTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation ftLocation = new ResourceLocation(functionTypeString);
        FunctionType<?> functionType = FunctionTypes.getFunctionType(ftLocation);
        if(functionType == null) throw new JsonParseException("Invalid function type: " + functionTypeString);
        return functionType;
    }

    public static <U, R> List<Function<U, R>> parseFunctions(JsonObject jsonObject, String functionsMemberName, String typeMemberName) {
        JsonArray functionsArray = GsonHelper.getAsJsonArray(jsonObject, functionsMemberName);
        List<Function<U, R>> functions = new ArrayList<>();
        functionsArray.forEach(jsonElement -> {
            JsonObject elemObj = jsonElement.getAsJsonObject();
            FunctionType<?> functionType = parseFunctionType(elemObj, typeMemberName);
            Function<U, R> function = (Function<U, R>) functionType.fromJson(elemObj);
            functions.add(function);
        });
        return functions;
    }

    public static <T, U, R> BiFunction<T, U, R> parseBiFunctionOrDefault(JsonObject jsonObject, String memberName, String typeMemberName, BiFunction<T, U, R> defaultBiFunction){
        if(jsonObject.has(memberName)) return parseBiFunction(jsonObject, memberName, typeMemberName);
        return defaultBiFunction;
    }

    public static <T, U, R> BiFunction<T, U, R> parseBiFunction(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject biFunctionObj = GsonHelper.getAsJsonObject(jsonObject, memberName);
        BiFunctionType<?> predicateType = parseBiFunctionType(biFunctionObj, typeMemberName);

        return (BiFunction<T, U, R>) predicateType.fromJson(biFunctionObj);
    }

    public static BiFunctionType<?> parseBiFunctionType(JsonObject jsonObject, String memberName){
        String biFunctionTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation ftLocation = new ResourceLocation(biFunctionTypeString);
        BiFunctionType<?> functionType = BiFunctionTypes.getBiFunctionType(ftLocation);
        if(functionType == null) throw new JsonParseException("Invalid function type: " + biFunctionTypeString);
        return functionType;
    }

    public static <T, R> Function<T, R> parseFunction(JsonObject jsonObject, String typeMemberName){
        FunctionType<?> predicateType = parseFunctionType(jsonObject, typeMemberName);
        return (Function<T, R>) predicateType.fromJson(jsonObject);
    }

    public static <T, U, R> BiFunction<T, U, R> parseBiFunction(JsonObject jsonObject, String typeMemberName){
        BiFunctionType<?> predicateType = parseBiFunctionType(jsonObject, typeMemberName);
        return (BiFunction<T, U, R>) predicateType.fromJson(jsonObject);
    }

}
