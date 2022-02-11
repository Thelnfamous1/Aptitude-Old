package com.infamous.aptitude.common.manager.base;

import com.google.gson.JsonObject;
import com.infamous.aptitude.common.behavior.util.ConsumerHelper;
import com.infamous.aptitude.common.behavior.util.PredicateHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class BaseAIContainer {
    private Consumer<LivingEntity> addedToWorld = le -> {};
    private Consumer<LivingEntity> finalizeSpawn = le -> {};
    private BiPredicate<LivingEntity, ItemStack> wantsToPickUp = ((le, stack) -> false);
    private BiConsumer<LivingEntity, ItemEntity> pickUpItem = (le, itemEntity) -> {};

    public static final BaseAIContainer EMPTY =  new BaseAIContainer();

    public Consumer<LivingEntity> getAddedToWorld() {
        return addedToWorld;
    }

    public Consumer<LivingEntity> getFinalizeSpawn() {
        return finalizeSpawn;
    }

    public BiPredicate<LivingEntity, ItemStack> getWantsToPickUp() {
        return wantsToPickUp;
    }

    public BiConsumer<LivingEntity, ItemEntity> getPickUpItem() {
        return pickUpItem;
    }

    private BaseAIContainer(){

    }

    public static BaseAIContainer of(JsonObject jsonObject){
        BaseAIContainer baseAIContainer = new BaseAIContainer();

        baseAIContainer.addedToWorld = ConsumerHelper.parseConsumerOrDefault(jsonObject, "added_to_world", "type", le -> {});
        baseAIContainer.finalizeSpawn = ConsumerHelper.parseConsumerOrDefault(jsonObject, "finalize_spawn", "type", le -> {});
        baseAIContainer.wantsToPickUp = PredicateHelper.parseBiPredicateOrDefault(jsonObject, "wants_to_pick_up", "type", (livingEntity, stack) -> false);
        baseAIContainer.pickUpItem = ConsumerHelper.parseBiConsumerOrDefault(jsonObject, "pick_up_item", "type", ((livingEntity, itemEntity) -> {}));

        return baseAIContainer;
    }
}
