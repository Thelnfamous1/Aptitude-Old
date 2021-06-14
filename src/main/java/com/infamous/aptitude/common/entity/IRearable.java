package com.infamous.aptitude.common.entity;

import net.minecraft.util.SoundEvent;

public interface IRearable {

    void stopRearing();

    SoundEvent getAngrySoundRaw();

    void playAngrySound();

    int getAngrySoundCooldown();

    void setAngrySoundCooldown(int angrySoundCooldown);

    void startRearing();
}
