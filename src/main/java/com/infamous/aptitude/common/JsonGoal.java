package com.infamous.aptitude.common;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.function.Consumer;
import java.util.function.Function;

public class JsonGoal<T extends Mob> extends Goal {
    private final T mob;
    private final ResourceLocation location;
    private int priority = 0;
    private boolean interruptable = true;
    private boolean requiresUpdateEveryTick = false;
    private JsonGoal<T> parent;
    private Function<T, Boolean> canUse = m -> false;
    private Consumer<T> start = m -> {};
    private Consumer<T> tick = m -> {};
    private Function<T, Boolean> canContinueToUse = m -> false;
    private Consumer<T> stop = m -> {};

    public JsonGoal(T mob, ResourceLocation location){
        this.mob = mob;
        this.location = location;
    }

    public static <T extends Mob> JsonGoal<T> fromJson(T mob, ResourceLocation location, JsonObject jsonObject){
        JsonGoal<T> goal = new JsonGoal<>(mob, location);
        goal.priority = GsonHelper.getAsInt(jsonObject, "priority", 0);
        goal.interruptable = GsonHelper.getAsBoolean(jsonObject, "interruptable", true);
        goal.requiresUpdateEveryTick = GsonHelper.getAsBoolean(jsonObject, "requiresUpdateEveryTick", false);
        String parentPath = GsonHelper.getAsString(jsonObject, "parent", "");
        ResourceLocation parentLocation = ResourceLocation.tryParse(parentPath);
        goal.parent = fromJson(mob, parentLocation, new JsonObject());
        goal.canUse = funcFromJson(GsonHelper.getAsJsonObject(jsonObject, "canUse"));
        goal.start = consumerFromJson(GsonHelper.getAsJsonObject(jsonObject, "start"));
        goal.tick = consumerFromJson(GsonHelper.getAsJsonObject(jsonObject, "tick"));
        goal.canContinueToUse = funcFromJson(GsonHelper.getAsJsonObject(jsonObject, "canContinueToUse"));
        goal.stop = consumerFromJson(GsonHelper.getAsJsonObject(jsonObject, "stop"));
        return goal;
    }

    public static <T extends Mob> Consumer<T> consumerFromJson(JsonObject jsonObject){
        return m -> {};
    }

    public static <T extends Mob> Function<T, Boolean> funcFromJson(JsonObject jsonObject){
        return m -> false;
    }

    public ResourceLocation getLocation(){
        return this.location;
    }

    public int getPriority(){
        return this.priority;
    }

    @Override
    public boolean isInterruptable() {
        return this.interruptable;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return this.requiresUpdateEveryTick;
    }

    @Override
    public boolean canUse() {
        return this.canUse.apply(this.mob);
    }


    @Override
    public void start() {
        this.start.accept(this.mob);
    }

    @Override
    public void tick() {
        this.tick.accept(this.mob);
    }

    @Override
    public boolean canContinueToUse() {
        return this.canContinueToUse.apply(this.mob);
    }

    @Override
    public void stop() {
        this.stop.accept(this.mob);
    }
}
