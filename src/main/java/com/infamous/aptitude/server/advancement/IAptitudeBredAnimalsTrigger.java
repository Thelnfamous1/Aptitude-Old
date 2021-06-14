package com.infamous.aptitude.server.advancement;

import com.infamous.aptitude.common.entity.IAnimal;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;

public interface IAptitudeBredAnimalsTrigger {
    <T extends MobEntity & IAnimal> void trigger(ServerPlayerEntity serverPlayer, T parent, T partner, @Nullable T child);
}
