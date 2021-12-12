package com.infamous.aptitude.server.advancement;

import com.infamous.aptitude.common.entity.IAnimal;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public interface IAptitudeBredAnimalsTrigger {
    <T extends Mob & IAnimal> void trigger(ServerPlayer serverPlayer, T parent, T partner, @Nullable T child);
}
