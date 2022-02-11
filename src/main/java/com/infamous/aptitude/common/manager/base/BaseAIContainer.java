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
    public static final BaseAIContainer EMPTY =  new BaseAIContainer();

    private Consumer<LivingEntity> addedToWorld = le -> {};
    private Consumer<LivingEntity> firstSpawn = le -> {};
    private BiPredicate<LivingEntity, ItemStack> wantsToPickUp = (le, is) -> false;
    private BiConsumer<LivingEntity, ItemEntity> pickUpItem = (le, ie) -> {};
    private BiConsumer<LivingEntity, LivingEntity> attackedBy = (victim, attacker) -> {};

    public Consumer<LivingEntity> getAddedToWorld() {
        return addedToWorld;
    }

    public Consumer<LivingEntity> getFirstSpawn() {
        return firstSpawn;
    }

    public BiConsumer<LivingEntity, LivingEntity> getAttackedBy() {
        return attackedBy;
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
        baseAIContainer.firstSpawn = ConsumerHelper.parseConsumerOrDefault(jsonObject, "first_spawn", "type", le -> {});
        baseAIContainer.attackedBy = ConsumerHelper.parseBiConsumerOrDefault(jsonObject, "attacked_by", "type", (victim, attacker) -> {});
        baseAIContainer.wantsToPickUp = PredicateHelper.parseBiPredicateOrDefault(jsonObject, "wants_to_pick_up", "type", (le, is) -> false);
        baseAIContainer.pickUpItem = ConsumerHelper.parseBiConsumerOrDefault(jsonObject, "pick_up_item", "type", (le, ie) -> {});

        return baseAIContainer;
    }
}
