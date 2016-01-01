package me.scai.plato.state;

import com.google.gson.JsonObject;

public interface PlatoState {
    String getUserAction();
    JsonObject getState();
}
