package com.infamous.aptitude.common;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * CustomServerAiStepEvent is fired when a Mob's custom AI is updated on the server.
 * This event is fired when {@link Mob#customServerAiStep()} is called in {@link Mob#serverAiStep()}.
 * In vanilla, custom AI typically refers to a Mob's {@link Brain}, if it makes use of it.
 *
 * This event is {@link Cancelable}.
 * If this event is canceled, the Mob does not update its custom AI.
 * Cancellation may be useful if you want to replace the Mob's custom AI update.
 * Otherwise, the Mob's custom AI will be updated normally after this event is fired.
 *
 * This event does not have a result. {@link Event#hasResult()}
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 */
@Cancelable
public class CustomServerAiStepEvent extends LivingEvent {
    private final Mob mob;

    public CustomServerAiStepEvent(Mob e) {
        super(e);
        this.mob = e;
    }

    public Mob getMob() {
        return mob;
    }
}
