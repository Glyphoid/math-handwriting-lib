package me.scai.parsetree.geometry;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Test_GeometricRelationJsonConversion {
    private static final Gson gson = new Gson();

    @Test
    public void testConvertAlignRelation() {
        AlignRelation ar1 = new AlignRelation(AlignRelation.AlignType.AlignBottom, 1, 0);

        JsonObject arObj1 = gson.toJsonTree(ar1).getAsJsonObject();

        assertEquals("AlignBottom", arObj1.get("alignType").getAsString());

        assertEquals(1, arObj1.get("idxTested").getAsJsonArray().size());
        assertEquals(1, arObj1.get("idxTested").getAsJsonArray().get(0).getAsInt());

        assertEquals(1, arObj1.get("idxInRel").getAsJsonArray().size());
        assertEquals(0, arObj1.get("idxInRel").getAsJsonArray().get(0).getAsInt());
    }


}
