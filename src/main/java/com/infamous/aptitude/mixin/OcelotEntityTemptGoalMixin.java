package com.infamous.aptitude.mixin;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.entity.passive.OcelotEntity$TemptGoal")
public abstract class OcelotEntityTemptGoalMixin extends TemptGoal {

    @Final
    @Shadow
    private Ocelot ocelot;

    public OcelotEntityTemptGoalMixin(PathfinderMob p_i47822_1_, double p_i47822_2_, Ingredient p_i47822_4_, boolean p_i47822_5_) {
        super(p_i47822_1_, p_i47822_2_, p_i47822_4_, p_i47822_5_);
    }

    @Override
    protected boolean shouldFollowItem(ItemStack stack) {
        return this.ocelot.isFood(stack);
    }
}
