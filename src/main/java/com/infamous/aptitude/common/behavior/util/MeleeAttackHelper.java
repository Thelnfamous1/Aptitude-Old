package com.infamous.aptitude.common.behavior.util;

import net.minecraft.world.entity.LivingEntity;

public class MeleeAttackHelper {

    public static boolean isWithinMeleeAttackRangeDefault(LivingEntity attacker, LivingEntity target){
        double distanceToSqr = attacker.distanceToSqr(target.getX(), target.getY(), target.getZ());
        return distanceToSqr <= getDefaultMeleeAttackRangeSqr(attacker, target);
    }

    public static double getDefaultMeleeAttackRangeSqr(LivingEntity attacker, LivingEntity target){
        return calculateMeleeAttackRangeSqr(attacker, target, 0, 2, 0, 1);
    }
    public static double getRavagerMeleeAttackRangeSqr(LivingEntity attacker, LivingEntity target){
        return calculateMeleeAttackRangeSqr(attacker, target, 0.1F, 2, 0, 1);
    }

    public static double getSpiderBearRabbitMeleeAttackRangeSqr(LivingEntity attacker, LivingEntity target){
        return calculateMeleeAttackRangeSqr(attacker, target, 0, 0, 4, 1);
    }

    public static double getAxolotlMeleeAttackRangeSqr(LivingEntity attacker, LivingEntity target){
        return calculateMeleeAttackRangeSqr(attacker, target, 0, 0, 1.5F, 2);
    }

    public static double calculateMeleeAttackRangeSqr(
            LivingEntity attacker,
            LivingEntity target,
            float attackerWidthRed, // 1.0F for ravagers
            float adjAttackerWidthScale, // 0.0F for spiders, bears, rabbits, axolotls
            float adjAttackerWidthSqrAdd, // 4.0 for spiders, bears, rabbits, 1.5F for axolotls
            float targetWidthScale){
        float adjAttackerWidth = attacker.getBbWidth() - attackerWidthRed;
        float adjAttackerWidthSqr = adjAttackerWidth * adjAttackerWidthScale * adjAttackerWidth * adjAttackerWidthScale + adjAttackerWidthSqrAdd;

        float targetWidth = target.getBbWidth();
        float adjTargetWidth = targetWidth * targetWidthScale;
        return (double) (adjAttackerWidthSqr + adjTargetWidth);
    }
}
