package com.infamous.aptitude.common.goal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class GoalManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).create();

    public GoalManager() {
        super(GSON, "goals");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceToElement, ResourceManager resourceManager, ProfilerFiller profilerFiller) {

    }
}
