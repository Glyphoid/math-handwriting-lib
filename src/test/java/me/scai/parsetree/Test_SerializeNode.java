package me.scai.parsetree;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.scai.handwriting.CWrittenTokenSetNoStroke;
import me.scai.handwriting.NodeToken;
import me.scai.handwriting.TestHelper;
import me.scai.parsetree.evaluation.ParseTreeEvaluator;
import me.scai.plato.helpers.CWrittenTokenJsonHelper;
import me.scai.plato.helpers.CWrittenTokenSetJsonHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class Test_SerializeNode {
    private static final Gson gson = new Gson();

    private static TokenSetParser tokenSetParser;
    private static ParseTreeStringizer stringizer;
    private static ParseTreeEvaluator evaluator;

    private static GraphicalProductionSet gpSet;
    private static TerminalSet termSet;


    @BeforeClass
    public static void beforeClass() { // TODO: Refactor and de-duplicate with Test_NodeToken
        TestHelper.WorkerTuple workerTuple = TestHelper.getTestWorkerTuple();

        gpSet          = workerTuple.gpSet;
        termSet        = workerTuple.termSet;

        tokenSetParser = workerTuple.tokenSetParser;
        stringizer     = workerTuple.stringizer;
        evaluator      = workerTuple.evaluator;
    }

    private Node parseTokenSet(CWrittenTokenSetNoStroke wtSet) {
        Node node = null;
        try {
            node = tokenSetParser.parse(wtSet);
        } catch (TokenSetParserException e) {
            fail("Parsing failed due to TokenSetParserException: " + e.getMessage());
        } catch (InterruptedException e) {
            fail("Parsing failed due to InterruptedException: " + e.getMessage());
        }

        return node;
    }

    @Test
    public void test1() {
        Node node1 = new Node();

        JsonObject jsonNode1 = gson.toJsonTree(node1).getAsJsonObject();

        assertNotNull(jsonNode1);
    }

    @Test
    public void test2() {
        CWrittenTokenSetNoStroke wtSet = TestHelper.getMockTokenSet(
                new float[][] {
                        {0f, 0f, 1f, 1f},
                        {2f, 0f, 3f, 1f},
                        {4f, 0f, 5f, 1f}
                },
                new String[] {"1", "+", "2"}
        );

        assertFalse(wtSet.hasNodeToken());
        assertEquals(3, wtSet.getNumTokens());

        Node node = parseTokenSet(wtSet);
        JsonObject jsonNode = gson.toJsonTree(node).getAsJsonObject();

        NodeToken nodeToken = new NodeToken(node, wtSet);

        JsonObject jsonNodeToken = CWrittenTokenJsonHelper.CWrittenToken2JsonObject(nodeToken);
    }

}
