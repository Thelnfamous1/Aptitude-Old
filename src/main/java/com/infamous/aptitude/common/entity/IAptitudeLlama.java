package com.infamous.aptitude.common.entity;

public interface IAptitudeLlama {

    boolean getDidSpit();

    void setLlamaDidSpit(boolean didSpit);

    int getSpitCooldown();

    void setSpitCooldown(int spitCooldown);
}
