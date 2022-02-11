package com.infamous.aptitude.common.behavior.consumer;

import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import com.infamous.aptitude.common.behavior.util.ConsumerHelper;
import com.infamous.aptitude.common.behavior.util.PredicateHelper;
import com.infamous.aptitude.common.behavior.util.RangedAttackHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.*;

public class BiConsumerTypes {

    private static final DeferredRegister<BiConsumerType<?>> BICONSUMER_TYPES = DeferredRegister.create((Class<BiConsumerType<?>>)(Class)BiConsumerType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<BiConsumerType<?>>> BICONSUMER_TYPE_REGISTRY = BICONSUMER_TYPES.makeRegistry("biconsumer_types", () ->
            new RegistryBuilder<BiConsumerType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("BiConsumerType Added: " + obj.getRegistryName().toString() + " ")
            )
    );

    public static final RegistryObject<BiConsumerType<BiConsumer<?, ?>>> NOTHING = register("nothing",
            jsonObject -> {
                return (o, o1) -> {};
            });


    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, Boolean>>> ENTITY_SET_CHARGING_CROSSBOW = register("entity_set_charging_crossbow",
            jsonObject -> {
                return (le, b) -> {
                    if(le instanceof CrossbowAttackMob crossbowAttackMob){
                        crossbowAttackMob.setChargingCrossbow(b);
                    }
                };
            });


    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, LivingEntity>>> ENTITY_PERFORM_CROSSBOW_ATTACK = register("entity_perform_crossbow_attack",
            jsonObject -> {
                float shootingPower = GsonHelper.getAsFloat(jsonObject, "shooting_power", 1.6F);
                int baseInaccuracy = GsonHelper.getAsInt(jsonObject, "base_inaccuracy", 14);
                int difficultyScale = GsonHelper.getAsInt(jsonObject, "difficulty_scale", 4);
                return (shooter, target) -> {
                    InteractionHand weaponHoldingHand = ProjectileUtil.getWeaponHoldingHand(shooter, item -> item instanceof CrossbowItem);
                    ItemStack itemInHand = shooter.getItemInHand(weaponHoldingHand);
                    if (shooter.isHolding(is -> is.getItem() instanceof CrossbowItem)) {
                        RangedAttackHelper.peformCrossbowShooting(shooter.level, shooter, target, weaponHoldingHand, itemInHand, shootingPower, (float)(baseInaccuracy - shooter.level.getDifficulty().getId() * difficultyScale));
                    }

                    shooter.setNoActionTime(0); // onCrossbowAttackPerformed
                };
            });


    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, ItemEntity>>> ENTITY_PICK_UP_WANTED_ITEM = register("entity_pick_up_wanted_item",
            jsonObject -> {
                Predicate<ItemStack> takesAll = PredicateHelper.parsePredicateOrDefault(jsonObject, "takes_all", "type", stack -> false);

                Predicate<ItemStack> lovedItems = PredicateHelper.parsePredicateOrDefault(jsonObject, "loved_items", "type", stack -> false);
                MemoryModuleType<Integer> timeTryingToReachAdmireItem = BehaviorHelper.parseMemoryType(jsonObject, "time_trying_to_reach_wanted_item_memory");
                MemoryModuleType<Boolean> admiringItem = BehaviorHelper.parseMemoryType(jsonObject, "handling_item_memory");
                long admiringItemTime = GsonHelper.getAsLong(jsonObject, "handling_item_time", 120L);

                Predicate<ItemStack> foodItems = PredicateHelper.parsePredicateOrDefault(jsonObject, "food_items", "type", stack -> false);
                MemoryModuleType<Boolean> ateRecently = BehaviorHelper.parseMemoryType(jsonObject, "consumed_item_memory");
                long ateRecentlyTime = GsonHelper.getAsLong(jsonObject, "consumed_item_time", 200L);

                return (le, ie) -> {
                    le.onItemPickup(ie);
                    BehaviorHelper.stopWalking(le);
                    ItemStack pickedUp;
                    if (takesAll.test(ie.getItem())) {
                        le.take(ie, ie.getItem().getCount());
                        pickedUp = ie.getItem();
                        ie.discard();
                    } else {
                        le.take(ie, 1);
                        pickedUp = BehaviorHelper.removeOneItemFromItemEntity(ie);
                    }

                    if (lovedItems.test(pickedUp)) {
                        le.getBrain().eraseMemory(timeTryingToReachAdmireItem);
                        BehaviorHelper.holdInOffhand(le, pickedUp);
                        le.getBrain().setMemoryWithExpiry(admiringItem, true, admiringItemTime);
                    } else {
                        if (foodItems.test(pickedUp) && !le.getBrain().hasMemoryValue(ateRecently)) {
                            le.getBrain().setMemoryWithExpiry(ateRecently, true, ateRecentlyTime);
                        } else {
                            boolean equippedItem = le instanceof Mob mob && mob.equipItemIfPossible(pickedUp);
                            if (!equippedItem) {
                                BehaviorHelper.putInInventory(le, pickedUp);
                            }
                        }
                    }
                };
            });


    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, ItemEntity>>> ENTITY_PICK_UP_EQUIPMENT = register("entity_pick_up_equipment",
            jsonObject -> {
                return (le, ie) -> {
                    ItemStack itemstack = ie.getItem();
                    if (le instanceof Mob mob && mob.equipItemIfPossible(itemstack)) {
                        le.onItemPickup(ie);
                        le.take(ie, itemstack.getCount());
                        ie.discard();
                    }
                };
            });

    public static final RegistryObject<BiConsumerType<BiConsumer<?, ?>>> BICONSUMER_OF_CONSUMERS = register("biconsumer_of_consumer",
            jsonObject -> {
                Consumer<Object> consumerForFirst = ConsumerHelper.parseConsumerOrDefault(jsonObject, "consumer_for_first", "type", o -> {});
                Consumer<Object> consumerForSecond = ConsumerHelper.parseConsumerOrDefault(jsonObject, "consumer_for_second", "type", o -> {});

                return (o, o1) -> {
                    consumerForFirst.accept(o);
                    consumerForSecond.accept(o1);
                };
            });


    private static <U extends BiConsumer<?, ?>> RegistryObject<BiConsumerType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return BICONSUMER_TYPES.register(name, () -> new BiConsumerType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        BICONSUMER_TYPES.register(bus);
    }

    public static BiConsumerType<?> getBiConsumerType(ResourceLocation bctLocation) {
        BiConsumerType<?> value = BICONSUMER_TYPE_REGISTRY.get().getValue(bctLocation);
        if(value == null) Aptitude.LOGGER.error("Failed to get BiConsumerType {}", bctLocation);
        return value;
    }
}
