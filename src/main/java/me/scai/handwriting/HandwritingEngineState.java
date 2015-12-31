package me.scai.handwriting;

import com.google.gson.JsonObject;

public class HandwritingEngineState {
    private StrokeCuratorUserAction userAction;
    private JsonObject state;

    /* Constructors */
    public HandwritingEngineState(StrokeCuratorUserAction userAction, JsonObject state) {
        this.userAction = userAction;
        this.state = state;
    }

    /* Getters */
    public StrokeCuratorUserAction getUserAction() {
        return userAction;
    }

    public JsonObject getState() {
        return state;
    }
}
