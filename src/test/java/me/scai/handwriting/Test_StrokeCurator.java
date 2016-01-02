package me.scai.handwriting;

import com.google.gson.JsonParser;
import me.scai.plato.helpers.CStrokeJsonHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;

public class Test_StrokeCurator {
    private static final String TEST_ROOT_DIR = TestHelper.TEST_ROOT_DIR;
    private static final String RESOURCES_DIR = "resources";
    private static final String RESOURCES_CONFIG_DIR = "config";
    private static final String STROKE_CURATOR_CONFIG_FILE = "stroke_curator_config.json";

    private static final String RESOURCES_TOKEN_ENGINE_DIR = "token_engine";
    private static final String TOKEN_ENGINE_FILE_NAME = "token_engine.sdv.sz0_whr1_ns1.ser";

    private StrokeCurator curator;
    private StrokeCurator curatorPrime;

    @Before
    public void setup() {
        URL strokeCuratorConfigUrl = this.getClass().getClassLoader().getResource(TEST_ROOT_DIR +
                File.separator + RESOURCES_DIR +
                File.separator + RESOURCES_CONFIG_DIR +
                File.separator + STROKE_CURATOR_CONFIG_FILE);

        URL tokenEngineFileUrl = this.getClass().getClassLoader().getResource(TEST_ROOT_DIR +
                File.separator + RESOURCES_DIR +
                File.separator + RESOURCES_TOKEN_ENGINE_DIR +
                File.separator + TOKEN_ENGINE_FILE_NAME);

        TokenRecogEngine tokenRecogEngine = null;
        try {
            tokenRecogEngine = TestHelper.readTokenEngine();
        } catch (Exception exc) {
            fail("Failed to read token engine");
        }

        curator = new StrokeCuratorConfigurable(strokeCuratorConfigUrl, tokenRecogEngine);

        // For state injection test
        curatorPrime = new StrokeCuratorConfigurable(strokeCuratorConfigUrl, tokenRecogEngine);
    }

    @Test
    public void testMergeStrokes() {
        /* Add 1st stroke: "-" */
        CStroke stroke0 = new CStroke(0.0f, 0.0f);
        stroke0.addPoint(10f, 0.0f);
        stroke0.addPoint(20f, 0.0f);
        stroke0.addPoint(30f, 0.0f);
        stroke0.addPoint(40f, 0.0f);

        curator.addStroke(stroke0);

        assertEquals(1, curator.getWrittenTokenRecogWinners().size());
        assertEquals(1, curator.getTokenUuids().size());
        assertEquals("-", curator.getWrittenTokenRecogWinners().get(0));


        /* Add 2nd stroke: "-": 1st stroke of T */
        CStroke stroke1 = new CStroke(50f, -10f);
        stroke1.addPoint(60f, -10f);
        stroke1.addPoint(70f, -10f);
        stroke1.addPoint(80f, -10f);

        curator.addStroke(stroke1);

        assertEquals(2, curator.getWrittenTokenRecogWinners().size());
        assertEquals(2, curator.getTokenUuids().size());
        assertEquals("-", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals("-", curator.getWrittenTokenRecogWinners().get(1));

        /* Add 3rd stroke: "1": 2nd stroke of T */
        CStroke stroke2 = new CStroke(65.1f, -5f);
        stroke2.addPoint(65.2f, 5f);
        stroke2.addPoint(65.3f, 15f);
        stroke2.addPoint(65.4f, 25f);

        curator.addStroke(stroke2);

        assertEquals(3, curator.getWrittenTokenRecogWinners().size());
        assertEquals(3, curator.getTokenUuids().size());
        assertEquals("-", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals("-", curator.getWrittenTokenRecogWinners().get(1));
        assertEquals("1", curator.getWrittenTokenRecogWinners().get(2));

        List<String> uuidsBefore = curator.getTokenUuids();

        /* Merge the last two strokes as "T" */
        curator.mergeStrokesAsToken(new int[] {1, 2});

        assertEquals(2, curator.getWrittenTokenRecogWinners().size());
        assertEquals(2, curator.getTokenUuids().size());
        assertEquals("-", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals("T", curator.getWrittenTokenRecogWinners().get(1));

        List<String> uuidsAfter = curator.getTokenUuids();

        // The first token ("-") is not affected by the merger, so its UUID should not change
        assertEquals(uuidsBefore.get(0), uuidsAfter.get(0));

        // The 2nd tokens should now be different due to the merging
        assertNotEquals(uuidsBefore.get(1), uuidsAfter.get(1));
    }

    @Test
    public void testUnmergeStroke1() {
        /* Add 1st stroke of "=" */
        CStroke stroke0 = new CStroke(0.0f, 0.0f);
        stroke0.addPoint(10f, 0.0f);
        stroke0.addPoint(20f, 0.0f);
        stroke0.addPoint(30f, 0.0f);
        stroke0.addPoint(40f, 0.0f);

        curator.addStroke(stroke0);

        assertEquals(curator.getWrittenTokenRecogWinners().size(), 1);
        assertEquals(curator.getWrittenTokenRecogWinners().get(0), "-");
        assertEquals(1, curator.getTokenUuids().size());

        /* Add 2nd stroke of "=" */
        CStroke stroke1 = new CStroke(0.0f, 10f);
        stroke1.addPoint(10f, 10f);
        stroke1.addPoint(20f, 10f);
        stroke1.addPoint(30f, 10f);
        stroke1.addPoint(40f, 10f);

        curator.addStroke(stroke1);

        assertEquals(curator.getWrittenTokenRecogWinners().size(), 1);
        assertEquals(curator.getWrittenTokenRecogWinners().get(0), "=");
        assertEquals(1, curator.getTokenUuids().size());

        /* Unmerge the 2nd stroke */
        curator.mergeStrokesAsToken(new int[] {1});

        assertEquals(curator.getWrittenTokenRecogWinners().size(), 2);
        assertEquals(curator.getWrittenTokenRecogWinners().get(0), "-");
        assertEquals(curator.getWrittenTokenRecogWinners().get(1), "-");

        assertEquals(2, curator.getTokenUuids().size());
        assertNotEquals(curator.getTokenUuid(0), curator.getTokenUuid(1));
    }

    @Test
    public void testUnmergeStroke2() {
        /* Add 1st stroke of "T" */
        CStroke stroke0 = new CStroke(0.0f, 0.0f);
        stroke0.addPoint(10f, 0.0f);
        stroke0.addPoint(20f, 0.0f);
        stroke0.addPoint(30f, 0.0f);
        stroke0.addPoint(40f, 0.0f);

        curator.addStroke(stroke0);

        assertEquals(curator.getWrittenTokenRecogWinners().size(), 1);
        assertEquals(curator.getWrittenTokenRecogWinners().get(0), "-");
        assertEquals(1, curator.getTokenUuids().size());

        /* Add 2nd stroke of "T" */
        CStroke stroke1 = new CStroke(20f, 0f);
        stroke1.addPoint(20f, 10f);
        stroke1.addPoint(20f, 20f);
        stroke1.addPoint(20f, 30f);
        stroke1.addPoint(20f, 40f);

        curator.addStroke(stroke1);

        assertEquals(1, curator.getWrittenTokenRecogWinners().size());
        assertEquals("T", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals(1, curator.getTokenUuids().size());

        /* Add 1st stroke of "=" */
        CStroke stroke2 = new CStroke(50.0f, 0.0f);
        stroke2.addPoint(60f, 0.0f);
        stroke2.addPoint(70f, 0.0f);
        stroke2.addPoint(80f, 0.0f);
        stroke2.addPoint(90f, 0.0f);

        curator.addStroke(stroke2);

        assertEquals(2, curator.getWrittenTokenRecogWinners().size());
        assertEquals("T", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals("-", curator.getWrittenTokenRecogWinners().get(1));
        assertEquals(2, curator.getTokenUuids().size());

        /* Add 1st stroke of "=" */
        CStroke stroke3 = new CStroke(50.0f, 20f);
        stroke3.addPoint(60f, 20f);
        stroke3.addPoint(70f, 20f);
        stroke3.addPoint(80f, 20f);
        stroke3.addPoint(90f, 20f);

        curator.addStroke(stroke3);

        assertEquals(2, curator.getWrittenTokenRecogWinners().size());
        assertEquals("T", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals("=", curator.getWrittenTokenRecogWinners().get(1));
        assertEquals(4, curator.getNumStrokes());
        assertEquals(2, curator.getTokenUuids().size());

        /* Unmerge the 2nd stroke of "=" */
        curator.mergeStrokesAsToken(new int[] {3});

        assertEquals(curator.getWrittenTokenRecogWinners().size(), 3);
        assertEquals(curator.getWrittenTokenRecogWinners().get(0), "T");
        assertEquals(curator.getWrittenTokenRecogWinners().get(1), "-");
        assertEquals(curator.getWrittenTokenRecogWinners().get(2), "-");

        assertEquals(3, curator.getTokenUuids().size());
        assertNotEquals(curator.getTokenUuid(0), curator.getTokenUuid(1));
        assertNotEquals(curator.getTokenUuid(1), curator.getTokenUuid(2));
        assertNotEquals(curator.getTokenUuid(2), curator.getTokenUuid(0));
    }

    @Test
    public void testSerializeDeserialize() {
        CStroke[] strokesToAdd = new CStroke[4];

        /* stroke0 and stroke1 constitute "=" */
        CStroke stroke0 = new CStroke();
        stroke0.addPoint(0.0f, 0.0f);
        stroke0.addPoint(10.0f, 0.1f);
        stroke0.addPoint(20.0f, 0.0f);
        stroke0.addPoint(30.0f, 0.05f);

        CStroke stroke1 = new CStroke();
        stroke1.addPoint(0.0f, 10.0f);
        stroke1.addPoint(10.0f, 10.95f);
        stroke1.addPoint(20.0f, 10.0f);
        stroke1.addPoint(30.0f, 10.02f);

        /* stroke2 and stroke3 constitute "T". But they need to be merged manually.
         * But eventually it'll be force-recognized as "+" instead */
        CStroke stroke2 = new CStroke();
        stroke2.addPoint(40.0f, -10.0f);
        stroke2.addPoint(50.0f, -10.1f);
        stroke2.addPoint(60.0f, -10.2f);
        stroke2.addPoint(70.0f, -10.1f);

        CStroke stroke3 = new CStroke();
        stroke3.addPoint(55.0f, -10.0f);
        stroke3.addPoint(55.1f, 0.0f);
        stroke3.addPoint(55.0f, 10.0f);
        stroke3.addPoint(55.1f, 20.1f);

        strokesToAdd[0] = stroke0;
        strokesToAdd[1] = stroke1;
        strokesToAdd[2] = stroke2;
        strokesToAdd[3] = stroke3;

        for (CStroke strokeToAdd : strokesToAdd) {
            curator.addStroke(strokeToAdd);
        }

        /* Merge stroke2 and stroke3 to get "T" */
        curator.mergeStrokesAsToken(new int[] {2, 3});

        assertEquals(2, curator.getNumTokens());
        curator.forceSetRecogWinner(1, "+");

        List<String> serializedStrokes = curator.getSerializedStrokes();

        assertEquals(strokesToAdd.length, serializedStrokes.size());
        for (int i = 0; i < serializedStrokes.size(); ++i) {
            String serializedStroke = serializedStrokes.get(i);

            try {
                CStroke stroke = CStrokeJsonHelper.json2CStroke(serializedStroke);
                assertEquals(strokesToAdd[i].nPoints(), stroke.nPoints());
            } catch (CStrokeJsonHelper.CStrokeJsonConversionException exc) {
                fail("Failed due to " + exc.getMessage());
            }
        }

        String serializedTokenSet = curator.getSerializedTokenSet();
        assertNotNull(serializedTokenSet);
        assertFalse(serializedTokenSet.isEmpty());

        String serializedWtConstStrokeIndices = curator.getSerializedConstStrokeIndices();
        assertNotNull(serializedWtConstStrokeIndices);
        assertFalse(serializedWtConstStrokeIndices.isEmpty());

        String serializedState = curator.getStateSerializationString();
        assertNotNull(serializedState);
        assertFalse(serializedState.isEmpty());

        /* Deserializatoin / injection */
        curatorPrime.injectSerializedState((new JsonParser()).parse(serializedState).getAsJsonObject());

        assertEquals(curator.getNumStrokes(), curatorPrime.getNumStrokes());
        assertEquals(curator.getNumTokens(), curatorPrime.getNumTokens());

        assertEquals(curator.getWrittenTokenSet().recogWinners.size(), curatorPrime.getWrittenTokenSet().recogWinners.size());
        assertEquals(curator.getWrittenTokenSet().recogPs.size(), curatorPrime.getWrittenTokenSet().recogPs.size());

        // Check deserialized token UUIDs
        assertEquals(curator.getTokenUuids().size(), curatorPrime.getTokenUuids().size());
        for (int i = 0; i < curator.getTokenUuids().size(); ++i) {
            assertEquals(curator.getTokenUuid(i), curatorPrime.getTokenUuid(i));
        }

        for (int i = 0; i < curator.getWrittenTokenSet().recogWinners.size(); ++i) {
            assertEquals(curator.getWrittenTokenRecogWinners().get(i),
                         curatorPrime.getWrittenTokenRecogWinners().get(i));

            assertArrayEquals(curator.getWrittenTokenRecogPs().get(i),
                              curatorPrime.getWrittenTokenRecogPs().get(i), 1e-9);
        }

        assertEquals(curator.getWrittenTokenConstStrokeIndices().size(), curatorPrime.getWrittenTokenConstStrokeIndices().size());
        List<int[]> constStrokeIndices = curator.getWrittenTokenConstStrokeIndices();
        List<int[]> primeConstStrokeIndices = curatorPrime.getWrittenTokenConstStrokeIndices();
        for (int i = 0; i < constStrokeIndices.size(); ++i) {
            assertArrayEquals(constStrokeIndices.get(i), primeConstStrokeIndices.get(i));
        }

    }

    /* Test undo / redo stack */
    @Test
    public void testUndoRedo() {
        final float tol = 1E-9f;

        /* stroke0 and stroke1 constitute "=" */
        CStroke stroke0 = new CStroke();
        stroke0.addPoint(0.0f, 0.0f);
        stroke0.addPoint(10.0f, 0.1f);
        stroke0.addPoint(20.0f, 0.0f);
        stroke0.addPoint(30.0f, 0.05f);

        CStroke stroke1 = new CStroke();
        stroke1.addPoint(0.0f, 10.0f);
        stroke1.addPoint(10.0f, 10.95f);
        stroke1.addPoint(20.0f, 10.0f);
        stroke1.addPoint(30.0f, 10.02f);

        /* stroke2 and stroke3 constitute "T". But they need to be merged manually.
         * But eventually it'll be force-recognized as "+" instead */
        CStroke stroke2 = new CStroke();
        stroke2.addPoint(40.0f, -10.0f);
        stroke2.addPoint(50.0f, -10.1f);
        stroke2.addPoint(60.0f, -10.2f);
        stroke2.addPoint(70.0f, -10.1f);

        CStroke stroke3 = new CStroke();
        stroke3.addPoint(55.0f, -10.0f);
        stroke3.addPoint(55.0f, 0.0f);
        stroke3.addPoint(55.0f, 10.0f);
        stroke3.addPoint(55.0f, 20.0f);

        // State stack is empty initially
        assertNull(curator.getLastUserAction());

        // Initially the state stack should be empty. An exception should be thrown if you attempt to undo or redo.
        IllegalStateException caughtException = null;

        try {
            curator.undoUserAction();
        } catch (IllegalStateException e) {
            caughtException = e;
        }
        assertNotNull(caughtException);

        caughtException = null;

        try {
            curator.redoUserAction();
        } catch (IllegalStateException e) {
            caughtException = e;
        }
        assertNotNull(caughtException);

        assertFalse(curator.canUndoUserAction());
        assertFalse(curator.canRedoUserAction());

        // Add the first stroke
        curator.addStroke(stroke0);
        assertEquals(StrokeCuratorUserAction.AddStroke, curator.getLastUserAction());
        assertTrue(curator.canUndoUserAction());
        assertFalse(curator.canRedoUserAction());

        assertEquals(1, curator.getNumTokens());
        assertEquals(1, curator.getNumStrokes());
        assertEquals(1, curator.getWrittenTokenRecogWinners().size());
        assertEquals("-", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals(1, curator.getTokenUuids().size());
        assertEquals(1, curator.getTokenUuids().size());
        String token0Uuid = curator.getTokenUuid(0);

        // Add the second stroke
        curator.addStroke(stroke1);
        assertEquals(StrokeCuratorUserAction.AddStroke, curator.getLastUserAction());
        assertTrue(curator.canUndoUserAction());
        assertFalse(curator.canRedoUserAction());

        assertEquals(1, curator.getNumTokens());
        assertEquals(2, curator.getNumStrokes());
        assertEquals(1, curator.getWrittenTokenRecogWinners().size());
        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals(1, curator.getTokenUuids().size());
        String tokenUuidEqualSign = curator.getTokenUuid(0);

        // Undo the AddStroke action
        curator.undoUserAction();

        assertTrue(curator.canUndoUserAction());
        assertTrue(curator.canRedoUserAction());

        assertEquals(1, curator.getNumTokens());
        assertEquals(1, curator.getNumStrokes());
        assertEquals(1, curator.getWrittenTokenRecogWinners().size());
        assertEquals("-", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals(1, curator.getTokenUuids().size());
        assertEquals(token0Uuid, curator.getTokenUuid(0)); // The UUID shouldn't have changed

        // Redo the AddStroke action
        curator.redoUserAction();

        assertTrue(curator.canUndoUserAction());
        assertFalse(curator.canRedoUserAction());

        assertEquals(1, curator.getNumTokens());
        assertEquals(2, curator.getNumStrokes());
        assertEquals(1, curator.getWrittenTokenRecogWinners().size());
        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals(1, curator.getTokenUuids().size());
        assertEquals(tokenUuidEqualSign, curator.getTokenUuid(0)); // The UUID shouldn't have changed

        float[] tokenBounds = curator.getWrittenTokenSet().getTokenBounds(0);
        assertArrayEquals(new float[] {0.0f, 0.0f, 30.0f, 10.95f}, tokenBounds, tol);

        // Move token
        curator.moveToken(0, new float[] {1.0f, 1.0f, 31.0f, 11.95f});

        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));
        tokenBounds = curator.getWrittenTokenSet().getTokenBounds(0);
        assertArrayEquals(new float[] {1.0f, 1.0f, 31.0f, 11.95f}, tokenBounds, tol);

        assertEquals(StrokeCuratorUserAction.MoveToken, curator.getLastUserAction());
        assertTrue(curator.canUndoUserAction());
        assertFalse(curator.canRedoUserAction());

        // Undo MoveToken
        curator.undoUserAction();

        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));
        tokenBounds = curator.getWrittenTokenSet().getTokenBounds(0);
        assertArrayEquals(new float[] {0.0f, 0.0f, 30.0f, 10.95f}, tokenBounds, tol);

        assertEquals(StrokeCuratorUserAction.AddStroke, curator.getLastUserAction());
        assertTrue(curator.canUndoUserAction());
        assertTrue(curator.canRedoUserAction());

        // Redo MoveToken
        curator.redoUserAction();

        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));
        tokenBounds = curator.getWrittenTokenSet().getTokenBounds(0);
        assertArrayEquals(new float[] {1.0f, 1.0f, 31.0f, 11.95f}, tokenBounds, tol);

        assertEquals(StrokeCuratorUserAction.MoveToken, curator.getLastUserAction());
        assertTrue(curator.canUndoUserAction());
        assertFalse(curator.canRedoUserAction());

        // Undo the MoveToken and two AddStroke actions
        curator.undoUserAction();
        curator.undoUserAction();
        curator.undoUserAction();

        assertFalse(curator.canUndoUserAction());
        assertTrue(curator.canRedoUserAction());

        assertEquals(0, curator.getNumTokens());
        assertEquals(0, curator.getNumStrokes());

        // Redo both AddStroke actions
        curator.redoUserAction();
        curator.redoUserAction();

        assertTrue(curator.canUndoUserAction());
        assertTrue(curator.canRedoUserAction());

        assertEquals(1, curator.getNumTokens());
        assertEquals(2, curator.getNumStrokes());
        assertEquals(1, curator.getWrittenTokenRecogWinners().size());
        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));

        // Unmerge the 2nd stroke
        curator.mergeStrokesAsToken(new int[] {1});
        assertEquals(2, curator.getNumTokens());
        assertEquals(2, curator.getNumStrokes());
        assertEquals(2, curator.getWrittenTokenRecogWinners().size());
        assertEquals("-", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals("-", curator.getWrittenTokenRecogWinners().get(1));
        assertEquals(2, curator.getTokenUuids().size());
        assertNotEquals(curator.getTokenUuid(0), curator.getTokenUuid(1));

        assertTrue(curator.canUndoUserAction());
        assertFalse(curator.canRedoUserAction());

        // The internal implementation of the unmerge action is actually merge
        assertEquals(StrokeCuratorUserAction.MergeStrokesAsToken, curator.getLastUserAction());

        // Undo the unmerge (i.e., implemented as merge internally)
        curator.undoUserAction();

        assertTrue(curator.canUndoUserAction());
        assertTrue(curator.canRedoUserAction());

        assertEquals(1, curator.getNumTokens());
        assertEquals(2, curator.getNumStrokes());
        assertEquals(1, curator.getWrittenTokenRecogWinners().size());
        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals(StrokeCuratorUserAction.AddStroke, curator.getLastUserAction());

        // Force set token name
        curator.forceSetRecogWinner(0, "A");

        assertTrue(curator.canUndoUserAction());
        assertFalse(curator.canRedoUserAction());

        assertEquals(1, curator.getNumTokens());
        assertEquals(2, curator.getNumStrokes());
        assertEquals(1, curator.getWrittenTokenRecogWinners().size());
        assertEquals("A", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals(StrokeCuratorUserAction.ForceSetTokenName, curator.getLastUserAction());

        // Undo force set token name
        curator.undoUserAction();

        assertTrue(curator.canUndoUserAction());
        assertTrue(curator.canRedoUserAction());

        assertEquals(1, curator.getNumTokens());
        assertEquals(2, curator.getNumStrokes());
        assertEquals(1, curator.getWrittenTokenRecogWinners().size());
        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals(StrokeCuratorUserAction.AddStroke, curator.getLastUserAction());

        // Clear tokens
        curator.clear();

        assertTrue(curator.canUndoUserAction());
        assertFalse(curator.canRedoUserAction());

        assertEquals(0, curator.getNumTokens());
        assertEquals(0, curator.getNumStrokes());
        assertEquals(StrokeCuratorUserAction.ClearStrokes, curator.getLastUserAction());

        // Undo clear strokes
        curator.undoUserAction();

        assertTrue(curator.canUndoUserAction());
        assertTrue(curator.canRedoUserAction());

        assertEquals(1, curator.getNumTokens());
        assertEquals(2, curator.getNumStrokes());
        assertEquals(1, curator.getWrittenTokenRecogWinners().size());
        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals(StrokeCuratorUserAction.AddStroke, curator.getLastUserAction());

        // Add the third and fourth strokes
        curator.addStroke(stroke2);
        curator.addStroke(stroke3);

        assertTrue(curator.canUndoUserAction());
        assertFalse(curator.canRedoUserAction());

        assertEquals(3, curator.getNumTokens());
        assertEquals(4, curator.getNumStrokes());
        assertEquals(3, curator.getWrittenTokenRecogWinners().size());
        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals("-", curator.getWrittenTokenRecogWinners().get(1));
        assertEquals("1", curator.getWrittenTokenRecogWinners().get(2));

        // Merge the third and fourth strokes into "T"
        curator.mergeStrokesAsToken(new int[] {2, 3});

        assertTrue(curator.canUndoUserAction());
        assertFalse(curator.canRedoUserAction());

        assertEquals(2, curator.getNumTokens());
        assertEquals(4, curator.getNumStrokes());
        assertEquals(2, curator.getWrittenTokenRecogWinners().size());
        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals("T", curator.getWrittenTokenRecogWinners().get(1));
        assertEquals(StrokeCuratorUserAction.MergeStrokesAsToken, curator.getLastUserAction());

        // Remove a token
        curator.removeToken(1);

        assertTrue(curator.canUndoUserAction());
        assertFalse(curator.canRedoUserAction());

        assertEquals(1, curator.getNumTokens());
        assertEquals(2, curator.getNumStrokes());
        assertEquals(1, curator.getWrittenTokenRecogWinners().size());
        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals(StrokeCuratorUserAction.RemoveToken, curator.getLastUserAction());

        // Undo remove token
        curator.undoUserAction();

        assertTrue(curator.canUndoUserAction());
        assertTrue(curator.canRedoUserAction());

        assertEquals(2, curator.getNumTokens());
        assertEquals(4, curator.getNumStrokes());
        assertEquals(2, curator.getWrittenTokenRecogWinners().size());
        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals("T", curator.getWrittenTokenRecogWinners().get(1));
        assertEquals(StrokeCuratorUserAction.MergeStrokesAsToken, curator.getLastUserAction());

        // Redo remove token
        curator.redoUserAction();

        assertTrue(curator.canUndoUserAction());
        assertFalse(curator.canRedoUserAction());

        assertEquals(1, curator.getNumTokens());
        assertEquals(2, curator.getNumStrokes());
        assertEquals(1, curator.getWrittenTokenRecogWinners().size());
        assertEquals("=", curator.getWrittenTokenRecogWinners().get(0));
        assertEquals(StrokeCuratorUserAction.RemoveToken, curator.getLastUserAction());
        assertEquals(1, curator.getTokenUuids().size());

        // Redo again and it should throw an exception
        caughtException = null;

        try {
            curator.redoUserAction();
        } catch (IllegalStateException e) {
            caughtException = e;
        }

        assertNotNull(caughtException);

    }

}
