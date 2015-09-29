/**
 * Created by scai on 3/25/2015.
 */

package me.scai.plato.helpers;

import com.google.gson.*;
import me.scai.handwriting.CWrittenToken;

public class CWrittenTokenJsonHelper {
    private static final Gson gson = new Gson();

    public static JsonObject CWrittenToken2JsonObject(CWrittenToken wt) {
        JsonObject wtObj = new JsonObject();

        /* numStrokes */
        final int numStrokes = wt.nStrokes();
        wtObj.add("numStrokes", new JsonPrimitive(numStrokes));

        /* strokes */
        JsonObject strokes = new JsonObject();
        for (int i = 0; i < numStrokes; ++i) {
            JsonObject stroke = CStrokeJsonHelper.CStroke2JsonObject(wt.getStroke(i));
            
//            JsonObject stroke = new JsonObject();
//
//            int numPoints = wt.getStroke(i).nPoints();
//            stroke.add("numPoints", new JsonPrimitive(numPoints));
//
//            JsonElement x = gson.toJsonTree(wt.getStroke(i).getXs()).getAsJsonArray();
//            JsonElement y = gson.toJsonTree(wt.getStroke(i).getYs()).getAsJsonArray();
//
//            stroke.add("x", x);
//            stroke.add("y", y);

            strokes.add(Integer.toString(i), stroke);
        }

        wtObj.add("strokes", strokes);

        return wtObj;
    }

    public static String CWrittenToken2JsonNoStroke(CWrittenToken wt) {
        return gson.toJson(CWrittenToken2JsonObjNoStroke(wt));
    }

    public static JsonObject CWrittenToken2JsonObjNoStroke(CWrittenToken wt) {
        if ( !wt.bNormalized ) {
            throw new RuntimeException("Attempt to generate JSON string from un-normalized CWrittenToken object");
        }

        JsonObject obj = new JsonObject();

        /* Get the bounds: [min_x, min_y, max_x, max_y] */
        float [] bounds = wt.getBounds();
        JsonArray jsonBounds = new JsonArray();
        for (int i = 0; i < bounds.length; ++i) {
            jsonBounds.add(new JsonPrimitive(bounds[i]));
        }

        obj.add("bounds", jsonBounds);

        /* Width and height */
        obj.add("width", new JsonPrimitive(wt.width));
        obj.add("height", new JsonPrimitive(wt.height));

        /* Get the recognition winner (if exists) */
        if (wt.getRecogWinner() != null) {
            obj.add("recogWinner", new JsonPrimitive(wt.getRecogWinner()));
        }

        /* Get the recognition p-values (if exists) */
        if (wt.getRecogPs() != null) {
            double [] recogPs = wt.getRecogPs();
            JsonArray jsonRecogPs = new JsonArray();

            for (int i = 0; i < recogPs.length; ++i) {
                jsonRecogPs.add(new JsonPrimitive(recogPs[i]));
            }

            obj.add("recogPs", jsonRecogPs);
        }

        return obj;
    }
    
    public static CWrittenToken jsonObj2CWrittenTokenNoStroke(JsonObject jsonObj) {
        CWrittenToken wt = new CWrittenToken();
        
        /* Width and height */
        if (jsonObj.has("width")) {
            wt.width = jsonObj.get("width").getAsFloat();
        }
        if (jsonObj.has("height")) {
            wt.height = jsonObj.get("height").getAsFloat();
        }
        
        /* Bounds */
        if (jsonObj.has("bounds")) {
            JsonArray jsBounds = jsonObj.get("bounds").getAsJsonArray();
            
            float [] bounds = new float[jsBounds.size()];
            for (int i = 0; i < jsBounds.size(); ++i) {
                bounds[i] = jsBounds.get(i).getAsFloat();
            }
            
            wt.setBounds(bounds);
        }
        
        /* Recognition winner */
        if (jsonObj.has("recogWinner")) {
            wt.setRecogWinner(jsonObj.get("recogWinner").getAsString());
        }
        
        /* Recognition p-values */
        if (jsonObj.has("recogPs")) {            
            JsonArray ps = jsonObj.get("recogPs").getAsJsonArray();
            double [] recogPs = new double[ps.size()];
            
            for (int i = 0; i < ps.size(); ++i) {
                recogPs[i] = ps.get(i).getAsDouble();
            }
            wt.setRecogPs(recogPs);
        }
 
        
        return wt;
    }
}
