package com.infamous.aptitude.common.entity;

public interface IRearing {
    int ANGRY_SOUND_INTERVAL = 40;

    void startRearing();

    void stopRearing();

    void playAngrySound();

    int getAngrySoundCooldown();

    void setAngrySoundCooldown(int angrySoundCooldown);

    default int getAngrySoundInterval(){
        return ANGRY_SOUND_INTERVAL;
    }
}
