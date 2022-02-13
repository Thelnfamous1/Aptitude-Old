package com.infamous.aptitude.common.manager.custom;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
    private Map<ResourceLocation, Consumer<?>> customConsumers = ImmutableMap.of();
    private Map<ResourceLocation, BiConsumer<?, ?>> customBiConsumers = ImmutableMap.of();
    private Map<ResourceLocation, Predicate<?>> customPredicates = ImmutableMap.of();
    private Map<ResourceLocation, BiPredicate<?, ?>> customBiPredicates = ImmutableMap.of();
    private Map<ResourceLocation, Function<?, ?>> customFunctions = ImmutableMap.of();
    private Map<ResourceLocation, BiFunction<?, ?, ?>> customBiFunctions = ImmutableMap.of();

    public CustomLogicManager() {
        super(GSON, "aptitude/custom");
    }

    public <T> Consumer<T> getConsumer(ResourceLocation location){
        return (Consumer<T>) this.customConsumers.get(location);
    }

    public <T, U> BiConsumer<T, U> getBiConsumer(ResourceLocation location){
        return (BiConsumer<T, U>) this.customBiConsumers.get(location);
    }

    public <T> Predicate<T> getPredicate(ResourceLocation location){
        return (Predicate<T>) this.customPredicates.get(location);
    }

    public <T, U> BiPredicate<T, U> getBiPredicate(ResourceLocation location){
        return (BiPredicate<T, U>) this.customBiPredicates.get(location);
    }

    public <T, R> Function<T, R> getFunction(ResourceLocation location){
        return (Function<T, R>) this.customConsumers.get(location);
    }

    public <T, U, R> BiFunction<T, U, R> getBiFunction(ResourceLocation location){
        return (BiFunction<T, U, R>) this.customBiFunctions.get(location);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> locationElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder<ResourceLocation, Consumer<?>> customConsumersBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, BiConsumer<?, ?>> customBiConsumersBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, Predicate<?>> customPredicatesBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, BiPredicate<?, ?>> customBiPredicatesBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, Function<?, ?>> customFunctionsBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, BiFunction<?, ?, ?>> customBiFunctionsBuilder = ImmutableMap.builder();

        locationElementMap.forEach((location, jsonElement) -> {
            try{
                JsonObject topElement = GsonHelper.convertToJsonObject(jsonElement, "top element");
                if(location.getPath().startsWith(CONSUMERS_DIR)){
                    Consumer<Object> consumer = ConsumerHelper.parseConsumer(topElement, "type");
                    customConsumersBuilder.put(withoutDirectoryPrefix(location, CONSUMERS_DIR), consumer);
                } else if(location.getPath().startsWith(BICONSUMERS_DIR)){
                    BiConsumer<Object, Object> biConsumer = ConsumerHelper.parseBiConsumer(topElement, "type");
                    customBiConsumersBuilder.put(withoutDirectoryPrefix(location, BICONSUMERS_DIR), biConsumer);
                } else if(location.getPath().startsWith(PREDICATES_DIR)){
                    Predicate<Object> predicate = PredicateHelper.parsePredicate(topElement, "type");
                    customPredicatesBuilder.put(withoutDirectoryPrefix(location, PREDICATES_DIR), predicate);
                } else if(location.getPath().startsWith(BIPREDICATES_DIR)){
                    BiPredicate<Object, Object> biPredicate = PredicateHelper.parseBiPredicate(topElement, "type");
                    customBiPredicatesBuilder.put(withoutDirectoryPrefix(location, BIPREDICATES_DIR), biPredicate);
                } else if(location.getPath().startsWith(FUNCTIONS_DIR)){
                    Function<Object, Object> function = FunctionHelper.parseFunction(topElement, "type");
                    customFunctionsBuilder.put(withoutDirectoryPrefix(location, FUNCTIONS_DIR), function);
                } else if(location.getPath().startsWith(BIFUNCTIONS_DIR)){
                    BiFunction<Object, Object, Object> biFunction = FunctionHelper.parseBiFunction(topElement, "type");
                    customBiFunctionsBuilder.put(withoutDirectoryPrefix(location, BIFUNCTIONS_DIR), biFunction);
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
