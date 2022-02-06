package com.infamous.aptitude.common.util;

import com.infamous.aptitude.Aptitude;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
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

    private static Method getMethod(Class<?> sourceClass, String methodName, Class<?>... parameterTypes){
        return CACHED_METHODS.computeIfAbsent(methodName, (k) -> ObfuscationReflectionHelper.findMethod(sourceClass, methodName, parameterTypes));
    }
}
