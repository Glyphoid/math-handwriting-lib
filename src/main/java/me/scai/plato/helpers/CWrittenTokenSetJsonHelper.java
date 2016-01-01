package me.scai.plato.helpers;

import com.google.gson.*;

import com.google.gson.reflect.TypeToken;
import me.scai.handwriting.*;
import me.scai.parsetree.Node;

import java.util.List;
import java.util.ArrayList;

class CWrittenTokenSetJsonConversionException extends Exception {
    public CWrittenTokenSetJsonConversionException(String msg) {
        super(msg);
    }
}

public class CWrittenTokenSetJsonHelper {
    private static Gson gson = new Gson();
    
    public static CWrittenTokenSet jsonObj2CWrittenTokenSet(JsonObject jsonObj) {
        CWrittenTokenSet wtSet = new CWrittenTokenSet();

        JsonArray tokens = jsonObj.get("tokens").getAsJsonArray();

        for (int i = 0; i < tokens.size(); ++i) {
            CWrittenToken wt = CWrittenTokenJsonHelper.jsonObj2CWrittenTokenNoStroke(tokens.get(i).getAsJsonObject());
            wtSet.addToken(wt);

            wtSet.recogWinners.add(wt.getRecogResult());
            wtSet.recogPs.add(wt.getRecogPs());
        }

        return wtSet;
    }

    public static CWrittenTokenSetNoStroke jsonObj2CWrittenTokenSetNoStroke(JsonObject jsonObj) {
        CWrittenTokenSetNoStroke wtSet = new CWrittenTokenSetNoStroke();

        JsonArray tokens = jsonObj.get("tokens").getAsJsonArray();
        JsonArray tokenUuidsArray = jsonObj.get("tokenUuids").getAsJsonArray();
        JsonArray tokenIDsArray = jsonObj.get("tokenIDs").getAsJsonArray();

        for (int i = 0; i < tokens.size(); ++i) {
            JsonObject tokenObj = tokens.get(i).getAsJsonObject();
            List<String> tokenUuids = gson.fromJson(tokenUuidsArray.get(i).getAsJsonArray(), new TypeToken<ArrayList<String>>() {}.getType());
            int tokenID = tokenIDsArray.get(i).getAsInt();

            if (tokenObj.has("node")) {     // This is a node token
//                NodeToken nodeToken = gson.fromJson(tokens.get(i).getAsJsonObject(), NodeToken.class);
                JsonObject nodeTokenJson = tokens.get(i).getAsJsonObject();

                Node node = gson.fromJson(nodeTokenJson.get("node").getAsJsonObject(), Node.class);

                CWrittenTokenSetNoStroke wtSetInner = jsonObj2CWrittenTokenSetNoStroke(tokens.get(i).getAsJsonObject().get("wtSet").getAsJsonObject());

                NodeToken nodeToken = new NodeToken(node, wtSetInner);

                nodeToken.setRecogResult(nodeTokenJson.get("parsingResult").getAsString());

                if ( !nodeTokenJson.get("matchingGraphicalProductionIndices").isJsonNull() ) { //TODO: De-uglify
                    List<Integer> matchingGraphicalProductionIndices = gson.fromJson(nodeTokenJson.get("matchingGraphicalProductionIndices").getAsJsonArray(),
                            new TypeToken<List<Integer>>() {
                            }.getType());
                    nodeToken.setMatchingGraphicalProductionIndices(matchingGraphicalProductionIndices);
                }

                wtSet.addToken(nodeToken, tokenUuids, tokenID);


            } else { // This is a written token
                wtSet.addToken(CWrittenTokenJsonHelper.jsonObj2CWrittenTokenNoStroke(tokens.get(i).getAsJsonObject()), tokenUuids.get(0), tokenID);
            }
        }

        wtSet.calcBounds();

        return wtSet;
    }

    public static List<int []> jsonArray2ConstituentStrokeIndices(JsonArray jsConstIndices) {
        List<int []> constStrokeIndices = new ArrayList<int []>();
                
        ((ArrayList) constStrokeIndices).ensureCapacity(jsConstIndices.size());
        
        for (int i = 0; i < jsConstIndices.size(); ++i) {
            JsonArray jsStrokeIndices = jsConstIndices.get(i).getAsJsonArray();
                        
            int [] strokeIndices = new int[jsStrokeIndices.size()];
            for (int j = 0; j < jsStrokeIndices.size(); ++j) {
                strokeIndices[j] = jsStrokeIndices.get(j).getAsInt();
            }
            constStrokeIndices.add(strokeIndices);
        }
        
        return constStrokeIndices;
    }

    public static JsonObject CAbstractWrittenTokenSet2JsonObj(CAbstractWrittenTokenSet wtSet) {
        if (wtSet instanceof CWrittenTokenSet) {
            return CWrittenTokenSet2JsonObj((CWrittenTokenSet) wtSet);
        } else if (wtSet instanceof CWrittenTokenSetNoStroke) {
            return CWrittenTokenSetNoStroke2JsonObj((CWrittenTokenSetNoStroke) wtSet);
        } else {
            throw new IllegalArgumentException("Unrecognized subtype of token set: " + wtSet);
        }
    }

    public static JsonObject CWrittenTokenSet2JsonObj(CWrittenTokenSet wtSet) {
        JsonObject obj = new JsonObject();
        JsonArray jsonTokens = new JsonArray();

        for (int i = 0; i < wtSet.getNumTokens(); ++i) {
            if ( !(wtSet.tokens.get(i) instanceof CWrittenToken) ) {
                throw new IllegalStateException("Not implemented yet");
                // TODO: Implement JSON serialization for NodeToken members
            }

            jsonTokens.add(CWrittenTokenJsonHelper.CWrittenToken2JsonObjNoStroke((CWrittenToken) wtSet.tokens.get(i)));
        }

        obj.add("tokens", jsonTokens);

        return obj;
    }

    // TODO: De-duplicate with the above method
    public static JsonObject CWrittenTokenSetNoStroke2JsonObj(CWrittenTokenSetNoStroke wtSet) {
        JsonObject obj = new JsonObject();
        JsonArray jsonTokens = new JsonArray();

        for (int i = 0; i < wtSet.getNumTokens(); ++i) {
            if ( wtSet.tokens.get(i) instanceof NodeToken) {
                // Serialize the note token
                NodeToken nodeToken = (NodeToken) wtSet.tokens.get(i);

                JsonObject nodeTokenJson = (JsonObject) gson.toJsonTree(nodeToken);

                if (nodeToken.getTokenSet() instanceof CWrittenTokenSetNoStroke) {
                    nodeTokenJson.add("wtSet", CWrittenTokenSetNoStroke2JsonObj((CWrittenTokenSetNoStroke) nodeToken.getTokenSet()));

                    // TODO: Refactor and de-duplicate
                    nodeTokenJson.add("matchingGraphicalProductionIndices", gson.toJsonTree(nodeToken.getMatchingGraphicalProductionIndices()));

                } else {
                    throw new IllegalStateException("Unexpected subtype of written token set in node token");
                }
//                nodeTokenJson.add("");

                jsonTokens.add(nodeTokenJson);
                // TODO: Implement JSON serialization for NodeToken members
            } else if (wtSet.tokens.get(i) instanceof CWrittenToken) {
                jsonTokens.add(CWrittenTokenJsonHelper.CWrittenToken2JsonObjNoStroke((CWrittenToken) wtSet.tokens.get(i)));
            }
        }

        obj.add("tokens", jsonTokens);
        obj.add("tokenUuids", gson.toJsonTree(wtSet.tokenUuids));
        obj.add("tokenIDs", gson.toJsonTree(wtSet.tokenIDs));
        obj.add("hasNodeToken", gson.toJsonTree(wtSet.hasNodeToken()));

        // Base class
        obj.add("nt", gson.toJsonTree(wtSet.getNumTokens())); // Is this correct?
        obj.add("tokenNames", gson.toJsonTree(wtSet.getTokenNames()));

        return obj;
    }

    public static JsonArray listOfInt2JsonArray(List<int[]> lints) {
        JsonArray arr = new JsonArray();

        for (int i = 0; i < lints.size(); ++i) {
            int [] tInts = lints.get(i);
            JsonArray tArr = new JsonArray();

            for (int j = 0; j < tInts.length; ++j) {
                tArr.add(new JsonPrimitive(tInts[j]));
            }

            arr.add(tArr);
        }

        return arr;
    }

}
