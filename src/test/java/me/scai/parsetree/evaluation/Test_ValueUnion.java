package me.scai.parsetree.evaluation;

import Jama.Matrix;
import me.scai.handwriting.TestHelper;
import me.scai.parsetree.HandwritingEngineException;
import me.scai.parsetree.ParseTreeStringizer;
import me.scai.parsetree.TokenSetParserOutput;
import me.scai.plato.engine.HandwritingEngine;
import org.jscience.physics.amount.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Test_ValueUnion {
    /* Constants */
    private static final double DOUBLE_TOL = 1e-9;

    /* Member variables */
    HandwritingEngine hwEng;
    ParseTreeStringizer stringizer;

    /* Methods */
    @Before
    public void setUp() {
        TestHelper.WorkerTuple workerTuple = TestHelper.getTestWorkerTuple();

        stringizer = workerTuple.stringizer;
        assertNotNull(stringizer);

        hwEng = TestHelper.getHandwritingEngine();
        assertNotNull(hwEng);
    }

    @After
    public void tearDown() {}

    @Test
    public void testValueUnion_booleanValueString() {
        ValueUnion boolean0 = new ValueUnion(false);
        ValueUnion boolean1 = new ValueUnion(true);

        assertEquals("false", boolean0.getValueString(stringizer));
        assertEquals("true", boolean1.getValueString(stringizer));

    }

    @Test
    public void testValueUnion_doubleValueString() {
        ValueUnion double0 = new ValueUnion(0);
        ValueUnion double1 = new ValueUnion(-1.0);
        ValueUnion double2 = new ValueUnion(200034.0);

        assertEquals(double0.getDouble(), Double.parseDouble(double0.getValueString(stringizer)), DOUBLE_TOL);
        assertEquals(double1.getDouble(), Double.parseDouble(double1.getValueString(stringizer)), DOUBLE_TOL);
        assertEquals(double2.getDouble(), Double.parseDouble(double2.getValueString(stringizer)), DOUBLE_TOL);
    }

    @Test
    public void testValueUnion_matrixValueString() {
        Matrix matrix0 = new Matrix(2, 2);
        matrix0.set(0, 0, 1); // Column major order
        matrix0.set(1, 0, 2);
        matrix0.set(0, 1, 3);
        matrix0.set(1, 1, 4);

        ValueUnion matrixVal0 = new ValueUnion(matrix0);

        assertEquals("[1.0, 3.0; 2.0, 4.0]", matrixVal0.getValueString(stringizer));

        // Corner case: 0-by-0 empty matrix
        Matrix matrix1 = new Matrix(0, 0);

        ValueUnion matrixVal1 = new ValueUnion(matrix1);

        assertEquals("[]", matrixVal1.getValueString(stringizer));

        // Edge case: 1-by-0 empty matrix
        Matrix matrix2 = new Matrix(1, 0);

        ValueUnion matrixVal2 = new ValueUnion(matrix2);

        assertEquals("[]", matrixVal2.getValueString(stringizer));

        // Edge case: 1-by-0 empty matrix
        Matrix matrix3 = new Matrix(0, 1);

        ValueUnion matrixVal3 = new ValueUnion(matrix3);

        assertEquals("[]", matrixVal3.getValueString(stringizer));
    }

    @Test
    public void testValueUnion_userFunctionValueString() throws HandwritingEngineException {
        /* Add 1st token "V":
         * Note that dummy stroke data are used here. The stroke data merely serves to set up
         * proper bounds for the tokens. This is okay because we are going
         * to force set the token name. */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {0, 1}, new float[] {0, 1}));
        hwEng.forceSetRecogWinner(0, "f");

        /* Add 2nd token "(" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {1.2f, 1.4f}, new float[] {0, 1}));
        hwEng.forceSetRecogWinner(1, "(");

        /* Add 3rd token "x" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {1.6f, 2.6f}, new float[] {0, 1}));
        hwEng.forceSetRecogWinner(2, "x");

        /* Add 4th token ")" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {2.8f, 3.0f}, new float[] {0, 1}));
        hwEng.forceSetRecogWinner(3, ")");

        /* Add 5th token "=" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {3.2f, 4.0f}, new float[] {0, 1}));
        hwEng.forceSetRecogWinner(4, "=");

        /* Add 6th token "x" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {4.2f, 5.0f}, new float[] {0, 1}));
        hwEng.forceSetRecogWinner(5, "x");

        /* Add 7th token "3" (exponent) */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {5.1f, 5.3f}, new float[] {-0.2f, 0.2f}));
        hwEng.forceSetRecogWinner(6, "3");

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"f", "(", "x", ")", "=", "x", "3"});

        TokenSetParserOutput output = hwEng.parseTokenSet();
        assertEquals("(f(x) = (x ^ 3))", output.getStringizerOutput());

        PlatoVarMap varMap = hwEng.getVarMap();

        assertTrue(varMap.containsVarName("f"));

        ValueUnion f = varMap.getVarValue("f");
        assertNotNull(f);

        assertEquals("function: f(x) := (x ^ 3)", f.getValueString(stringizer));

    }

    @Test
    public void testValueUnion_physicalQuantityValueString() {
        ValueUnion gravConst = new ValueUnion(Constants.G);

        assertEquals("(6.674E-11 ± 1.0E-14) m³/(kg·s²)", gravConst.getValueString(stringizer));
    }

}

