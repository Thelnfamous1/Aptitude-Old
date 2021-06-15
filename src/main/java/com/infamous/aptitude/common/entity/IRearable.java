package com.infamous.aptitude.common.entity;

public interface IRearable {

    void startRearing();

    void stopRearing();

    void playAngrySound();

    int getAngrySoundCooldown();

    void setAngrySoundCooldown(int angrySoundCooldown);
}
