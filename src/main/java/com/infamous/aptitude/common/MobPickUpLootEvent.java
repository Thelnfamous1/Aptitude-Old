package com.infamous.aptitude.common;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * TODO
 * This event is {@link Cancelable}.
 *
 * This event does not have a result. {@link Event#hasResult()}
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 */
@Cancelable
public class MobPickUpLootEvent extends LivingEvent {
    private final Mob mob;
    private final ItemStack itemStack;

    public MobPickUpLootEvent(Mob e, ItemStack itemStack) {
        super(e);
        this.mob = e;
        this.itemStack = itemStack;
    }

    public Mob getMob() {
        return mob;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
