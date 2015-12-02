package me.scai.handwriting;

import me.scai.parsetree.*;
import me.scai.parsetree.evaluation.ParseTreeEvaluator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class Test_NodeToken {

    private static TokenSetParser tokenSetParser;
    private static ParseTreeStringizer stringizer;
    private static ParseTreeEvaluator evaluator;

    private static GraphicalProductionSet gpSet;
    private static TerminalSet termSet;


    @BeforeClass
    public static void beforeClass() {
        TestHelper.WorkerTuple workerTuple = TestHelper.getTestWorkerTuple();

        gpSet          = workerTuple.gpSet;
        termSet        = workerTuple.termSet;

        tokenSetParser = workerTuple.tokenSetParser;
        stringizer     = workerTuple.stringizer;
        evaluator      = workerTuple.evaluator;
    }

    @Test
    public void test_oneNodeTokenAddedToSQROOT() throws TokenSetParserException {

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

        Node node = null;
        try {
            node = tokenSetParser.parse(wtSet);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(node);

        NodeToken nodeToken = new NodeToken(node, wtSet);

        List<Integer> gpIndices = nodeToken.getMatchingGraphicalProductionIndices(gpSet);

        assertNotNull(gpIndices);
        assertFalse(gpIndices.isEmpty());

        List<String> gpSumStrings = TestHelper.getGraphicalProductionSumStrings(gpSet, gpIndices);

        // WARNING: Grammar dependency!!
        assertTrue(gpSumStrings.contains("ROOT->EXPR_LV4"));
        assertTrue(gpSumStrings.contains("EXPR_LV4->EXPR_LV3"));
        assertTrue(gpSumStrings.contains("EXPR_LV3->ADDITION"));
        assertTrue(gpSumStrings.contains("ADDITION->PLUS_OP EXPR_LV4 EXPR_LV4"));

        // Construct a (composite) token set with the nodeToken

        AbstractToken[] writtenTokensComposite = new AbstractToken[2];

        writtenTokensComposite[0] = new CWrittenToken();
        writtenTokensComposite[0].setBounds(new float[] {-0.1f, -0.2f, 5f, 1.2f});
        writtenTokensComposite[0].setRecogResult("root");

        writtenTokensComposite[1] = nodeToken;

        CWrittenTokenSetNoStroke wtSetComposite = CWrittenTokenSetNoStroke.from(writtenTokensComposite);
        assertTrue(wtSetComposite.hasNodeToken());
        assertEquals(2, wtSetComposite.getNumTokens());


        Node node2 = null;
        String stringized2 = null;
        try {
            node2 = tokenSetParser.parse(wtSetComposite);
            stringized2 = stringizer.stringize(node2);
        } catch (Exception e) {
            fail("Failed due to Exception: " + e.getMessage());
        }

        assertNotNull(node2);
        assertEquals("(sqrt((1 + 2)))", stringized2);
    }

    @Test
    public void test_oneNodeTokenAddedToFRACTION() throws TokenSetParserException {

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

        Node node = null;
        try {
            node = tokenSetParser.parse(wtSet);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(node);

        NodeToken nodeToken = new NodeToken(node, wtSet);

        List<Integer> gpIndices = nodeToken.getMatchingGraphicalProductionIndices(gpSet);

        assertNotNull(gpIndices);
        assertFalse(gpIndices.isEmpty());

        List<String> gpSumStrings = TestHelper.getGraphicalProductionSumStrings(gpSet, gpIndices);

        // WARNING: Grammar dependency!!
        assertTrue(gpSumStrings.contains("ROOT->EXPR_LV4"));
        assertTrue(gpSumStrings.contains("EXPR_LV4->EXPR_LV3"));
        assertTrue(gpSumStrings.contains("EXPR_LV3->ADDITION"));
        assertTrue(gpSumStrings.contains("ADDITION->PLUS_OP EXPR_LV4 EXPR_LV4"));

        // Construct a (composite) token set with the nodeToken

        AbstractToken[] writtenTokensComposite = new AbstractToken[3];

        writtenTokensComposite[0] = new CWrittenToken();
        writtenTokensComposite[0].setBounds(new float[] {-0.2f, 1.2f, 5.2f, 1.2f});
        writtenTokensComposite[0].setRecogResult("-");

        writtenTokensComposite[1] = new CWrittenToken();
        writtenTokensComposite[1].setBounds(new float[] {2f, 1.4f, 3f, 2.4f});
        writtenTokensComposite[1].setRecogResult("3");

        writtenTokensComposite[2] = nodeToken;

        CWrittenTokenSetNoStroke wtSetComposite = CWrittenTokenSetNoStroke.from(writtenTokensComposite);
        assertTrue(wtSetComposite.hasNodeToken());
        assertEquals(3, wtSetComposite.getNumTokens());

        Node node2 = null;
        String stringized2 = null;
        try {
            node2 = tokenSetParser.parse(wtSetComposite);
            stringized2 = stringizer.stringize(node2);
        } catch (Exception e) {
            fail("Failed due to Exception: " + e.getMessage());
        }

        assertNotNull(node2);
        assertEquals("((1 + 2) / 3)", stringized2);
    }

    @Test
    public void test_twoNodeTokensAddedToADDITION() throws TokenSetParserException {

        // 1st number in the addition
        CWrittenTokenSetNoStroke wtSetNum0 = TestHelper.getMockTokenSet(
                new float[][] {
                        {0f, 0f, 1f, 1f},
                        {1f, 0f, 2f, 1f}
                },
                new String[] {"1", "2"}
        );

        assertFalse(wtSetNum0.hasNodeToken());
        assertEquals(2, wtSetNum0.getNumTokens());

        Node nodeNum0 = null;
        try {
            nodeNum0 = tokenSetParser.parse(wtSetNum0);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeNum0);
        assertEquals("12", stringizer.stringize(nodeNum0));

        NodeToken nodeTokenNum0 = new NodeToken(nodeNum0, wtSetNum0);

        // 2nd number in the addition
        CWrittenTokenSetNoStroke wtSetNum1 = TestHelper.getMockTokenSet(
                new float[][] {
                        {3f, 0f, 4f, 1f},
                        {4f, 0f, 5f, 1f}
                },
                new String[] {"3", "4"}
        );

        assertFalse(wtSetNum1.hasNodeToken());
        assertEquals(2, wtSetNum1.getNumTokens());

        Node nodeNum1 = null;
        try {
            nodeNum1 = tokenSetParser.parse(wtSetNum1);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeNum1);
        assertEquals("34", stringizer.stringize(nodeNum1));

        NodeToken nodeTokenNum1 = new NodeToken(nodeNum1, wtSetNum1);

        // Combined denominator
        AbstractToken[] writtenTokensDenom = new AbstractToken[3];

        writtenTokensDenom[1] = new CWrittenToken();
        writtenTokensDenom[1].setBounds(new float[]{2f, 0f, 3f, 1f});
        writtenTokensDenom[1].setRecogResult("+");

        writtenTokensDenom[0] = nodeTokenNum0;
        writtenTokensDenom[2] = nodeTokenNum1;

        CWrittenTokenSetNoStroke wtSetDenom= CWrittenTokenSetNoStroke.from(writtenTokensDenom);
        assertTrue(wtSetDenom.hasNodeToken());
        assertEquals(3, wtSetDenom.getNumTokens());

        // Parse the composite token
        Node nodeDenom = null;
        String stringizedDenom = null;
        try {
            nodeDenom = tokenSetParser.parse(wtSetDenom);
            stringizedDenom = stringizer.stringize(nodeDenom);
        } catch (Exception e) {
            fail("Failed due to Exception: " + e.getMessage());
        }

        assertNotNull(nodeDenom);
        assertEquals("(12 + 34)", stringizedDenom);
    }

    @Test
    public void test_twoNodeTokensAddedToFRACTION() throws TokenSetParserException {

        // Construct numerator node: 1 + 2
        CWrittenTokenSetNoStroke wtSetNumer = TestHelper.getMockTokenSet(
                new float[][]{
                        {0f, 0f, 1f, 1f},
                        {2f, 0f, 3f, 1f},
                        {4f, 0f, 5f, 1f}
                },
                new String[]{"1", "+", "2"}
        );

        assertFalse(wtSetNumer.hasNodeToken());
        assertEquals(3, wtSetNumer.getNumTokens());

        Node nodeNumer = null;
        try {
            nodeNumer = tokenSetParser.parse(wtSetNumer);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeNumer);

        NodeToken nodeTokenNumer = new NodeToken(nodeNumer, wtSetNumer);

        List<Integer> gpIndices = nodeTokenNumer.getMatchingGraphicalProductionIndices(gpSet);
        List<String> gpSumStrings = TestHelper.getGraphicalProductionSumStrings(gpSet, gpIndices);

        // WARNING: Grammar dependency!!
        assertTrue(gpSumStrings.contains("ROOT->EXPR_LV4"));
        assertTrue(gpSumStrings.contains("EXPR_LV4->EXPR_LV3"));
        assertTrue(gpSumStrings.contains("EXPR_LV3->ADDITION"));
        assertTrue(gpSumStrings.contains("ADDITION->PLUS_OP EXPR_LV4 EXPR_LV4"));


        // Construct denominator node: 3 + 4
        CWrittenTokenSetNoStroke wtSetDenom = TestHelper.getMockTokenSet(
                new float[][]{
                        {0f, 2f, 1f, 3f},
                        {2f, 2f, 3f, 3f},
                        {4f, 2f, 5f, 3f}
                },
                new String[]{"3", "+", "4"}
        );

        assertFalse(wtSetDenom.hasNodeToken());
        assertEquals(3, wtSetDenom.getNumTokens());

        Node nodeDenom = null;
        try {
            nodeDenom = tokenSetParser.parse(wtSetDenom);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeDenom);

        NodeToken nodeTokenDenom = new NodeToken(nodeDenom, wtSetDenom);

        gpIndices = nodeTokenDenom.getMatchingGraphicalProductionIndices(gpSet);
        gpSumStrings = TestHelper.getGraphicalProductionSumStrings(gpSet, gpIndices);

        // WARNING: Grammar dependency!!
        assertTrue(gpSumStrings.contains("ROOT->EXPR_LV4"));
        assertTrue(gpSumStrings.contains("EXPR_LV4->EXPR_LV3"));
        assertTrue(gpSumStrings.contains("EXPR_LV3->ADDITION"));
        assertTrue(gpSumStrings.contains("ADDITION->PLUS_OP EXPR_LV4 EXPR_LV4"));

        // Construct the composite token
        AbstractToken[] writtenTokensComposite = new AbstractToken[3];

        // The fraction line: "-"
        writtenTokensComposite[0] = new CWrittenToken();
        writtenTokensComposite[0].setBounds(new float[] {-0.2f, 1.5f, 5.2f, 1.5f});
        writtenTokensComposite[0].setRecogResult("-");

        writtenTokensComposite[1] = nodeTokenNumer;     // Numerator node
        writtenTokensComposite[2] = nodeTokenDenom;     // Denominator node

        CWrittenTokenSetNoStroke wtSetComposite = CWrittenTokenSetNoStroke.from(writtenTokensComposite);
        assertTrue(wtSetComposite.hasNodeToken());
        assertEquals(3, wtSetComposite.getNumTokens());

        // Parse the composite token
        Node node2 = null;
        String stringized2 = null;
        try {
            node2 = tokenSetParser.parse(wtSetComposite);
            stringized2 = stringizer.stringize(node2);
        } catch (Exception e) {
            fail("Failed due to Exception: " + e.getMessage());
        }

        assertNotNull(node2);
        assertEquals("((1 + 2) / (3 + 4))", stringized2);

    }

    @Test
    public void test_twoNodeTokensAddedToVariableDefinition() throws TokenSetParserException {

        // Construct the node with the symbol name in it
        CWrittenTokenSetNoStroke wtSetSymbol = TestHelper.getMockTokenSet(
                new float[][] {
                        {0f, 0f, 1f, 1f}
                },
                new String[] {"A"}
        );

        assertFalse(wtSetSymbol.hasNodeToken());
        assertEquals(1, wtSetSymbol.getNumTokens());

        Node nodeSymbol = null;
        String stringizedSymbol = null;
        try {
            nodeSymbol = tokenSetParser.parse(wtSetSymbol);
            stringizedSymbol = stringizer.stringize(nodeSymbol);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeSymbol);
        assertEquals("A", stringizedSymbol);

        NodeToken nodeTokenSymbol = new NodeToken(nodeSymbol, wtSetSymbol);

        List<Integer> gpIndices = nodeTokenSymbol.getMatchingGraphicalProductionIndices(gpSet);

        assertNotNull(gpIndices);
        assertFalse(gpIndices.isEmpty());

        // Construct the node with the value in it
        CWrittenTokenSetNoStroke wtSetValue = TestHelper.getMockTokenSet(
                new float[][] {
                        {4f, 0f, 5f, 1f},
                        {5.2f, 0f, 6.2f, 1f}
                },
                new String[] {"3", "4"}
        );

        assertFalse(wtSetValue.hasNodeToken());
        assertEquals(2, wtSetValue.getNumTokens());

        Node nodeValue = null;
        String stringizedValue = null;
        try {
            nodeValue = tokenSetParser.parse(wtSetValue);
            stringizedValue = stringizer.stringize(nodeValue);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeValue);
        assertEquals("34", stringizedValue);

        NodeToken nodeTokenValue = new NodeToken(nodeValue, wtSetValue);

        gpIndices = nodeTokenValue.getMatchingGraphicalProductionIndices(gpSet);

        assertNotNull(gpIndices);
        assertFalse(gpIndices.isEmpty());

        // Construct the composite token set
        AbstractToken[] writtenTokensComposite = new AbstractToken[3];

        writtenTokensComposite[0] = new CWrittenToken();
        writtenTokensComposite[0].setBounds(new float[] {2f, 0f, 3f, 1f});
        writtenTokensComposite[0].setRecogResult("=");

        writtenTokensComposite[1] = nodeTokenSymbol;
        writtenTokensComposite[2] = nodeTokenValue;

        CWrittenTokenSetNoStroke wtSetComposite = CWrittenTokenSetNoStroke.from(writtenTokensComposite);
        assertTrue(wtSetComposite.hasNodeToken());
        assertEquals(3, wtSetComposite.getNumTokens());

        Node node2 = null;
        String stringized2 = null;
        try {
            node2 = tokenSetParser.parse(wtSetComposite);
            stringized2 = stringizer.stringize(node2);
        } catch (Exception e) {
            fail("Failed due to Exception: " + e.getMessage());
        }

        assertNotNull(node2);
        assertEquals("(A = 34)", stringized2);

        // TODO: VARIABLE_EVALUATED needs to be replaced with SYMBOL
//        try {
//            evaluator.eval(node2);
//        } catch (ParseTreeEvaluatorException e) {
//            fail("Failed due to ParseTreeEvaluatorException: " + e.getMessage());
//        }
//        ValueUnion vu = evaluator.getVarMap().getVarValue("A");

    }

    @Test
    public void test_threeNodeTokensAddedToSigmaTermSimple() throws TokenSetParserException {

        // Construct summed node: x ^ 5
        CWrittenTokenSetNoStroke wtSetSummed = TestHelper.getMockTokenSet(
                new float[][]{
                        {3f, 1f, 4f, 2f}
                },
                new String[]{"x"}
        );

        assertFalse(wtSetSummed.hasNodeToken());
        assertEquals(1, wtSetSummed.getNumTokens());

        Node nodeSummed = null;
        String stringizedSummed = null;
        try {
            nodeSummed = tokenSetParser.parse(wtSetSummed);
            stringizedSummed = stringizer.stringize(nodeSummed);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeSummed);
        assertEquals("x", stringizedSummed);

        NodeToken nodeTokenSummed = new NodeToken(nodeSummed, wtSetSummed);

        // Construct lower-bound node: x = 1 + 2
        CWrittenTokenSetNoStroke wtSetLB = TestHelper.getMockTokenSet(
                new float[][]{
                        {1f,   2.5f, 1.1f, 2.7f},
                        {1.2f, 2.5f, 1.3f, 2.7f},
                        {1.4f, 2.5f, 1.5f, 2.7f}
                },
                new String[]{"x", "=", "1"}
        );

        assertFalse(wtSetLB.hasNodeToken());
        assertEquals(3, wtSetLB.getNumTokens());

        Node nodeLB = null;
        String stringizedLB = null;
        try {
            nodeLB = tokenSetParser.parse(wtSetLB);
            stringizedLB = stringizer.stringize(nodeLB);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeLB);
        assertEquals("(x = 1)", stringizedLB);

        NodeToken nodeTokenLB = new NodeToken(nodeLB, wtSetLB);

        // Construct upper-bound node: x = 3 + 4
        CWrittenTokenSetNoStroke wtSetUB = TestHelper.getMockTokenSet(
                new float[][]{
                        {1.4f, 0.5f, 1.5f, 0.7f}
                },
                new String[]{"3"}
        );

        assertFalse(wtSetUB.hasNodeToken());
        assertEquals(1, wtSetUB.getNumTokens());

        Node nodeUB = null;
        String stringizedUB = null;
        try {
            nodeUB = tokenSetParser.parse(wtSetUB);
            stringizedUB = stringizer.stringize(nodeUB);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeUB);
        assertEquals("3", stringizedUB);

        NodeToken nodeTokenUB = new NodeToken(nodeUB, wtSetUB);

        // Construct the composite token
        AbstractToken[] writtenTokensComposite = new AbstractToken[4];

        // The fraction line: "-"
        writtenTokensComposite[0] = new CWrittenToken();
        writtenTokensComposite[0].setBounds(new float[] {1f, 1f, 2f, 2f});
        writtenTokensComposite[0].setRecogResult("gr_Si");

        writtenTokensComposite[1] = nodeTokenLB;     // LB node
        writtenTokensComposite[2] = nodeTokenUB;     // UB node
        writtenTokensComposite[3] = nodeTokenSummed; // Summed node

        CWrittenTokenSetNoStroke wtSetComposite = CWrittenTokenSetNoStroke.from(writtenTokensComposite);
        assertTrue(wtSetComposite.hasNodeToken());
        assertEquals(4, wtSetComposite.getNumTokens());

        // Parse the composite token
        Node node2 = null;
        String stringized2 = null;
        try {
            node2 = tokenSetParser.parse(wtSetComposite);
            stringized2 = stringizer.stringize(node2);
        } catch (Exception e) {
            fail("Failed due to Exception: " + e.getMessage());
        }

        assertNotNull(node2);
        assertEquals("Sum((x = 1) : (3))(x)", stringized2);


    }

    @Test
    public void test_threeNodeTokensAddedToSigmaTermComplex() throws TokenSetParserException {

        // Construct summed node: x ^ 5
        CWrittenTokenSetNoStroke wtSetSummed = TestHelper.getMockTokenSet(
                new float[][]{
                        {3f, 1f, 4f, 2f},
                        {4.5f, 1f, 5f, 1.2f}
                },
                new String[]{"x", "5"}
        );

        assertFalse(wtSetSummed.hasNodeToken());
        assertEquals(2, wtSetSummed.getNumTokens());

        Node nodeSummed = null;
        String stringizedSummed = null;
        try {
            nodeSummed = tokenSetParser.parse(wtSetSummed);
            stringizedSummed = stringizer.stringize(nodeSummed);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeSummed);
        assertEquals("(x ^ 5)", stringizedSummed);

        NodeToken nodeTokenSummed = new NodeToken(nodeSummed, wtSetSummed);

        // Construct lower-bound node: x = 1 + 2
        CWrittenTokenSetNoStroke wtSetLB = TestHelper.getMockTokenSet(
                new float[][]{
                        {1f,   2.5f, 1.1f, 2.7f},
                        {1.2f, 2.5f, 1.3f, 2.7f},
                        {1.4f, 2.5f, 1.5f, 2.7f},
                        {1.6f, 2.5f, 1.7f, 2.7f},
                        {1.8f, 2.5f, 1.9f, 2.7f},
                },
                new String[]{"x", "=", "1", "+", "2"}
        );

        assertFalse(wtSetLB.hasNodeToken());
        assertEquals(5, wtSetLB.getNumTokens());

        Node nodeLB = null;
        String stringizedLB = null;
        try {
            nodeLB = tokenSetParser.parse(wtSetLB);
            stringizedLB = stringizer.stringize(nodeLB);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeLB);
        assertEquals("(x = (1 + 2))", stringizedLB);

        NodeToken nodeTokenLB = new NodeToken(nodeLB, wtSetLB);

        // Construct upper-bound node: x = 3 + 4
        CWrittenTokenSetNoStroke wtSetUB = TestHelper.getMockTokenSet(
                new float[][]{
                        {1.2f, 0.5f, 1.3f, 0.7f},
                        {1.4f, 0.5f, 1.5f, 0.7f},
                        {1.6f, 0.5f, 1.7f, 0.7f}
                },
                new String[]{"3", "+", "4"}
        );

        assertFalse(wtSetUB.hasNodeToken());
        assertEquals(3, wtSetUB.getNumTokens());

        Node nodeUB = null;
        String stringizedUB = null;
        try {
            nodeUB = tokenSetParser.parse(wtSetUB);
            stringizedUB = stringizer.stringize(nodeUB);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeUB);
        assertEquals("(3 + 4)", stringizedUB);

        NodeToken nodeTokenUB = new NodeToken(nodeUB, wtSetUB);

        // Construct the composite token
        AbstractToken[] writtenTokensComposite = new AbstractToken[4];

        // The fraction line: "-"
        writtenTokensComposite[0] = new CWrittenToken();
        writtenTokensComposite[0].setBounds(new float[] {1f, 1f, 2f, 2f});
        writtenTokensComposite[0].setRecogResult("gr_Si");

        writtenTokensComposite[1] = nodeTokenLB;     // LB node
        writtenTokensComposite[2] = nodeTokenUB;     // UB node
        writtenTokensComposite[3] = nodeTokenSummed; // Summed node

        CWrittenTokenSetNoStroke wtSetComposite = CWrittenTokenSetNoStroke.from(writtenTokensComposite);
        assertTrue(wtSetComposite.hasNodeToken());
        assertEquals(4, wtSetComposite.getNumTokens());

        // Parse the composite token
        Node node2 = null;
        String stringized2 = null;
        try {
            node2 = tokenSetParser.parse(wtSetComposite);
            stringized2 = stringizer.stringize(node2);
        } catch (Exception e) {
            fail("Failed due to Exception: " + e.getMessage());
        }

        assertNotNull(node2);
        assertEquals("Sum((x = (1 + 2)) : ((3 + 4)))((x ^ 5))", stringized2);


    }

    @Test
    public void test_twoLevelNodeToken() throws TokenSetParserException {

        // 1st number in the denominator
        CWrittenTokenSetNoStroke wtSetDenom1 = TestHelper.getMockTokenSet(
                new float[][] {
                        {0f, 0f, 1f, 1f},
                        {1f, 0f, 2f, 1f}
                },
                new String[] {"1", "2"}
        );

        assertFalse(wtSetDenom1.hasNodeToken());
        assertEquals(2, wtSetDenom1.getNumTokens());

        Node nodeDenom1 = null;
        try {
            nodeDenom1 = tokenSetParser.parse(wtSetDenom1);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeDenom1);
        assertEquals("12", stringizer.stringize(nodeDenom1));

        NodeToken nodeTokenDenom1 = new NodeToken(nodeDenom1, wtSetDenom1);

        // 2nd number in the denominator
        CWrittenTokenSetNoStroke wtSetDenom2 = TestHelper.getMockTokenSet(
                new float[][] {
                        {3f, 0f, 4f, 1f},
                        {4f, 0f, 5f, 1f}
                },
                new String[] {"3", "4"}
        );

        assertFalse(wtSetDenom2.hasNodeToken());
        assertEquals(2, wtSetDenom2.getNumTokens());

        Node nodeDenom2 = null;
        try {
            nodeDenom2 = tokenSetParser.parse(wtSetDenom2);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeDenom2);
        assertEquals("34", stringizer.stringize(nodeDenom2));

        NodeToken nodeTokenDenom2 = new NodeToken(nodeDenom2, wtSetDenom2);

        // Combined denominator
        AbstractToken[] writtenTokensDenom = new AbstractToken[3];

        writtenTokensDenom[1] = new CWrittenToken();
        writtenTokensDenom[1].setBounds(new float[]{2f, 0f, 3f, 1f});
        writtenTokensDenom[1].setRecogResult("-");

        writtenTokensDenom[0] = nodeTokenDenom1;
        writtenTokensDenom[2] = nodeTokenDenom2;

        CWrittenTokenSetNoStroke wtSetDenom= CWrittenTokenSetNoStroke.from(writtenTokensDenom);
        assertTrue(wtSetDenom.hasNodeToken());
        assertEquals(3, wtSetDenom.getNumTokens());

        // Parse the composite token
        Node nodeDenom = null;
        String stringizedDenom = null;
        try {
            nodeDenom = tokenSetParser.parse(wtSetDenom);
            stringizedDenom = stringizer.stringize(nodeDenom);
        } catch (Exception e) {
            fail("Failed due to Exception: " + e.getMessage());
        }

        assertNotNull(nodeDenom);
        assertEquals("(12 - 34)", stringizedDenom);

        // 1st number in the numerator
        CWrittenTokenSetNoStroke wtSetNumer1 = TestHelper.getMockTokenSet(
                new float[][] {
                        {0f, -2f, 1f, -1f},
                        {1f, -2f, 2f, -1f}
                },
                new String[] {"5", "6"}
        );

        assertFalse(wtSetNumer1.hasNodeToken());
        assertEquals(2, wtSetNumer1.getNumTokens());

        Node nodeNumer1 = null;
        try {
            nodeNumer1 = tokenSetParser.parse(wtSetNumer1);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeNumer1);
        assertEquals("56", stringizer.stringize(nodeNumer1));

        NodeToken nodeTokenNumer1 = new NodeToken(nodeNumer1, wtSetNumer1);

        // 2nd number in the denominator
        CWrittenTokenSetNoStroke wtSetNumer2 = TestHelper.getMockTokenSet(
                new float[][] {
                        {3f, -2f, 4f, -1f},
                        {4f, -2f, 5f, -1f}
                },
                new String[] {"7", "8"}
        );

        assertFalse(wtSetNumer2.hasNodeToken());
        assertEquals(2, wtSetNumer2.getNumTokens());

        Node nodeNumer2 = null;
        try {
            nodeNumer2 = tokenSetParser.parse(wtSetNumer2);
        } catch (TokenSetParserException e) {
            fail("Failed due to TokenSetParserException: " + e.getMessage());
        }

        assertNotNull(nodeNumer2);
        assertEquals("78", stringizer.stringize(nodeNumer2));

        NodeToken nodeTokenNumer2 = new NodeToken(nodeNumer2, wtSetNumer2);

        // Combined denominator
        AbstractToken[] writtenTokensNumer = new AbstractToken[3];

        writtenTokensNumer[1] = new CWrittenToken();
        writtenTokensNumer[1].setBounds(new float[]{2f, -2f, 3f, -1f});
        writtenTokensNumer[1].setRecogResult("+");

        writtenTokensNumer[0] = nodeTokenNumer1;
        writtenTokensNumer[2] = nodeTokenNumer2;

        CWrittenTokenSetNoStroke wtSetNumer = CWrittenTokenSetNoStroke.from(writtenTokensNumer);
        assertTrue(wtSetNumer.hasNodeToken());
        assertEquals(3, wtSetNumer.getNumTokens());

        // Parse the composite token
        Node nodeNumer = null;
        String stringizedNumer = null;
        try {
            nodeNumer = tokenSetParser.parse(wtSetNumer);
            stringizedNumer = stringizer.stringize(nodeNumer);
        } catch (Exception e) {
            fail("Failed due to Exception: " + e.getMessage());
        }

        assertNotNull(nodeNumer);
        assertEquals("(56 + 78)", stringizedNumer);

        // Level-2 composite token
        NodeToken nodeTokenDenom = new NodeToken(nodeDenom, wtSetDenom);
        NodeToken nodeTokenNumer = new NodeToken(nodeNumer, wtSetNumer);

        AbstractToken[] writtenTokensL2 = new AbstractToken[3];

        writtenTokensL2[1] = new CWrittenToken();
        writtenTokensL2[1].setBounds(new float[]{-0.2f, -0.5f, 5.2f, -0.4f});
        writtenTokensL2[1].setRecogResult("-");

        writtenTokensL2[0] = nodeTokenDenom;
        writtenTokensL2[2] = nodeTokenNumer;

        CWrittenTokenSetNoStroke wtSetL2 = CWrittenTokenSetNoStroke.from(writtenTokensL2);
        assertTrue(wtSetL2.hasNodeToken());
        assertEquals(3, wtSetL2.getNumTokens());

        // Parse the composite token
        Node nodeL2 = null;
        String stringizerL2 = null;
        try {
            nodeL2 = tokenSetParser.parse(wtSetL2);
            stringizerL2 = stringizer.stringize(nodeL2);
        } catch (Exception e) {
            fail("Failed due to Exception: " + e.getMessage());
        }

        assertNotNull(nodeL2);
        assertEquals("((56 + 78) / (12 - 34))", stringizerL2);

    }


}
