package com.infamous.aptitude.common.interaction;

import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import com.infamous.aptitude.common.behavior.util.ConsumerHelper;
import com.infamous.aptitude.common.behavior.util.PredicateHelper;
import com.infamous.aptitude.common.util.ReflectionHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.*;

public class MobInteractionTypes {

    private static final DeferredRegister<MobInteractionType> MOB_INTERACTION_TYPES = DeferredRegister.create(MobInteractionType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<MobInteractionType>> MOB_INTERACTION_TYPE_REGISTRY = MOB_INTERACTION_TYPES.makeRegistry("mob_interaction_types", () ->
            new RegistryBuilder<MobInteractionType>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("MobInteractionType Added: " + obj.getRegistryName().toString() + " ")
            )
    );

    public static final RegistryObject<MobInteractionType> DEFAULT_INTERACTION = register("default_interaction",
            jsonObject -> {
                return MobInteraction.DEFAULT;
            });

    public static final RegistryObject<MobInteractionType> CONSUMER_ON_INTERACTION = register("consumer_on_interaction",
            jsonObject -> {
                Consumer<LivingEntity> consumer = ConsumerHelper.parseConsumer(jsonObject, "consumer", "type");
                MobInteraction interaction = MobInteractionHelper.parseMobInteractionOrDefault(jsonObject, "interaction", "type", MobInteraction.DEFAULT);

                return (mob, player, hand) -> {
                    InteractionResult interactionResult = interaction.interact(mob, player, hand);
                    if (interactionResult.consumesAction()) {
                        consumer.accept(mob);
                    }

                    return interactionResult;
                };
            });

    public static final RegistryObject<MobInteractionType> SIDED_INTERACTION = register("sided_interaction",
            jsonObject -> {
                MobInteraction defaultMobInteraction = MobInteractionHelper.parseMobInteractionOrDefault(jsonObject, "default", "type", MobInteraction.DEFAULT);
                MobInteraction serverSideInteraction = MobInteractionHelper.parseMobInteraction(jsonObject, "server_side", "type");
                MobInteraction clientSideInteraction = MobInteractionHelper.parseMobInteraction(jsonObject, "client_side", "type");

                return (mob, player, hand) -> {
                    InteractionResult interactionResult = defaultMobInteraction.interact(mob, player, hand);
                    if (interactionResult.consumesAction()) {
                        return interactionResult;
                    } else if (!mob.level.isClientSide) {
                        return serverSideInteraction.interact(mob, player, hand);
                    } else {
                        return clientSideInteraction.interact(mob, player, hand);
                    }
                };
            });


    public static final RegistryObject<MobInteractionType> ADMIRE_INTERACTION_SERVER = register("admire_interaction_server",
            jsonObject -> {
                BiPredicate<LivingEntity, ItemStack> canAdmire = PredicateHelper.parseBiPredicate(jsonObject, "can_admire", "type");
                Consumer<LivingEntity> admire = ConsumerHelper.parseConsumer(jsonObject, "admire", "type");

                return (mob, player, hand) -> {
                    ItemStack itemInHand = player.getItemInHand(hand);
                    if (canAdmire.test(mob, itemInHand)) {
                        ItemStack singleton = itemInHand.split(1);
                        BehaviorHelper.holdInOffhand(mob, singleton);
                        admire.accept(mob);
                        BehaviorHelper.stopWalking(mob);
                        return InteractionResult.CONSUME;
                    } else {
                        return InteractionResult.PASS;
                    }
                };
            });


    public static final RegistryObject<MobInteractionType> ADMIRE_INTERACTION_CLIENT = register("admire_interaction_client",
            jsonObject -> {
                BiPredicate<LivingEntity, ItemStack> canAdmire = PredicateHelper.parseBiPredicate(jsonObject, "can_admire", "type");
                Predicate<LivingEntity> isAdmiring = PredicateHelper.parsePredicate(jsonObject, "is_admiring", "type");
                return (mob, player, hand) -> {
                    boolean wantsToAdmire = canAdmire.test(mob, player.getItemInHand(hand)) && !isAdmiring.test(mob);
                    return wantsToAdmire ? InteractionResult.SUCCESS : InteractionResult.PASS;
                };
            });


    public static final RegistryObject<MobInteractionType> ANIMAL_INTERACTION = register("animal_interaction",
            jsonObject -> {
                Predicate<ItemStack> isFood = PredicateHelper.parsePredicate(jsonObject, "is_food", "type");
                MobInteraction defaultMobInteraction = MobInteractionHelper.parseMobInteractionOrDefault(jsonObject, "default", "type", MobInteraction.DEFAULT);

                return (mob, player, hand) -> {
                    if(mob instanceof Animal animal){
                        ItemStack itemInHand = player.getItemInHand(hand);
                        if (isFood.test(itemInHand)) {
                            int age = animal.getAge();
                            if (!animal.level.isClientSide && age == 0 && animal.canFallInLove()) {
                                ReflectionHelper.reflectAnimalUsePlayerItem(animal, player, hand, itemInHand);
                                animal.setInLove(player);
                                animal.gameEvent(GameEvent.MOB_INTERACT, animal.eyeBlockPosition());
                                return InteractionResult.SUCCESS;
                            }

                            if (animal.isBaby()) {
                                ReflectionHelper.reflectAnimalUsePlayerItem(animal, player, hand, itemInHand);
                                animal.ageUp((int)((float)(-age / 20) * 0.1F), true);
                                animal.gameEvent(GameEvent.MOB_INTERACT, animal.eyeBlockPosition());
                                return InteractionResult.sidedSuccess(animal.level.isClientSide);
                            }

                            if (animal.level.isClientSide) {
                                return InteractionResult.CONSUME;
                            }
                        }
                    }

                    return defaultMobInteraction.interact(mob, player, hand);
                };
            });


    public static final RegistryObject<MobInteractionType> MILK_INTERACTION = register("milk_interaction",
            jsonObject -> {
                MobInteraction defaultMobInteraction = MobInteractionHelper.parseMobInteractionOrDefault(jsonObject, "default", "type", MobInteraction.DEFAULT);
                SoundEvent soundEvent = BehaviorHelper.parseSoundEventString(jsonObject, "milking_sound");
                Predicate<ItemStack> canBeFilled = PredicateHelper.parsePredicateOrDefault(jsonObject, "can_be_filled", "type", is -> is.is(Items.BUCKET));
                Predicate<LivingEntity> canBeMilked = PredicateHelper.parsePredicateOrDefault(jsonObject, "can_be_milked", "type", le -> !le.isBaby());
                ItemStack filled = BehaviorHelper.parseItemStackOrDefault(jsonObject, "filled", Items.MILK_BUCKET.getDefaultInstance());

                return (mob, player, hand) -> {
                    ItemStack itemInHand = player.getItemInHand(hand);
                    if (canBeFilled.test(itemInHand) && canBeMilked.test(mob)) {
                        player.playSound(soundEvent, 1.0F, 1.0F);
                        ItemStack filledResult = ItemUtils.createFilledResult(itemInHand, player, filled);
                        player.setItemInHand(hand, filledResult);
                        return InteractionResult.sidedSuccess(mob.level.isClientSide);
                    } else {
                        return defaultMobInteraction.interact(mob, player, hand);
                    }
                };
            });

    private static RegistryObject<MobInteractionType> register(String name, Function<JsonObject, MobInteraction> jsonFactory) {
        return MOB_INTERACTION_TYPES.register(name, () -> new MobInteractionType(jsonFactory));
    }

    public static void register(IEventBus bus){
        MOB_INTERACTION_TYPES.register(bus);
    }

    public static MobInteractionType getMobInteractionType(ResourceLocation location) {
        MobInteractionType value = MOB_INTERACTION_TYPE_REGISTRY.get().getValue(location);
        if(value == null) Aptitude.LOGGER.error("Failed to get MobInteractionType {}", location);
        return value;
    }
}
