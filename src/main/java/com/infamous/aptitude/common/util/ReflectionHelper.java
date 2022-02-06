package com.infamous.aptitude.common.util;

import com.infamous.aptitude.Aptitude;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionHelper {
    public static boolean reflectPiglinCanHunt(AbstractPiglin piglin){
        Method canHunt = ObfuscationReflectionHelper.findMethod(AbstractPiglin.class, "m_7121_");
        try {
            return (boolean) canHunt.invoke(piglin);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Aptitude.LOGGER.error("Reflection error for AbstractPiglin#canHunt! Defaulting to true.", e);
            return true;
        }
    }
}
