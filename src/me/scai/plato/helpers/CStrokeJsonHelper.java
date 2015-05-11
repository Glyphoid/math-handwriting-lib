/**
 * Created by scai on 3/25/2015.
 */

package me.scai.plato.helpers;

import com.google.gson.*;
import me.scai.handwriting.CStroke;

public class CStrokeJsonHelper {
    private final static Gson gson = new Gson();

    /* Exception classes */
    public static class CStrokeJsonConversionException extends Exception {
        /* Constructor */
        public CStrokeJsonConversionException(String msg) {
            super(msg);
        }
    }

    private static final JsonParser jsonParser = new JsonParser();

    public static JsonObject CStroke2JsonObject(CStroke stroke) {
        /* strokes */
        JsonObject strokeObj = new JsonObject();

        int numPoints = stroke.nPoints();
        strokeObj.add("numPoints", new JsonPrimitive(numPoints));

        JsonElement x = gson.toJsonTree(stroke.getXs()).getAsJsonArray();
        JsonElement y = gson.toJsonTree(stroke.getYs()).getAsJsonArray();

        strokeObj.add("x", x);
        strokeObj.add("y", y);

        return strokeObj;
    }

    public static CStroke json2CStroke(String json)
            throws CStrokeJsonConversionException {
        JsonObject jsonObj = null;
        try {
            jsonObj = jsonParser.parse(json).getAsJsonObject();
        }
        catch (JsonParseException exc) {
            throw new CStrokeJsonConversionException("Failed to parse json for CStroke, due to " + exc.getMessage());
        }

        int numPoints = jsonObj.get("numPoints").getAsInt();
        if (numPoints <= 0) {
            return null;
        }

        JsonArray xs = jsonObj.get("x").getAsJsonArray();
        JsonArray ys = jsonObj.get("y").getAsJsonArray();

        if (xs.size() != numPoints) {
            throw new CStrokeJsonConversionException("Mismatch between the value of numPoints (" + numPoints +
                                                     ") and the actual length of xs (" + xs.size() + ")");
        }
        if (ys.size() != numPoints) {
            throw new CStrokeJsonConversionException("Mismatch between the value of numPoints (" + numPoints +
                                                     ") and the actual length of xs (" + ys.size() + ")");
        }

        CStroke stroke = new CStroke();
        for (int i = 0; i < numPoints; ++i) {
            float x = xs.get(i).getAsFloat();
            float y = ys.get(i).getAsFloat();

            stroke.addPoint(x, y);
        }

        return stroke;
    }
}
