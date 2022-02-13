package com.infamous.aptitude.common.behavior.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.infamous.aptitude.common.behavior.predicates.BiPredicateType;
import com.infamous.aptitude.common.behavior.predicates.BiPredicateTypes;
import com.infamous.aptitude.common.behavior.predicates.PredicateType;
import com.infamous.aptitude.common.behavior.predicates.PredicateTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class PredicateHelper {
    public static <U> Predicate<U> parsePredicate(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject predicateObj = GsonHelper.getAsJsonObject(jsonObject, memberName);
        PredicateType<?> predicateType = parsePredicateType(predicateObj, typeMemberName);

        return (Predicate<U>) predicateType.fromJson(predicateObj);
    }

    public static PredicateType<?> parsePredicateType(JsonObject jsonObject, String memberName){
        String predicateTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation ptLocation = new ResourceLocation(predicateTypeString);
        PredicateType<?> predicateType = PredicateTypes.getPredicateType(ptLocation);
        if(predicateType == null) throw new JsonParseException("Invalid predicate type: " + predicateTypeString);
        return predicateType;
    }

    public static <U> List<Predicate<U>> parsePredicates(JsonObject jsonObject, String predicatesMemberName, String typeMemberName) {
        JsonArray predicatesArray = GsonHelper.getAsJsonArray(jsonObject, predicatesMemberName);
        List<Predicate<U>> predicates = new ArrayList<>();
        predicatesArray.forEach(jsonElement -> {
            JsonObject elemObj = jsonElement.getAsJsonObject();
            PredicateType<?> predicateType = parsePredicateType(elemObj, typeMemberName);
            Predicate<U> predicate = (Predicate<U>) predicateType.fromJson(elemObj);
            predicates.add(predicate);
        });
        return predicates;
    }

    public static <T, U> List<BiPredicate<T, U>> parseBiPredicates(JsonObject jsonObject, String predicatesMemberName, String typeMemberName) {
        JsonArray biPredicatesArray = GsonHelper.getAsJsonArray(jsonObject, predicatesMemberName);
        List<BiPredicate<T, U>> biPredicates = new ArrayList<>();
        biPredicatesArray.forEach(jsonElement -> {
            JsonObject elemObj = jsonElement.getAsJsonObject();
            BiPredicateType<?> biPredicateType = parseBiPredicateType(elemObj, typeMemberName);
            BiPredicate<T, U> biPredicate = (BiPredicate<T, U>) biPredicateType.fromJson(elemObj);
            biPredicates.add(biPredicate);
        });
        return biPredicates;
    }

    public static <T, U> BiPredicate<T, U> parseBiPredicate(JsonObject jsonObject, String memberName, String typeMemberName) {
        JsonObject predicateObj = GsonHelper.getAsJsonObject(jsonObject, memberName);
        BiPredicateType<?> biPredicateType = parseBiPredicateType(predicateObj, typeMemberName);

        return (BiPredicate<T, U>) biPredicateType.fromJson(predicateObj);
    }

    public static BiPredicateType<?> parseBiPredicateType(JsonObject jsonObject, String memberName){
        String biPredicateTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        return parseBiPredicateTypeString(biPredicateTypeString);
    }

    public static BiPredicateType<?> parseBiPredicateTypeString(String biPredicateTypeString) {
        ResourceLocation bptLocation = new ResourceLocation(biPredicateTypeString);
        BiPredicateType<?> biPredicateType = BiPredicateTypes.getBiPredicateType(bptLocation);
        if(biPredicateType == null) throw new JsonParseException("Invalid bipredicate type: " + biPredicateTypeString);
        return biPredicateType;
    }

    public static <U> Predicate<U> parsePredicateOrDefault(JsonObject jsonObject, String memberName, String typeMemberName, Predicate<U> defaultPredicate) {
        if(jsonObject.has(memberName)) return parsePredicate(jsonObject, memberName, typeMemberName);
        return defaultPredicate;
    }

    public static <T, U> BiPredicate<T, U> parseBiPredicateOrDefault(JsonObject jsonObject, String memberName, String typeMemberName, BiPredicate<T, U> defaultPredicate) {
        if(jsonObject.has(memberName)) return parseBiPredicate(jsonObject, memberName, typeMemberName);
        return defaultPredicate;
    }

    public static <U> Predicate<U> parsePredicate(JsonObject jsonObject, String typeMemberName) {
        PredicateType<?> predicateType = parsePredicateType(jsonObject, typeMemberName);
        return (Predicate<U>) predicateType.fromJson(jsonObject);
    }

    public static <T, U> BiPredicate<T, U> parseBiPredicate(JsonObject jsonObject, String typeMemberName) {
        BiPredicateType<?> biPredicateType = parseBiPredicateType(jsonObject, typeMemberName);
        return (BiPredicate<T, U>) biPredicateType.fromJson(jsonObject);
    }
}
