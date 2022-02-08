package com.infamous.aptitude.common.manager.selector;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

public class GoalSelectorContainer implements SelectorContainer{
    public static final GoalSelectorContainer EMPTY = new GoalSelectorContainer();
    private boolean replaceGoalSelector = false;

    private GoalSelectorContainer(){

    }

    public static GoalSelectorContainer of(JsonObject topElement) {
        GoalSelectorContainer goalSelectorContainer = new GoalSelectorContainer();
        goalSelectorContainer.replaceGoalSelector = GsonHelper.getAsBoolean(topElement, "replace_goal_selector", false);

        return goalSelectorContainer;
    }

    @Override
    public boolean replaceSelector() {
        return this.replaceGoalSelector;
    }
}
