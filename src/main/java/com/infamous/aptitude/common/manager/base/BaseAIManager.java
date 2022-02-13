package com.infamous.aptitude.common.manager.base;

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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class BaseAIManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<ResourceLocation, BaseAIContainer> baseAIContainers = ImmutableMap.of();

    public BaseAIManager() {
        super(GSON, "aptitude/base");
    }

    public BiConsumer<LivingEntity, ItemEntity> pickUpItem(ResourceLocation location) {
        return this.getBaseAIContainer(location).getPickUpItem();
    }

    public BiPredicate<LivingEntity, ItemStack> wantsToPickUp(ResourceLocation location) {
        return this.getBaseAIContainer(location).getWantsToPickUp();
    }

    public Consumer<LivingEntity> addedToWorld(ResourceLocation location) {
        return this.getBaseAIContainer(location).getAddedToWorld();
    }

    public Consumer<LivingEntity> firstSpawn(ResourceLocation location){
        return this.getBaseAIContainer(location).getFirstSpawn();
    }

    public BiConsumer<LivingEntity, LivingEntity> attackedBy(ResourceLocation location) {
        return this.getBaseAIContainer(location).getAttackedBy();
    }

    public BiConsumer<LivingEntity, LivingEntity> attacked(ResourceLocation location) {
        return this.getBaseAIContainer(location).getAttacked();
    }

    private BaseAIContainer getBaseAIContainer(ResourceLocation location){
        return this.baseAIContainers.getOrDefault(location, BaseAIContainer.EMPTY);
    }

    public boolean hasBaseAIEntry(ResourceLocation location){
        return this.baseAIContainers.containsKey(location);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> locationElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder<ResourceLocation, BaseAIContainer> stateContainerBuilder = ImmutableMap.builder();
        locationElementMap.forEach((location, jsonElement) -> {
            try {
                JsonObject topElement = GsonHelper.convertToJsonObject(jsonElement, "top element");
                BaseAIContainer baseAIContainer = BaseAIContainer.of(topElement);
                stateContainerBuilder.put(location, baseAIContainer);

            } catch (Exception exception) {
                LOGGER.error("Couldn't parse base AI for {}", location, exception);
            }
        });
        this.baseAIContainers = stateContainerBuilder.build();
    }
}
