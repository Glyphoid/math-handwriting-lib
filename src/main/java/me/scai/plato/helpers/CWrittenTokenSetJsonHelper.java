/**
 * Created by scai on 3/25/2015.
 */

package me.scai.plato.helpers;

import com.google.gson.*;

import me.scai.handwriting.CWrittenToken;
import me.scai.handwriting.CWrittenTokenSet;

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
                        
            wtSet.recogWinners.add(wt.getRecogWinner());
            wtSet.recogPs.add(wt.getRecogPs());
        }
        
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

    public static JsonObject CWrittenTokenSet2JsonObj(CWrittenTokenSet wtSet) {
        JsonObject obj = new JsonObject();
        JsonArray jsonTokens = new JsonArray();

        for (int i = 0; i < wtSet.getNumTokens(); ++i) {
            jsonTokens.add(CWrittenTokenJsonHelper.CWrittenToken2JsonObjNoStroke(wtSet.tokens.get(i)));
        }

        obj.add("tokens", jsonTokens);

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
