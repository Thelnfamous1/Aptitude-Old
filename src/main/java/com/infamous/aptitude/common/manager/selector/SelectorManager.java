package com.infamous.aptitude.common.manager.selector;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class SelectorManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<ResourceLocation, GoalSelectorContainer> goalSelectorContainers = ImmutableMap.of();
    private Map<ResourceLocation, TargetSelectorContainer> targetSelectorContainers = ImmutableMap.of();

    public SelectorManager() {
        super(GSON, "aptitude/selector");
    }

    public boolean hasSelectorEntry(ResourceLocation location){
        return this.hasGoalSelectorEntry(location) || this.hasTargetSelectorEntry(location);
    }

    public boolean hasGoalSelectorEntry(ResourceLocation location){
        return this.goalSelectorContainers.containsKey(location);
    }

    public boolean hasTargetSelectorEntry(ResourceLocation location){
        return this.targetSelectorContainers.containsKey(location);
    }

    public boolean replaceGoalSelector(ResourceLocation location){
        return this.getGoalSelectorContainer(location).replaceSelector();
    }

    public boolean replaceTargetSelector(ResourceLocation location){
        return this.getTargetSelectorContainer(location).replaceSelector();
    }

    private GoalSelectorContainer getGoalSelectorContainer(ResourceLocation resourceLocation){
        return this.goalSelectorContainers.getOrDefault(resourceLocation, GoalSelectorContainer.EMPTY);
    }

    private TargetSelectorContainer getTargetSelectorContainer(ResourceLocation resourceLocation){
        return this.targetSelectorContainers.getOrDefault(resourceLocation, TargetSelectorContainer.EMPTY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> locationElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder<ResourceLocation, GoalSelectorContainer> goalSelectorContainerBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, TargetSelectorContainer> targetSelectorContainerBuilder = ImmutableMap.builder();

        locationElementMap.forEach((location, jsonElement) -> {
            try{
                JsonObject topElement = GsonHelper.convertToJsonObject(jsonElement, "top element");
                GoalSelectorContainer goalSelectorContainer = GoalSelectorContainer.of(topElement.getAsJsonObject("goal_selector"));
                goalSelectorContainerBuilder.put(location, goalSelectorContainer);
            } catch (Exception exception) {
                LOGGER.error("Couldn't parse goal selector for {}", location, exception);
            }

            try{
                JsonObject topElement = GsonHelper.convertToJsonObject(jsonElement, "top element");
                TargetSelectorContainer targetSelectorContainer = TargetSelectorContainer.of(topElement.getAsJsonObject("target_selector"));
                targetSelectorContainerBuilder.put(location, targetSelectorContainer);
            } catch (Exception exception) {
                LOGGER.error("Couldn't parse target selector for {}", location, exception);
            }
        });

        this.goalSelectorContainers = goalSelectorContainerBuilder.build();
        this.targetSelectorContainers = targetSelectorContainerBuilder.build();
    }
}
