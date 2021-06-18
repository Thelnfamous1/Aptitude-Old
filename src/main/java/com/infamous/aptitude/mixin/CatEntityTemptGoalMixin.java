package com.infamous.aptitude.mixin;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.entity.passive.CatEntity$TemptGoal")
public abstract class CatEntityTemptGoalMixin extends TemptGoal {

    @Final
    @Shadow
    private CatEntity cat;

    public CatEntityTemptGoalMixin(CreatureEntity p_i47822_1_, double p_i47822_2_, Ingredient p_i47822_4_, boolean p_i47822_5_) {
        super(p_i47822_1_, p_i47822_2_, p_i47822_4_, p_i47822_5_);
    }

    @Override
    protected boolean shouldFollowItem(ItemStack stack) {
        return this.cat.isFood(stack);
    }
}
