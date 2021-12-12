package com.infamous.aptitude.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow public abstract boolean isHolding(Predicate<Item> itemPredicate);

    @Inject(at = @At("HEAD"), method = "isHolding(Lnet/minecraft/item/Item;)Z", cancellable = true)
    private void betterIsHolding(Item itemIn, CallbackInfoReturnable<Boolean> cir){
        if(itemIn instanceof ProjectileWeaponItem){
            Class<? extends Item> itemInClass = itemIn.getClass();
            Predicate<Item> itemPredicate = testItem -> testItem.getClass().isAssignableFrom(itemInClass);
            cir.setReturnValue(this.isHolding(itemPredicate));
        }
    }
}
