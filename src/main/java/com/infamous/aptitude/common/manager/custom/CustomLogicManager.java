package com.infamous.aptitude.common.manager.custom;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.infamous.aptitude.common.behavior.util.ConsumerHelper;
import com.infamous.aptitude.common.behavior.util.FunctionHelper;
import com.infamous.aptitude.common.behavior.util.PredicateHelper;
import com.infamous.aptitude.common.manager.selector.GoalSelectorContainer;
import com.infamous.aptitude.common.manager.selector.TargetSelectorContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.*;

public class CustomLogicManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String CONSUMERS_DIR = "consumers/";
    public static final String BICONSUMERS_DIR = "biconsumers/";
    public static final String PREDICATES_DIR = "predicates/";
    public static final String BIPREDICATES_DIR = "bipredicates/";
    public static final String FUNCTIONS_DIR = "functions/";
    public static final String BIFUNCTIONS_DIR = "bifunctions/";
    private Map<ResourceLocation, JsonObject> customConsumers = ImmutableMap.of();
    private Map<ResourceLocation, JsonObject> customBiConsumers = ImmutableMap.of();
    private Map<ResourceLocation, JsonObject> customPredicates = ImmutableMap.of();
    private Map<ResourceLocation, JsonObject> customBiPredicates = ImmutableMap.of();
    private Map<ResourceLocation, JsonObject> customFunctions = ImmutableMap.of();
    private Map<ResourceLocation, JsonObject> customBiFunctions = ImmutableMap.of();

    public CustomLogicManager() {
        super(GSON, "aptitude/custom");
    }

    public <T> Consumer<T> getConsumer(ResourceLocation location){
        JsonObject consumer = this.customConsumers.get(location);
        if(consumer == null) throw new JsonParseException("Could not find custom consumer " + location);
        return ConsumerHelper.parseConsumer(consumer, "type");
    }

    public <T, U> BiConsumer<T, U> getBiConsumer(ResourceLocation location){
        JsonObject biConsumer = this.customBiConsumers.get(location);
        if(biConsumer == null) throw new JsonParseException("Could not find custom biconsumer " + location);
        return ConsumerHelper.parseBiConsumer(biConsumer, "type");
    }

    public <T> Predicate<T> getPredicate(ResourceLocation location){
        JsonObject predicate = this.customPredicates.get(location);
        if(predicate == null) throw new JsonParseException("Could not find custom predicate " + location);
        return PredicateHelper.parsePredicate(predicate, "type");
    }

    public <T, U> BiPredicate<T, U> getBiPredicate(ResourceLocation location){
        JsonObject biPredicate = this.customBiPredicates.get(location);
        if(biPredicate == null) throw new JsonParseException("Could not find custom bipredicate " + location);
        return PredicateHelper.parseBiPredicate(biPredicate, "type");
    }

    public <T, R> Function<T, R> getFunction(ResourceLocation location){
        JsonObject function = this.customFunctions.get(location);
        if(function == null) throw new JsonParseException("Could not find custom function " + location);
        return FunctionHelper.parseFunction(function, "type");
    }

    public <T, U, R> BiFunction<T, U, R> getBiFunction(ResourceLocation location){
        JsonObject biFunction = this.customBiFunctions.get(location);
        if(biFunction == null) throw new JsonParseException("Could not find custom biFunction " + location);
        return FunctionHelper.parseBiFunction(biFunction, "type");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> locationElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder<ResourceLocation, JsonObject> customConsumersBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, JsonObject> customBiConsumersBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, JsonObject> customPredicatesBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, JsonObject> customBiPredicatesBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, JsonObject> customFunctionsBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, JsonObject> customBiFunctionsBuilder = ImmutableMap.builder();

        locationElementMap.forEach((location, jsonElement) -> {
            try{
                JsonObject topElement = GsonHelper.convertToJsonObject(jsonElement, "top element");
                if(location.getPath().startsWith(CONSUMERS_DIR)){
                    customConsumersBuilder.put(withoutDirectoryPrefix(location, CONSUMERS_DIR), topElement);
                } else if(location.getPath().startsWith(BICONSUMERS_DIR)){
                    customBiConsumersBuilder.put(withoutDirectoryPrefix(location, BICONSUMERS_DIR), topElement);
                } else if(location.getPath().startsWith(PREDICATES_DIR)){
                    customPredicatesBuilder.put(withoutDirectoryPrefix(location, PREDICATES_DIR), topElement);
                } else if(location.getPath().startsWith(BIPREDICATES_DIR)){
                    customBiPredicatesBuilder.put(withoutDirectoryPrefix(location, BIPREDICATES_DIR), topElement);
                } else if(location.getPath().startsWith(FUNCTIONS_DIR)){
                    customFunctionsBuilder.put(withoutDirectoryPrefix(location, FUNCTIONS_DIR), topElement);
                } else if(location.getPath().startsWith(BIFUNCTIONS_DIR)){
                    customBiFunctionsBuilder.put(withoutDirectoryPrefix(location, BIFUNCTIONS_DIR), topElement);
                } else{
                    LOGGER.info("Found unusable resource for custom logic: {}", location);
                }

            } catch (Exception exception) {
                LOGGER.error("Couldn't parse custom logic for {}", location, exception);
            }
        });

        this.customConsumers = customConsumersBuilder.build();
        this.customBiConsumers = customBiConsumersBuilder.build();
        this.customPredicates = customPredicatesBuilder.build();
        this.customBiPredicates = customBiPredicatesBuilder.build();
        this.customFunctions = customFunctionsBuilder.build();
        this.customBiFunctions = customBiFunctionsBuilder.build();
    }

    private static ResourceLocation withoutDirectoryPrefix(ResourceLocation location, String directory){
        String namespace = location.getNamespace();
        String path = location.getPath();
        String newPath = path.substring(directory.length());
        return new ResourceLocation(namespace, newPath);
    }
}
