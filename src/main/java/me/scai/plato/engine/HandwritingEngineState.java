package me.scai.plato.engine;

import com.google.gson.JsonObject;
import me.scai.handwriting.HandwritingEngineUserAction;
import me.scai.plato.state.PlatoState;

public class HandwritingEngineState implements PlatoState {
    private HandwritingEngineUserAction userAction;
    private JsonObject state;

    /* Constructors */
    public HandwritingEngineState(HandwritingEngineUserAction userAction, JsonObject state) {
        this.userAction = userAction;
        this.state = state;
    }

    /* Implementing interface methods */
    @Override
    public String getUserAction() {
        return userAction.toString();
    }

    @Override
    public JsonObject getState() {
        return state;
    }
}
