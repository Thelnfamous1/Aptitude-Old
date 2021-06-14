package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.entity.IAnimal;
import com.infamous.aptitude.server.advancement.IAptitudeBredAnimalsTrigger;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.BredAnimalsTrigger;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(BredAnimalsTrigger.class)
public abstract class BredAnimalsTriggerMixin extends AbstractCriterionTrigger<BredAnimalsTrigger.Instance> implements IAptitudeBredAnimalsTrigger {

    @Override
    public <T extends MobEntity & IAnimal> void trigger(ServerPlayerEntity serverPlayer, T parent, T partner, @Nullable T child) {
        LootContext parentContext = EntityPredicate.createContext(serverPlayer, parent);
        LootContext partnerContext = EntityPredicate.createContext(serverPlayer, partner);
        LootContext childContext = child != null ? EntityPredicate.createContext(serverPlayer, child) : null;
        this.trigger(serverPlayer, (instance) -> {
            return instance.matches(parentContext, partnerContext, childContext);
        });
    }
}
