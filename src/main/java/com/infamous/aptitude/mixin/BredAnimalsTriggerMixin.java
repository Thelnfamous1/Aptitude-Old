package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.entity.IAnimal;
import com.infamous.aptitude.server.advancement.IAptitudeBredAnimalsTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.BredAnimalsTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(BredAnimalsTrigger.class)
public abstract class BredAnimalsTriggerMixin extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance> implements IAptitudeBredAnimalsTrigger {

    @Override
    public <T extends Mob & IAnimal> void trigger(ServerPlayer serverPlayer, T parent, T partner, @Nullable T child) {
        LootContext parentContext = EntityPredicate.createContext(serverPlayer, parent);
        LootContext partnerContext = EntityPredicate.createContext(serverPlayer, partner);
        LootContext childContext = child != null ? EntityPredicate.createContext(serverPlayer, child) : null;
        this.trigger(serverPlayer, (instance) -> {
            return instance.matches(parentContext, partnerContext, childContext);
        });
    }
}
