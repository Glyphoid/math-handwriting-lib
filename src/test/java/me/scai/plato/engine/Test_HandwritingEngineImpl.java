package me.scai.plato.engine;

import com.google.gson.JsonObject;
import me.scai.handwriting.*;
import me.scai.parsetree.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

public class Test_HandwritingEngineImpl {
    /* Constants */
    private static final float floatTol = 1e-9f;

    private static final String TEST_ROOT_DIR = TestHelper.TEST_ROOT_DIR;
    private static final String RESOURCES_DIR = "resources";
    private static final String RESOURCES_CONFIG_DIR = "config";
    private static final String STROKE_CURATOR_CONFIG_FILE = "stroke_curator_config.json";

    /* Member variables */
    private TokenSetParser tokenSetParser;

    private StrokeCuratorConfigurable strokeCurator;
    private HandwritingEngine hwEng;

    /* Constructor */
    @Before
    public void setUp() {
        // Create token engine
        TokenRecogEngine tokenRecogEngine = null;
        try {
            tokenRecogEngine = TestHelper.readTokenEngine();
        } catch (Exception exc) {
            fail("Failed to read token engine");
        }

        // Create stroke curator
        URL strokeCuratorConfigUrl = this.getClass().getClassLoader().getResource(TEST_ROOT_DIR +
                File.separator + RESOURCES_DIR +
                File.separator + RESOURCES_CONFIG_DIR +
                File.separator + STROKE_CURATOR_CONFIG_FILE);

        strokeCurator = new StrokeCuratorConfigurable(strokeCuratorConfigUrl, tokenRecogEngine);

        // Create parser, stringizer, evaluator, etc.
        TestHelper.WorkerTuple workerTuple = TestHelper.getTestWorkerTuple();

        tokenSetParser = workerTuple.tokenSetParser;

        // Create handwriting engine
        hwEng = new HandwritingEngineImpl(strokeCurator, tokenSetParser, workerTuple.gpSet, workerTuple.termSet);

    }

    @Test
    public void testSubsetParsingFollowedByRemoveTokenThenAddTokens() throws HandwritingEngineException {
        /* Add 1st token "7" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {5, 7, 9, 8, 7},
                                                 new float[] {30, 30, 30, 40, 50}));
        verifyWrittenTokenSet(hwEng, new String[] {"7"});
        verifyTokenSet(hwEng, new boolean[] {false}, new String[] {"7"});

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {15, 30, 30, 15, 15, 30},
                                                 new float[] {30, 30, 40, 40, 50, 50}));
        verifyWrittenTokenSet(hwEng, new String[] {"7", "2"});
        verifyTokenSet(hwEng, new boolean[] {false, false}, new String[] {"7", "2"});

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {0, 10, 20, 30, 40},
                                                 new float[] {25, 25, 25, 25, 25}));
        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[] {false, false, false}, new String[] {"7", "2", "-"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {20, 20.1f, 20.2f, 20.3f, 20.4f},
                                                 new float[] {0, 5, 10, 15, 20}));
        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[] {false, false, false, false}, new String[] {"7", "2", "-", "1"});

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[] {true, false, false}, new String[] {"72", "-", "1"});

        /* Remove the token "1" */
        // The index is to the abstract token, not written token
        hwEng.removeToken(2);

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[] {true, false}, new String[] {"72", "-"});

        /* Add two tokens: "11" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {20, 20.1f, 20.2f, 20.3f, 20.4f},
                                                 new float[] {0, 5, 10, 15, 20}));
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {22, 22.1f, 22.2f, 22.3f, 22.4f},
                                                 new float[] {0, 5, 10, 15, 20}));

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "1"});
        verifyTokenSet(hwEng, new boolean[] {true, false, false, false}, new String[] {"72", "-", "1", "1"});

        /* Parse the entire token set, which contains a node token */
        TokenSetParserOutput parseRes = hwEng.parseTokenSet();

        assertEquals("(11 / 72)", parseRes.getStringizerOutput());

    }

    @Test
     public void testSubsetParsingFollowedByTokenMoving_hardWay() throws HandwritingEngineException {
        /* Add 1st token "7" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {5, 7, 9, 8, 7},
                new float[] {30, 30, 30, 40, 50}));
        verifyWrittenTokenSet(hwEng, new String[] {"7"});
        verifyTokenSet(hwEng, new boolean[] {false}, new String[] {"7"});

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {15, 30, 30, 15, 15, 30},
                new float[] {30, 30, 40, 40, 50, 50}));
        verifyWrittenTokenSet(hwEng, new String[] {"7", "2"});
        verifyTokenSet(hwEng, new boolean[] {false, false}, new String[] {"7", "2"});

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {0, 10, 20, 30, 40},
                new float[] {25, 25, 25, 25, 25}));
        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[] {false, false, false}, new String[] {"7", "2", "-"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {11, 11, 11, 11, 11, 11},
                new float[] {5, 7, 9, 11, 13, 15}));
        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[] {false, false, false, false}, new String[] {"7", "2", "-", "1"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {14, 16, 18, 18, 17, 16},
                new float[] {5, 5, 5, 8, 12, 15}));
        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});
        verifyTokenSet(hwEng, new boolean[] {false, false, false, false, false}, new String[] {"7", "2", "-", "1", "7"});

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});
        verifyTokenSet(hwEng, new boolean[] {true, false, false, false}, new String[] {"72", "-", "1", "7"});

        /* Verify bounds before moving */
        // Token indices are to abstract tokens
        assertArrayEquals(new float[] {11f, 5f, 11f, 15f}, hwEng.getTokenBounds(2), floatTol);
        assertArrayEquals(new float[] {14f, 5f, 18f, 15f}, hwEng.getTokenBounds(3), floatTol);

        /* Move tokens */
        final float[] newBounds_1 = new float[] {26f, 5f, 26f, 15f}; // New position for "1"
        final float[] newBounds_7 = new float[] {11f, 5f, 15f, 15f}; // New position for "7"

        // Token indices are to abstract tokens
        hwEng.moveToken(2, newBounds_1);

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});

        // TODO: This may come as a little surprising and should be fixed. Due to the updateCurrentTokenSet() call
        // in HandwritingEngineImpl, the order of the abstract tokens may change after token move.
        int idx_7 = findLastAbstractTokenByName(hwEng.getTokenSet(), "7");

        hwEng.moveToken(idx_7, newBounds_7);
        // This is moving "7" in the numerator, even though we are possibly using the same index as when we used
        // when moving "1" in the numerator, because the indices of the abstract tokens may have shifted

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});

        int idx_1 = findLastAbstractTokenByName(hwEng.getTokenSet(), "1");
        idx_7 = findLastAbstractTokenByName(hwEng.getTokenSet(), "7");

        assertArrayEquals(newBounds_1, hwEng.getTokenBounds(idx_1), floatTol);
        assertArrayEquals(newBounds_7, hwEng.getTokenBounds(idx_7), floatTol);

        TokenSetParserOutput parseRes = hwEng.parseTokenSet();
        assertEquals("(71 / 72)", parseRes.getStringizerOutput());

    }

    @Test
    public void testSubsetParsingFollowedByTokenMoving_easyWay() throws HandwritingEngineException {
        /* Add 1st token "7" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {5, 7, 9, 8, 7},
                new float[] {30, 30, 30, 40, 50}));
        verifyWrittenTokenSet(hwEng, new String[] {"7"});
        verifyTokenSet(hwEng, new boolean[] {false}, new String[] {"7"});

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {15, 30, 30, 15, 15, 30},
                new float[] {30, 30, 40, 40, 50, 50}));
        verifyWrittenTokenSet(hwEng, new String[] {"7", "2"});
        verifyTokenSet(hwEng, new boolean[] {false, false}, new String[] {"7", "2"});

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {0, 10, 20, 30, 40},
                new float[] {25, 25, 25, 25, 25}));
        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[] {false, false, false}, new String[] {"7", "2", "-"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {11, 11, 11, 11, 11, 11},
                new float[] {5, 7, 9, 11, 13, 15}));
        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[] {false, false, false, false}, new String[] {"7", "2", "-", "1"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {14, 16, 18, 18, 17, 16},
                new float[] {5, 5, 5, 8, 12, 15}));
        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});
        verifyTokenSet(hwEng, new boolean[] {false, false, false, false, false}, new String[] {"7", "2", "-", "1", "7"});

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});
        verifyTokenSet(hwEng, new boolean[] {true, false, false, false}, new String[] {"72", "-", "1", "7"});

        /* Verify bounds before moving */
        // Token indices are to abstract tokens
        assertArrayEquals(new float[] {11f, 5f, 11f, 15f}, hwEng.getTokenBounds(2), floatTol);
        assertArrayEquals(new float[] {14f, 5f, 18f, 15f}, hwEng.getTokenBounds(3), floatTol);

        /* Move tokens */
        final float[] newBounds_1 = new float[] {26f, 5f, 26f, 15f}; // New position for "1"
        final float[] newBounds_7 = new float[] {11f, 5f, 15f, 15f}; // New position for "7"

        // Token indices are to abstract tokens
        hwEng.moveTokens(new int[]{2, 3}, new float[][]{newBounds_1, newBounds_7});

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});

        // TODO: This may come as a little surprising and should be fixed. Due to the updateCurrentTokenSet() call
        // in HandwritingEngineImpl, the order of the abstract tokens may change after token move.
        int idx_7 = findLastAbstractTokenByName(hwEng.getTokenSet(), "7");

        hwEng.moveToken(idx_7, newBounds_7);
        // This is moving "7" in the numerator, even though we are possibly using the same index as when we used
        // when moving "1" in the numerator, because the indices of the abstract tokens may have shifted

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "7"});

        int idx_1 = findLastAbstractTokenByName(hwEng.getTokenSet(), "1");
        idx_7 = findLastAbstractTokenByName(hwEng.getTokenSet(), "7");

        assertArrayEquals(newBounds_1, hwEng.getTokenBounds(idx_1), floatTol);
        assertArrayEquals(newBounds_7, hwEng.getTokenBounds(idx_7), floatTol);

        TokenSetParserOutput parseRes = hwEng.parseTokenSet();
        assertEquals("(71 / 72)", parseRes.getStringizerOutput());

    }

    @Test
    public void testSubsetParsingFollowedByRemoveMultipleTokens() throws HandwritingEngineException {
        /* Add 1st token "7" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{5, 7, 9, 8, 7},
                new float[]{30, 30, 30, 40, 50}));
        verifyWrittenTokenSet(hwEng, new String[]{"7"});
        verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"7"});

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{15, 30, 30, 15, 15, 30},
                new float[]{30, 30, 40, 40, 50, 50}));
        verifyWrittenTokenSet(hwEng, new String[]{"7", "2"});
        verifyTokenSet(hwEng, new boolean[]{false, false}, new String[]{"7", "2"});

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{0, 10, 20, 30, 40},
                new float[]{25, 25, 25, 25, 25}));
        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false}, new String[]{"7", "2", "-"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{20, 20.1f, 20.2f, 20.3f, 20.4f},
                new float[]{0, 5, 10, 15, 20}));
        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{true, false, false}, new String[]{"72", "-", "1"});

        /* Remove the tokens "72" (node token) and "-" (written token) with one function call */
        // The index is to the abstract token, not written token
        hwEng.removeTokens(new int[]{0, 1});

        verifyWrittenTokenSet(hwEng, new String[]{"1"});
        verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"1"});

        assertEquals(hwEng.getTokenSet().getNumTokens(), hwEng.getWrittenTokenUUIDs().size());
    }

    @Test
    public void testSubsetParsingFollowedByAbstractTokenMove() throws HandwritingEngineException {
        /* Add 1st token "7" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{5, 7, 9, 8, 7},
                new float[]{30, 30, 30, 40, 50}));
        verifyWrittenTokenSet(hwEng, new String[]{"7"});
        verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"7"});

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{15, 30, 30, 15, 15, 30},
                new float[]{30, 30, 40, 40, 50, 50}));
        verifyWrittenTokenSet(hwEng, new String[]{"7", "2"});
        verifyTokenSet(hwEng, new boolean[]{false, false}, new String[]{"7", "2"});

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{0, 10, 20, 30, 40},
                new float[]{25, 25, 25, 25, 25}));
        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false}, new String[]{"7", "2", "-"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{20, 20.1f, 20.2f, 20.3f, 20.4f},
                new float[]{0, 5, 10, 15, 20}));
        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{true, false, false}, new String[]{"72", "-", "1"});

        assertArrayEquals(new float[]{5, 30, 30, 50}, hwEng.getTokenBounds(0), floatTol); // Index is for abstract token
        assertArrayEquals(new float[] {20, 0, 20.4f, 20}, hwEng.getTokenBounds(2), floatTol); // Index is for abstract token

        /* Move the abstract token "72" */
        final float[] newBounds_72 = new float[] {5, 0, 30, 20};
        hwEng.moveToken(0, newBounds_72);

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});
        // The abstract token should have been dissolved as a result of the move
        // TODO: Can we preserve it?

        /* Move the written token "1" */
        final float[] newBounds_1 = new float[] {20, 30, 20.4f, 50};
        hwEng.moveToken(3, newBounds_1);

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        // Individual written tokens should have been shifted as a result of the abstract token move
        assertArrayEquals(new float[] {5, 0, 9, 20}, hwEng.getTokenBounds(0), floatTol);
        assertArrayEquals(new float[] {15, 0, 30, 20}, hwEng.getTokenBounds(1), floatTol);
        assertArrayEquals(newBounds_1, hwEng.getTokenBounds(3), floatTol); // Index is for abstract token

        // Parse the token set
        TokenSetParserOutput parseRes = hwEng.parseTokenSet();
        assertEquals("(72 / 1)", parseRes.getStringizerOutput());
    }

    @Test
    public void testSerializeAndInjectState() throws HandwritingEngineException {
        /* Add 1st token "7" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{5, 7, 9, 8, 7},
                new float[]{30, 30, 30, 40, 50}));
        verifyWrittenTokenSet(hwEng, new String[]{"7"});
        verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"7"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{15, 30, 30, 15, 15, 30},
                new float[]{30, 30, 40, 40, 50, 50}));
        verifyWrittenTokenSet(hwEng, new String[]{"7", "2"});
        verifyTokenSet(hwEng, new boolean[]{false, false}, new String[]{"7", "2"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{0, 10, 20, 30, 40},
                new float[]{25, 25, 25, 25, 25}));
        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false}, new String[]{"7", "2", "-"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{20, 20.1f, 20.2f, 20.3f, 20.4f},
                new float[]{0, 5, 10, 15, 20}));
        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{true, false, false}, new String[]{"72", "-", "1"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Remove the token "1" */
        // The index is to the abstract token, not written token
        hwEng.removeToken(2);

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[] {true, false}, new String[] {"72", "-"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Add two tokens: "11" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {20, 20.1f, 20.2f, 20.3f, 20.4f},
                new float[] {0, 5, 10, 15, 20}));
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {22, 22.1f, 22.2f, 22.3f, 22.4f},
                new float[] {0, 5, 10, 15, 20}));

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "1"});
        verifyTokenSet(hwEng, new boolean[] {true, false, false, false}, new String[] {"72", "-", "1", "1"});

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

        verifyWrittenTokenSet(hwEng, new String[]{});
        verifyTokenSet(hwEng, new boolean[]{}, new String[]{});

        /* Add 1st token "7" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{5, 7, 9, 8, 7},
                new float[]{30, 30, 30, 40, 50}));

        verifyWrittenTokenSet(hwEng, new String[]{"7"});
        verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"7"});

        /* Undo and then redo 1st AddToken action */
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        assertNull(hwEng.getLastUserAction());

        verifyWrittenTokenSet(hwEng, new String[]{});
        verifyTokenSet(hwEng, new boolean[]{}, new String[]{});

        assertFalse(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();

        verifyWrittenTokenSet(hwEng, new String[]{"7"});
        verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"7"});

        /* Add 2nd stroke: "2" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{15, 30, 30, 15, 15, 30},
                new float[]{30, 30, 40, 40, 50, 50}));

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2"});
        verifyTokenSet(hwEng, new boolean[]{false, false}, new String[]{"7", "2"});

        /* Undo and then redo 2nd AddToken action */
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());

        verifyWrittenTokenSet(hwEng, new String[]{"7"});
        verifyTokenSet(hwEng, new boolean[]{false}, new String[]{"7"});

        assertTrue(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2"});
        verifyTokenSet(hwEng, new boolean[]{false, false}, new String[]{"7", "2"});

        /* Add 3rd stroke: "-" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{0, 10, 20, 30, 40},
                new float[]{25, 25, 25, 25, 25}));

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false}, new String[]{"7", "2", "-"});

        /* Undo and then redo 3rd AddToken action */
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2"});
        verifyTokenSet(hwEng, new boolean[]{false, false}, new String[]{"7", "2"});

        assertTrue(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false}, new String[]{"7", "2", "-"});

        /* Add 4th stroke: "1" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[]{20, 20.1f, 20.2f, 20.3f, 20.4f},
                new float[]{0, 5, 10, 15, 20}));

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        /* Undo and then redo 4th AddToken action */
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false}, new String[]{"7", "2", "-"});

        assertTrue(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        /* Subset parsing "72" */
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSubset(new int[]{0, 1});
        assertEquals("72", subsetParseRes.getStringizerOutput());

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{true, false, false}, new String[]{"72", "-", "1"});

        /* Undo and redo the subset parsing */
        assertEquals(HandwritingEngineUserAction.ParseTokenSubset, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{false, false, false, false}, new String[]{"7", "2", "-", "1"});

        assertTrue(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{true, false, false}, new String[]{"72", "-", "1"});

        /* Test state injection round trip */
        roundTripVerifyStateSerializationThruInjection(hwEng);

        /* Remove the token "1" */
        // The index is to the abstract token, not written token
        hwEng.removeToken(2);

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[] {true, false}, new String[] {"72", "-"});

        /* Undo and redo the RemoveToken action */
        assertEquals(HandwritingEngineUserAction.RemoveToken, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        assertEquals(HandwritingEngineUserAction.ParseTokenSubset, hwEng.getLastUserAction());

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1"});
        verifyTokenSet(hwEng, new boolean[]{true, false, false}, new String[]{"72", "-", "1"});

        assertTrue(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[] {true, false}, new String[] {"72", "-"});

        assertEquals(HandwritingEngineUserAction.RemoveToken, hwEng.getLastUserAction());

        /* Add two tokens: "11" */
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {20, 20.1f, 20.2f, 20.3f, 20.4f},
                new float[] {0, 5, 10, 15, 20}));
        hwEng.addStroke(TestHelper.getMockStroke(new float[] {22, 22.1f, 22.2f, 22.3f, 22.4f},
                new float[] {0, 5, 10, 15, 20}));

        verifyWrittenTokenSet(hwEng, new String[] {"7", "2", "-", "1", "1"});
        verifyTokenSet(hwEng, new boolean[] {true, false, false, false}, new String[] {"72", "-", "1", "1"});

        /* Undo and redo the two latest add token actions */
        assertEquals(HandwritingEngineUserAction.AddStroke, hwEng.getLastUserAction());
        assertTrue(hwEng.canUndoUserAction());
        assertFalse(hwEng.canRedoUserAction());

        hwEng.undoUserAction();
        hwEng.undoUserAction();
        assertEquals(HandwritingEngineUserAction.RemoveToken, hwEng.getLastUserAction());

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-"});
        verifyTokenSet(hwEng, new boolean[] {true, false}, new String[] {"72", "-"});

        assertTrue(hwEng.canUndoUserAction());
        assertTrue(hwEng.canRedoUserAction());

        hwEng.redoUserAction();
        hwEng.redoUserAction();

        verifyWrittenTokenSet(hwEng, new String[]{"7", "2", "-", "1", "1"});
        verifyTokenSet(hwEng, new boolean[] {true, false, false, false}, new String[] {"72", "-", "1", "1"});

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

    private void verifyTokenSet(HandwritingEngine hwEng, boolean[] isNodeToken, String[] trueTokenNames) {
        final int tnt = isNodeToken.length; // True number of tokens

        assert(trueTokenNames.length == tnt);

        CWrittenTokenSetNoStroke wtSet = (CWrittenTokenSetNoStroke) hwEng.getTokenSet();

        assertEquals(tnt, wtSet.getNumTokens());

        for (int i = 0; i < tnt; ++i) {
            if (isNodeToken[i]) {
                assertTrue(wtSet.tokens.get(i) instanceof NodeToken);
            } else {
                assertTrue(wtSet.tokens.get(i) instanceof CWrittenToken);
            }

            assertEquals(trueTokenNames[i], wtSet.tokens.get(i).getRecogResult());
        }
    }

    /* Test helper methods */
    public void verifyWrittenTokenSet(HandwritingEngine hwEng, String[] trueTokenNames) {
        CWrittenTokenSet wtSet = hwEng.getWrittenTokenSet();

        final int tnt = trueTokenNames.length; // True number of tokens
        assertEquals(tnt, wtSet.getNumTokens());

        for (int i = 0; i < tnt; ++i) {
            assertEquals(trueTokenNames[i], wtSet.tokens.get(i).getRecogResult());
        }
    }



}

