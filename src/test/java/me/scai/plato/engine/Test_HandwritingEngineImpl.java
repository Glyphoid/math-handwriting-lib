package me.scai.plato.engine;

import com.google.gson.JsonObject;
import me.scai.handwriting.*;
import me.scai.parsetree.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class Test_HandwritingEngineImpl {
    /* Constants */
    private static final float FLOAT_TOL = 1e-9f;

    private HandwritingEngine hwEng;

    /* Constructor */
    @Before
    public void setUp() {

        hwEng = TestHelper.getHandwritingEngine();

    }

    @Test
    public void testDisableEnableProductions_sunnyDay() throws HandwritingEngineException {
        /* Add 1st stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {10, 10, 10, 10, 10, 10},
                                                 new float[] {15, 20, 25, 30, 35, 40}));

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {15, 30, 30, 15, 15, 30},
                                                 new float[] {0, 0, 10, 10, 20, 20}));

        TestHelper.verifyTokenSet(hwEng, new boolean[] {false, false}, new String[] {"1", "2"});

        /* Parse with all grammar enabled: Should yield 1 ^ 2 */
        TokenSetParserOutput parseRes = hwEng.parseTokenSet();
        assertEquals("(1 ^ 2)", parseRes.getStringizerOutput());

        /* Disable grammar node "EXPONENTATION" */
        hwEng.disableProductionsByGrammarNodeNames(new String[]{"EXPONENTIATION", "FRACTION"});

        /* This parsing should yield "12" */
        parseRes = hwEng.parseTokenSet();
        assertEquals("12", parseRes.getStringizerOutput());

        /* Re-enable the productions */
        hwEng.enableAllProductions();

        /* Now the parsing result should be the same as the original */
        parseRes = hwEng.parseTokenSet();
        assertEquals("(1 ^ 2)", parseRes.getStringizerOutput());
    }

    @Test
    public void testDisableNonExistentGrammarNode() {
        boolean exceptionThrown = false;

        try {
            hwEng.disableProductionsByGrammarNodeNames(new String[]{"QUX_BAZ", "EXPOENTIATION"});
        } catch (HandwritingEngineException exc) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

    }

    @Test
    public void testSubsetParsingFollowedByRemoveTokenThenAddTokens() throws HandwritingEngineException {
        /* Add 1st token "7" */
        addSeven(hwEng);
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false}, new String[] {"7"});

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {15, 30, 30, 15, 15, 30},
                                                 new float[] {30, 30, 40, 40, 50, 50}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false, false}, new String[] {"7", "2"});

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {0, 10, 20, 30, 40},
                                                 new float[] {25, 25, 25, 25, 25}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false, false, false}, new String[] {"7", "2", "-"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {20, 20.1f, 20.2f, 20.3f, 20.4f},
                                                 new float[] {0, 5, 10, 15, 20}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false, false, false, false}, new String[] {"7", "2", "-", "1"});

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {true, false, false}, new String[] {"72", "-", "1"});

        /* Remove the token "1" */
        // The index is to the abstract token, not written token
        hwEng.removeToken(2);

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {true, false}, new String[] {"72", "-"});

        /* Add two tokens: "11" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {20, 20.1f, 20.2f, 20.3f, 20.4f},
                                                 new float[] {0, 5, 10, 15, 20}));
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {22, 22.1f, 22.2f, 22.3f, 22.4f},
                                                 new float[] {0, 5, 10, 15, 20}));

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {true, false, false, false}, new String[] {"72", "-", "1", "1"});

        /* Parse the entire token set, which contains a node token */
        TokenSetParserOutput parseRes = hwEng.parseTokenSet();

        assertEquals("(11 / 72)", parseRes.getStringizerOutput());

    }

    @Test
     public void testSubsetParsingFollowedByTokenMoving_hardWay() throws HandwritingEngineException {
        /* Add 1st token "7" */
        addSeven(hwEng);
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false}, new String[] {"7"});

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {15, 30, 30, 15, 15, 30},
                new float[] {30, 30, 40, 40, 50, 50}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false, false}, new String[] {"7", "2"});

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {0, 10, 20, 30, 40},
                new float[] {25, 25, 25, 25, 25}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false, false, false}, new String[] {"7", "2", "-"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {11, 11, 11, 11, 11, 11},
                new float[] {5, 7, 9, 11, 13, 15}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false, false, false, false}, new String[] {"7", "2", "-", "1"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {14, 16, 18, 18, 17, 16},
                new float[] {5, 5, 5, 8, 12, 15}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false, false, false, false, false}, new String[] {"7", "2", "-", "1", "7"});

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {true, false, false, false}, new String[] {"72", "-", "1", "7"});

        /* Verify bounds before moving */
        // Token indices are to abstract tokens
        assertArrayEquals(new float[] {11f, 5f, 11f, 15f}, hwEng.getTokenBounds(2), FLOAT_TOL);
        assertArrayEquals(new float[] {14f, 5f, 18f, 15f}, hwEng.getTokenBounds(3), FLOAT_TOL);

        /* Move tokens */
        final float[] newBounds_1 = new float[] {26f, 5f, 26f, 15f}; // New position for "1"
        final float[] newBounds_7 = new float[] {11f, 5f, 15f, 15f}; // New position for "7"

        // Token indices are to abstract tokens
        hwEng.moveToken(2, newBounds_1);

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});

        // TODO: This may come as a little surprising and should be fixed. Due to the updateCurrentTokenSet() call
        // in HandwritingEngineImpl, the order of the abstract tokens may change after token move.
        int idx_7 = findLastAbstractTokenByName(hwEng.getTokenSet(), "7");

        hwEng.moveToken(idx_7, newBounds_7);
        // This is moving "7" in the numerator, even though we are possibly using the same index as when we used
        // when moving "1" in the numerator, because the indices of the abstract tokens may have shifted

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});

        int idx_1 = findLastAbstractTokenByName(hwEng.getTokenSet(), "1");
        idx_7 = findLastAbstractTokenByName(hwEng.getTokenSet(), "7");

        assertArrayEquals(newBounds_1, hwEng.getTokenBounds(idx_1), FLOAT_TOL);
        assertArrayEquals(newBounds_7, hwEng.getTokenBounds(idx_7), FLOAT_TOL);

        TokenSetParserOutput parseRes = hwEng.parseTokenSet();
        assertEquals("(71 / 72)", parseRes.getStringizerOutput());

    }

    @Test
    public void testSubsetParsingFollowedByTokenMoving_easyWay() throws HandwritingEngineException {
        /* Add 1st token "7" */
        addSeven(hwEng);
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false}, new String[] {"7"});

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {15, 30, 30, 15, 15, 30},
                new float[] {30, 30, 40, 40, 50, 50}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false, false}, new String[] {"7", "2"});

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {0, 10, 20, 30, 40},
                new float[] {25, 25, 25, 25, 25}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false, false, false}, new String[] {"7", "2", "-"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {11, 11, 11, 11, 11, 11},
                new float[] {5, 7, 9, 11, 13, 15}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false, false, false, false}, new String[] {"7", "2", "-", "1"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {14, 16, 18, 18, 17, 16},
                new float[] {5, 5, 5, 8, 12, 15}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {false, false, false, false, false}, new String[] {"7", "2", "-", "1", "7"});

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {true, false, false, false}, new String[] {"72", "-", "1", "7"});

        /* Verify bounds before moving */
        // Token indices are to abstract tokens
        assertArrayEquals(new float[] {11f, 5f, 11f, 15f}, hwEng.getTokenBounds(2), FLOAT_TOL);
        assertArrayEquals(new float[] {14f, 5f, 18f, 15f}, hwEng.getTokenBounds(3), FLOAT_TOL);

        /* Move tokens */
        final float[] newBounds_1 = new float[] {26f, 5f, 26f, 15f}; // New position for "1"
        final float[] newBounds_7 = new float[] {11f, 5f, 15f, 15f}; // New position for "7"

        // Token indices are to abstract tokens
        hwEng.moveTokens(new int[]{2, 3}, new float[][]{newBounds_1, newBounds_7});

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});

        // TODO: This may come as a little surprising and should be fixed. Due to the updateCurrentTokenSet() call
        // in HandwritingEngineImpl, the order of the abstract tokens may change after token move.
        int idx_7 = findLastAbstractTokenByName(hwEng.getTokenSet(), "7");

        hwEng.moveToken(idx_7, newBounds_7);
        // This is moving "7" in the numerator, even though we are possibly using the same index as when we used
        // when moving "1" in the numerator, because the indices of the abstract tokens may have shifted

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});

        int idx_1 = findLastAbstractTokenByName(hwEng.getTokenSet(), "1");
        idx_7 = findLastAbstractTokenByName(hwEng.getTokenSet(), "7");

        assertArrayEquals(newBounds_1, hwEng.getTokenBounds(idx_1), FLOAT_TOL);
        assertArrayEquals(newBounds_7, hwEng.getTokenBounds(idx_7), FLOAT_TOL);

        TokenSetParserOutput parseRes = hwEng.parseTokenSet();
        assertEquals("(71 / 72)", parseRes.getStringizerOutput());

    }

    @Test
    public void testSubsetParsingFollowedByRemoveMultipleTokens() throws HandwritingEngineException {
        /* Add 1st token "7" */
        addSeven(hwEng);
        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"7"});

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{15, 30, 30, 15, 15, 30},
                new float[]{30, 30, 40, 40, 50, 50}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false}, new String[]{"7", "2"});

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{0, 10, 20, 30, 40},
                new float[]{25, 25, 25, 25, 25}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false}, new String[]{"7", "2", "-"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{20, 20.1f, 20.2f, 20.3f, 20.4f},
                new float[]{0, 5, 10, 15, 20}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{true, false, false}, new String[]{"72", "-", "1"});

        /* Remove the tokens "72" (node token) and "-" (written token) with one function call */
        // The index is to the abstract token, not written token
        hwEng.removeTokens(new int[]{0, 1});

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"1"});

        assertEquals(hwEng.getTokenSet().getNumTokens(), hwEng.getWrittenTokenUUIDs().size());
    }

    @Test
    public void testSubsetParsingFollowedByAbstractTokenMove() throws HandwritingEngineException {
        /* Add 1st token "7" */
        addSeven(hwEng);
        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"7"});

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{15, 30, 30, 15, 15, 30},
                new float[]{30, 30, 40, 40, 50, 50}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false}, new String[]{"7", "2"});

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{0, 10, 20, 30, 40},
                new float[]{25, 25, 25, 25, 25}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false}, new String[]{"7", "2", "-"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{20, 20.1f, 20.2f, 20.3f, 20.4f},
                new float[]{0, 5, 10, 15, 20}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{true, false, false}, new String[]{"72", "-", "1"});

        assertArrayEquals(new float[]{5, 30, 30, 50}, hwEng.getTokenBounds(0), FLOAT_TOL); // Index is for abstract token
        assertArrayEquals(new float[] {20, 0, 20.4f, 20}, hwEng.getTokenBounds(2), FLOAT_TOL); // Index is for abstract token

        /* Move the abstract token "72" */
        final float[] newBounds_72 = new float[] {5, 0, 30, 20};
        hwEng.moveToken(0, newBounds_72);

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});
        // The abstract token should have been dissolved as a result of the move
        // TODO: Can we preserve it?

        /* Move the written token "1" */
        final float[] newBounds_1 = new float[] {20, 30, 20.4f, 50};
        hwEng.moveToken(3, newBounds_1);

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        // Individual written tokens should have been shifted as a result of the abstract token move
        assertArrayEquals(new float[] {5, 0, 14, 20}, hwEng.getTokenBounds(0), FLOAT_TOL);
        assertArrayEquals(new float[] {15, 0, 30, 20}, hwEng.getTokenBounds(1), FLOAT_TOL);
        assertArrayEquals(newBounds_1, hwEng.getTokenBounds(3), FLOAT_TOL); // Index is for abstract token

        // Parse the token set
        TokenSetParserOutput parseRes = hwEng.parseTokenSet();
        assertEquals("(72 / 1)", parseRes.getStringizerOutput());
    }

//    @Ignore // TODO: Fix the issue with numerical string differences such as:  9.999999682655225e-21 vs. 1e-20
    @Test
    public void testSerializeAndInjectState() throws HandwritingEngineException {
        /* Add 1st token "7" */
        addSeven(hwEng);
        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"7"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{15, 30, 30, 15, 15, 30},
                new float[]{30, 30, 40, 40, 50, 50}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false}, new String[]{"7", "2"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{0, 10, 20, 30, 40},
                new float[]{25, 25, 25, 25, 25}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false}, new String[]{"7", "2", "-"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{20, 20.1f, 20.2f, 20.3f, 20.4f},
                new float[]{0, 5, 10, 15, 20}));
        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{true, false, false}, new String[]{"72", "-", "1"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Remove the token "1" */
        // The index is to the abstract token, not written token
        hwEng.removeToken(2);

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {true, false}, new String[] {"72", "-"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Add two tokens: "11" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {20, 20.1f, 20.2f, 20.3f, 20.4f},
                new float[] {0, 5, 10, 15, 20}));
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {22, 22.1f, 22.2f, 22.3f, 22.4f},
                new float[] {0, 5, 10, 15, 20}));

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {true, false, false, false}, new String[] {"72", "-", "1", "1"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Parse the entire token set, which contains a node token */
        TokenSetParserOutput parseRes = hwEng.parseTokenSet();

        assertEquals("(11 / 72)", parseRes.getStringizerOutput());

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);
    }

    @Test
    public void testUndoRedo() throws HandwritingEngineException {
        /* Initial state */
        assertFalse(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{}, new String[]{});

        /* Add 1st token "7" */
        addSeven(hwEng);

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"7"});

        /* Undo and then redo 1st AddToken action */
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        assertNull(hwEng.getLastUserAction());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{}, new String[]{});

        assertFalse(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"7"});

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{15, 30, 30, 15, 15, 30},
                new float[]{30, 30, 40, 40, 50, 50}));

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false}, new String[]{"7", "2"});

        /* Undo and then redo 2nd AddToken action */
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"7"});

        assertTrue(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false}, new String[]{"7", "2"});

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{0, 10, 20, 30, 40},
                new float[]{25, 25, 25, 25, 25}));

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false}, new String[]{"7", "2", "-"});

        /* Undo and then redo 3rd AddToken action */
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false}, new String[]{"7", "2"});

        assertTrue(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false}, new String[]{"7", "2", "-"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{20, 20.1f, 20.2f, 20.3f, 20.4f},
                new float[]{0, 5, 10, 15, 20}));

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        /* Undo and then redo 4th AddToken action */
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false}, new String[]{"7", "2", "-"});

        assertTrue(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{true, false, false}, new String[]{"72", "-", "1"});

        /* Undo and redo the subset parsing */
        assertEquals(HandwritingEngineUserAction.ParseTokenSubset, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        assertTrue(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{true, false, false}, new String[]{"72", "-", "1"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Remove the token "1" */
        // The index is to the abstract token, not written token
        hwEng.removeToken(2);

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {true, false}, new String[] {"72", "-"});

        /* Undo and redo the RemoveToken action */
        assertEquals(HandwritingEngineUserAction.RemoveToken, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        assertEquals(HandwritingEngineUserAction.ParseTokenSubset, hwEng.getLastUserAction());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[]{true, false, false}, new String[]{"72", "-", "1"});

        assertTrue(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {true, false}, new String[] {"72", "-"});

        assertEquals(HandwritingEngineUserAction.RemoveToken, hwEng.getLastUserAction());

        /* Add two tokens: "11" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {20, 20.1f, 20.2f, 20.3f, 20.4f},
                new float[] {0, 5, 10, 15, 20}));
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {22, 22.1f, 22.2f, 22.3f, 22.4f},
                new float[] {0, 5, 10, 15, 20}));

        TestHelper.verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {true, false, false, false}, new String[] {"72", "-", "1", "1"});

        /* Undo and redo the two latest add token actions */
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        hwEng.undoUserAction();
        assertEquals(HandwritingEngineUserAction.RemoveToken, hwEng.getLastUserAction());

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {true, false}, new String[] {"72", "-"});

        assertTrue(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();
        hwEng.redoUserAction();

        TestHelper.verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1", "1"});
        TestHelper.verifyTokenSet(hwEng, new boolean[] {true, false, false, false}, new String[] {"72", "-", "1", "1"});

        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());

        /* Parse the entire token set, which contains a node token */
        TokenSetParserOutput parseRes = hwEng.parseTokenSet();

        assertEquals("(11 / 72)", parseRes.getStringizerOutput());

        /* The whole-token-set parsing should not be a part of the state stack */
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);
    }


    /* Test helper methods */
    // Round-trip verification of the JSON serialization and deserialization of handwriting engine state through
    // the extraction and injection of serialized state
    private void roundTripVerifyStateSerializationThruInjection(HandwritingEngine hwEng) {
        JsonObject stateJson0 = hwEng.getStateSerialization();
        String stateJsonStr0 = hwEng.getStateSerializationString();

        hwEng.injectSerializedState(stateJson0);

        String stateJsonStr1 = hwEng.getStateSerializationString();

        assertEquals(stateJsonStr0, stateJsonStr1);
    }

    private int findLastAbstractTokenByName(CAbstractWrittenTokenSet wtSet, String tokenName) {
        int tokenIdx = -1;

        for (int i = wtSet.getNumTokens() - 1; i >= 0; --i) {
            if (wtSet.getTokenName(i).equals(tokenName)) {
                tokenIdx = i;
                break;
            }
        }

        assertNotEquals(-1, tokenIdx);

        return tokenIdx;
    }

    private void addSeven(HandwritingEngine hwEng) throws HandwritingEngineException {
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {5.000f, 5.973f, 7.189f, 8.527f, 10.959f, 12.784f, 14.000f, 13.635f, 12.297f, 10.959f, 10.230f, 9.500f, 9.014f, 8.770f, 8.649f, 8.405f, 8.284f},
                                                 new float[] {30.899f, 30.449f, 30.225f, 30.000f, 30.000f, 30.000f, 30.449f, 32.247f, 35.169f, 38.090f, 39.888f, 41.910f, 43.933f, 45.730f, 47.753f, 48.876f, 50.000f}));
    }
    
}

