package com.infamous.aptitude.common.behavior.util;

import com.infamous.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class SelectorHelper {

    public static void remakeSelectors(Mob mob) {
        ResourceLocation registryName = mob.getType().getRegistryName();
        if(Aptitude.selectorManager.replaceGoalSelector(registryName)){
            clearGoalSelector(mob);
        }
        if(Aptitude.selectorManager.replaceTargetSelector(registryName)){
            clearTargetSelector(mob);
        }
    }
    public static void clearGoalSelector(Mob mob) {
        mob.goalSelector.removeAllGoals();
    }

    public static void clearTargetSelector(Mob mob) {
        mob.targetSelector.removeAllGoals();
    }

    public static boolean hasSelectorFile(Mob mob) {
        ResourceLocation etLocation = mob.getType().getRegistryName();
        return Aptitude.selectorManager.hasSelectorEntry(etLocation);
    }
}
