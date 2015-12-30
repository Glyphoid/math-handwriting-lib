package me.scai.plato.engine;

import com.google.gson.JsonObject;
import me.scai.handwriting.*;
import me.scai.parsetree.*;
import me.scai.parsetree.evaluation.ParseTreeEvaluator;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Test_HandwritingEngineImpl {
    /* Constants */
    private static final String TEST_ROOT_DIR = TestHelper.TEST_ROOT_DIR;
    private static final String RESOURCES_DIR = "resources";
    private static final String RESOURCES_CONFIG_DIR = "config";
    private static final String STROKE_CURATOR_CONFIG_FILE = "stroke_curator_config.json";

    /* Member variables */
    private TokenSetParser tokenSetParser;

    private StrokeCuratorConfigurable strokeCurator;
    private HandwritingEngine hwEng;

    /* Constructor */
    public Test_HandwritingEngineImpl() {
        // Create token engine
        TokenRecogEngine tokenRecogEngine = null;
        try {
            tokenRecogEngine = TestHelper.readTokenEngine();
        } catch (Exception exc) {
            fail("Failed to read token engine");
        }

        // Create stroke curator
        URL strokeCuratorConfigUrl = this.getClass().getClassLoader().getResource(File.separator + TEST_ROOT_DIR +
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
        TokenSetParserOutput subsetParseRes = hwEng.parseTokenSet(new int[] {0, 1});
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

