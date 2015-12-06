package me.scai.parsetree;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.scai.handwriting.TestHelper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Test_GraphicalProductionJsonConversion {
    private static final Gson gson = new Gson();

    GraphicalProductionSet gpSet;

    @Before
    public void setup() {
        TestHelper.WorkerTuple workerTuple = TestHelper.getTestWorkerTuple();

        gpSet = workerTuple.tokenSetParser.getGraphicalProductionSet();
        assertNotNull(gpSet);
    }

    @Test
    public void testConversionAll() {

        for (GraphicalProduction gp1 : gpSet.prods) {
            assertNotNull(gp1);
            JsonObject gpObj1 = gson.toJsonTree(gp1).getAsJsonObject();

            assertNotNull(gpObj1.get("lhs").getAsString());
            assertTrue(gpObj1.get("nrhs").getAsInt() > 0);

            assertNotNull(gpObj1.get("rhs").getAsJsonArray());
            assertNotNull(gpObj1.get("rhsIsTerminal").getAsJsonArray());

            assertNotNull(gpObj1.get("sumString").getAsString());
            assertNotNull(gpObj1.get("geomShortcut").getAsJsonObject());
            assertNotNull(gpObj1.get("geomShortcut").getAsJsonObject().get("shortcutType"));

            assertFalse(gpObj1.has("terminalSet"));
        }
    }

    @Test
    public void testConversionGraphicalProductions() {
        JsonArray gpSetArray = gson.toJsonTree(gpSet.prods).getAsJsonArray();

        assertNotNull(gpSetArray);
    }
}
