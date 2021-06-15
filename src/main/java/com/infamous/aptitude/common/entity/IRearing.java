package com.infamous.aptitude.common.entity;

public interface IRearing {

    void startRearing();

    void stopRearing();

    void playAngrySound();

    int getAngrySoundCooldown();

    void setAngrySoundCooldown(int angrySoundCooldown);
}
