package com.infamous.aptitude.common.entity;

import net.minecraft.util.SoundEvent;

public interface IRearable {

    void startRearing();

    void stopRearing();

    void playAngrySound();

    int getAngrySoundCooldown();

    void setAngrySoundCooldown(int angrySoundCooldown);
}
