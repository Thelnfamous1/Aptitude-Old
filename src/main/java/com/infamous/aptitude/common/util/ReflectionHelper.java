package com.infamous.aptitude.common.util;

import com.infamous.aptitude.Aptitude;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReflectionHelper {
    private static final Map<String, Method> CACHED_METHODS = new HashMap<>();

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
            Aptitude.LOGGER.error("Reflection error for Mob#canReplaceCurrentItem! Defaulting to true.", e);
            return true;
        }
    }

    private static Method getMethod(Class<?> sourceClass, String methodName, Class<?>... parameterTypes){
        return CACHED_METHODS.computeIfAbsent(methodName, (k) -> ObfuscationReflectionHelper.findMethod(sourceClass, methodName, parameterTypes));
    }
}
