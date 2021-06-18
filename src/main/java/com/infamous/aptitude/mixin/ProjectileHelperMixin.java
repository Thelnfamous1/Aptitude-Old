package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.util.AptitudeHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(ProjectileHelper.class)
public abstract class ProjectileHelperMixin {

    @Inject(at = @At("HEAD"), method = "getWeaponHoldingHand", cancellable = true)
    private static void betterGetWeaponHoldingHand(LivingEntity living, Item itemIn, CallbackInfoReturnable<Hand> cir){
        Class<? extends Item> itemInClass = itemIn.getClass();
        Predicate<Item> itemPredicate = testItem -> testItem.getClass().isAssignableFrom(itemInClass);
        cir.setReturnValue(AptitudeHelper.getWeaponHoldingHand(living, itemPredicate));
    }
}
