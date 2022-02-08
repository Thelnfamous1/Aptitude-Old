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
    private Consumer<LivingEntity> finalizeSpawn = le -> {};
    private BiPredicate<LivingEntity, ItemStack> wantsToPickUp = ((le, stack) -> false);
    private BiConsumer<LivingEntity, ItemEntity> pickUpItem = (le, itemEntity) -> {};

    public static final BaseAIContainer EMPTY =  new BaseAIContainer();

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

        baseAIContainer.finalizeSpawn = ConsumerHelper.parseConsumer(jsonObject, "finalize_spawn", "type");
        baseAIContainer.wantsToPickUp = PredicateHelper.parseBiPredicate(jsonObject, "wants_to_pick_up", "type");
        baseAIContainer.pickUpItem = ConsumerHelper.parseBiConsumer(jsonObject, "pick_up_item", "type");

        return baseAIContainer;
    }
}
