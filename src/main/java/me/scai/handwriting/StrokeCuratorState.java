package me.scai.handwriting;

import com.google.gson.JsonObject;
import me.scai.plato.state.PlatoState;

public class StrokeCuratorState implements PlatoState {
    private StrokeCuratorUserAction userAction;
    private JsonObject state;

    /* Constructors */
    public StrokeCuratorState(StrokeCuratorUserAction userAction, JsonObject state) {
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
