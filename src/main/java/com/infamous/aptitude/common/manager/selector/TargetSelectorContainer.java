package com.infamous.aptitude.common.manager.selector;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

public class TargetSelectorContainer implements SelectorContainer{
    public static final TargetSelectorContainer EMPTY = new TargetSelectorContainer();
    private boolean replaceTargetSelector = false;

    private TargetSelectorContainer(){

    }

    public static TargetSelectorContainer of(JsonObject topElement) {
        TargetSelectorContainer targetSelectorContainer = new TargetSelectorContainer();
        targetSelectorContainer.replaceTargetSelector = GsonHelper.getAsBoolean(topElement, "replace_target_selector", false);

        return targetSelectorContainer;
    }

    @Override
    public boolean replaceSelector() {
        return this.replaceTargetSelector;
    }
}
