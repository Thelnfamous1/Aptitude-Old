package com.infamous.aptitude.common.interaction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.infamous.aptitude.common.util.ReflectionHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;

public class MobInteractionHelper {

    public static MobInteraction withAllWrapping(MobInteraction mobInteraction) {
        return withPostWrapping(withPostWrapping(mobInteraction));
    }

    public static MobInteraction withPreWrapping(MobInteraction mobInteraction) {
        return (mob, player, hand) -> {
            if (!mob.isAlive()) {
                return InteractionResult.PASS;
            } else if (mob.getLeashHolder() == player) {
                mob.dropLeash(true, !player.getAbilities().instabuild);
                return InteractionResult.sidedSuccess(mob.level.isClientSide);
            } else {
                InteractionResult interactResult = ReflectionHelper.reflectMobCheckAndHandleImportantInteractions(mob, player, hand);
                if (interactResult.consumesAction()) {
                    return interactResult;
                } else {
                    interactResult = mobInteraction.interact(mob, player, hand);
                    return interactResult.consumesAction() ? interactResult : InteractionResult.PASS;
                }
            }
        };
    }

    public static MobInteraction withPostWrapping(MobInteraction mobInteraction){
        return (mob, player, hand) -> {
            ItemStack itemInHand = player.getItemInHand(hand);
            ItemStack itemInHandCopy = itemInHand.copy();
            InteractionResult interactResult = mobInteraction.interact(mob, player, hand);
            if (interactResult.consumesAction()) {
                if (player.getAbilities().instabuild && itemInHand == player.getItemInHand(hand) && itemInHand.getCount() < itemInHandCopy.getCount()) {
                    itemInHand.setCount(itemInHandCopy.getCount());
                }

                if (!player.getAbilities().instabuild && itemInHand.isEmpty()) {
                    ForgeEventFactory.onPlayerDestroyItem(player, itemInHandCopy, hand);
                }
                return interactResult;
            } else {
                if (!itemInHand.isEmpty()) {
                    if (player.getAbilities().instabuild) {
                        itemInHand = itemInHandCopy;
                    }

                    InteractionResult ileResult = itemInHand.interactLivingEntity(player, mob, hand);
                    if (ileResult.consumesAction()) {
                        if (itemInHand.isEmpty() && !player.getAbilities().instabuild) {
                            ForgeEventFactory.onPlayerDestroyItem(player, itemInHandCopy, hand);
                            player.setItemInHand(hand, ItemStack.EMPTY);
                        }

                        return ileResult;
                    }
                }

                return InteractionResult.PASS;
            }
        };

    }

    public static MobInteraction wrap(MobInteraction mobInteraction, MobInteraction.WrapMode wrapMode){
        switch (wrapMode){
            case ALL -> {
                return withAllWrapping(mobInteraction);
            }
            case PRE_ONLY -> {
                return withPreWrapping(mobInteraction);
            }
            case POST_ONLY -> {
                return withPostWrapping(mobInteraction);
            }
            default -> {
                return mobInteraction;
            }
        }
    }

    public static MobInteraction parseMobInteraction(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject mobInteractionObj = jsonObject.getAsJsonObject(memberName);
        return parseMobInteraction(mobInteractionObj, typeMemberName);
    }

    public static MobInteraction parseMobInteraction(JsonObject mobInteractionObj, String typeMemberName) {
        MobInteractionType mobInteractionType = parseMobInteractionType(mobInteractionObj, typeMemberName);
        return mobInteractionType.fromJson(mobInteractionObj);
    }

    private static MobInteractionType parseMobInteractionType(JsonObject mobInteractionObj, String typeMemberName) {
        String typeStr = GsonHelper.getAsString(mobInteractionObj, typeMemberName);
        ResourceLocation location = new ResourceLocation(typeStr);
        MobInteractionType mobInteractionType = MobInteractionTypes.getMobInteractionType(location);
        if(mobInteractionType == null) throw new JsonParseException("Invalid MobInteractionType: " + typeStr);
        return mobInteractionType;
    }

    public static MobInteraction.WrapMode parseWrapMode(JsonObject jsonObject, String memberName) {
        String wrapModeStr = GsonHelper.getAsString(jsonObject, memberName, "all");
        return MobInteraction.WrapMode.fromId(wrapModeStr);
    }

    public static MobInteraction parseMobInteractionOrDefault(JsonObject jsonObject, String memberName, String typeMemberName, MobInteraction defaultMobInteraction) {
        return jsonObject.has(memberName) ? parseMobInteraction(jsonObject, memberName, typeMemberName) : defaultMobInteraction;
    }

}
