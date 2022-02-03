package com.infamous.aptitude.common.behavior.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.infamous.aptitude.common.behavior.functions.FunctionType;
import com.infamous.aptitude.common.behavior.functions.FunctionTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FunctionHelper {
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
}
