package me.scai.handwriting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenSetJsonTestHelper {
    public static void verifyTokenSetJson(JsonObject tokenSetJson) {
        assertTrue(tokenSetJson.has("tokens"));
        assertTrue(tokenSetJson.get("tokens").isJsonArray());
        JsonArray tokensJson = tokenSetJson.get("tokens").getAsJsonArray();
        for (int i = 0; i < tokensJson.size(); ++i) {
            JsonObject tokenJson = tokensJson.get(i).getAsJsonObject();

            if (tokenJson.has("bounds")) { // Normal token
                TokenSetJsonTestHelper.verifyWrittenTokenJson(tokenJson);
            } else if (tokenJson.has("node")) {
                TokenSetJsonTestHelper.verifyNodeTokenJson(tokenJson);
            }
        }
    }

    public static void verifyWrittenTokenJson(JsonObject tokenJson) {
        assertTrue(tokenJson.get("bounds").isJsonArray());
        assertTrue(tokenJson.get("width").isJsonPrimitive());
        assertTrue(tokenJson.get("height").isJsonPrimitive());
        assertTrue(tokenJson.get("recogWinner").isJsonPrimitive());
        assertFalse(tokenJson.has("node"));
        assertFalse(tokenJson.has("wtSet"));
    }

    public static void verifyNodeTokenJson(JsonObject tokenJson) {
        assertTrue(tokenJson.get("node").isJsonObject());

//        assertTrue(tokenJson.get("matchingGraphicalProductionIndices").isJsonArray()); //TODO: Is this necessary?
//        if ( !tokenJson.has("matchingGraphicalProductionIndices") )  {
//            int iii = 888;
//        }

        assertTrue(tokenJson.get("tokenBounds").isJsonArray());
        assertTrue(tokenJson.get("width").isJsonPrimitive());
        assertTrue(tokenJson.get("height").isJsonPrimitive());
        assertFalse(tokenJson.get("parsingResult").getAsString().isEmpty());

        assertTrue(tokenJson.get("wtSet").isJsonObject());

        JsonObject tokenSetJson = tokenJson.get("wtSet").getAsJsonObject();
        verifyTokenSetJson(tokenSetJson);

        JsonObject nodeJson = tokenJson.get("node").getAsJsonObject();
        verifyNodeJson(nodeJson);
    }

    public static void verifyNodeJson(JsonObject nodeJson) {
        assertTrue(nodeJson.get("isTerminal").isJsonPrimitive());
        assertFalse(nodeJson.get("lhs").getAsString().isEmpty());
        assertFalse(nodeJson.get("prodSumString").getAsString().isEmpty());

        if (nodeJson.has("bounds") && nodeJson.get("bounds").isJsonArray()) {
            JsonArray bounds = nodeJson.get("bounds").getAsJsonArray();
            assertEquals(4, bounds.size());
        }

        boolean isTerminal = nodeJson.get("isTerminal").getAsBoolean();
        if (!isTerminal) {
            assertTrue(nodeJson.get("rhsTypes").isJsonArray());
            assertTrue(nodeJson.get("ch").isJsonArray());

            JsonArray children = nodeJson.get("ch").getAsJsonArray();
            for (int i = 0; i < children.size(); ++i) {
                JsonObject childNode = children.get(i).getAsJsonObject();

                verifyNodeJson(childNode);
            }
        }




    }
}
