package com.infamous.aptitude.common.util;

import com.google.common.collect.ImmutableMap;
import com.infamous.aptitude.Aptitude;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReflectionHelper {
    private static final Map<String, Method> CACHED_METHODS = new HashMap<>();
    private static final Map<String, Field> CACHED_FIELDS = new HashMap<>();

    public static void reflectAnimalUsePlayerItem(Animal animal, Player player, InteractionHand hand, ItemStack stack){
        Method usePlayerItem = getMethod(Animal.class, "m_142075_", Player.class, InteractionHand.class, ItemStack.class);
        try {
            usePlayerItem.invoke(animal, player, hand, stack);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Aptitude.LOGGER.error("Reflection error for Animal#usePlayerItem!", e);
        }
    }

    public static InteractionResult reflectMobCheckAndHandleImportantInteractions(Mob mob, Player player, InteractionHand hand){
        Method checkAndHandleImportantInteractions = getMethod(Mob.class, "m_21499_", Player.class, InteractionHand.class);
        try {
            return (InteractionResult) checkAndHandleImportantInteractions.invoke(mob, player, hand);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Aptitude.LOGGER.error("Reflection error for Mob#checkAndHandleImportantInteractions! Defaulting to InteractionResult.PASS.", e);
            return InteractionResult.PASS;
        }
    }

    public static boolean reflectPiglinCanHunt(AbstractPiglin piglin){
        Method canHunt = getMethod(AbstractPiglin.class, "m_7121_");
        try {
            return (boolean) canHunt.invoke(piglin);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Aptitude.LOGGER.error("Reflection error for AbstractPiglin#canHunt! Defaulting to true.", e);
            return true;
        }
    }

    public static boolean reflectCanReplaceCurrentItem(Mob mob, ItemStack replacement){
        EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(replacement);
        ItemStack current = mob.getItemBySlot(equipmentslot);
        return reflectCanReplaceCurrentItem(mob, replacement, current);
    }

    public static boolean reflectCanReplaceCurrentItem(Mob mob, ItemStack replacement, ItemStack current){
        Method canReplaceCurrentItem = getMethod(Mob.class, "m_34787_", ItemStack.class, ItemStack.class);
        try {
            return (boolean) canReplaceCurrentItem.invoke(mob, replacement, current);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Aptitude.LOGGER.error("Reflection error for Mob#canReplaceCurrentItem! Defaulting to false.", e);
            return false;
        }
    }

    private static Method getMethod(Class<?> sourceClass, String methodName, Class<?>... parameterTypes){
        return CACHED_METHODS.computeIfAbsent(methodName, (k) -> ObfuscationReflectionHelper.findMethod(sourceClass, methodName, parameterTypes));
    }

    private static Field getField(Class<?> sourceClass, String fieldName){
        return CACHED_FIELDS.computeIfAbsent(fieldName, (k) -> ObfuscationReflectionHelper.findField(sourceClass, fieldName));
    }


    public static Map<GameRules.Key<?>, GameRules.Value<?>> retrieveRulesMap(GameRules gameRules) {
        Field rules = getField(GameRules.class, "f_46130_");
        try {
            return (Map<GameRules.Key<?>, GameRules.Value<?>>) rules.get(gameRules);
        } catch (IllegalAccessException e) {
            Aptitude.LOGGER.error("Reflection error for GameRules#rules!", e);
            return ImmutableMap.of();
        }
    }


    public static void setMobLookControl(Mob mob, LookControl lookControl) {
        Field rules = getField(Mob.class, "f_21365_");
        try {
            rules.set(mob, lookControl);
        } catch (IllegalAccessException e) {
            Aptitude.LOGGER.error("Reflection error for Mob#lookControl!", e);
        }
    }

    public static void setMobMoveControl(Mob mob, MoveControl moveControl) {
        Field rules = getField(Mob.class, "f_21342_");
        try {
            rules.set(mob, moveControl);
        } catch (IllegalAccessException e) {
            Aptitude.LOGGER.error("Reflection error for Mob#moveControl!", e);
        }
    }

    public static void setMobNavigation(Mob mob, PathNavigation navigation) {
        Field rules = getField(Mob.class, "f_21344_");
        try {
            rules.set(mob, navigation);
        } catch (IllegalAccessException e) {
            Aptitude.LOGGER.error("Reflection error for Mob#navigation!", e);
        }
    }
}
