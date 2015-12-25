package me.scai.handwriting;

import me.scai.parsetree.TokenSet2NodeTokenParser;
import me.scai.parsetree.*;
import me.scai.plato.helpers.CWrittenTokenSetJsonHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class Test_NodeToken {
    // Member variables
    private static TokenSetParser tokenSetParser;
    private static ParseTreeStringizer stringizer;

    private static GraphicalProductionSet gpSet;
    private static TokenSet2NodeTokenParser tokenSet2NodeTokenParser;

    // Methods
    @BeforeClass
    public static void beforeClass() {
        TestHelper.WorkerTuple workerTuple = TestHelper.getTestWorkerTuple();

        gpSet          = workerTuple.gpSet;

        tokenSetParser = workerTuple.tokenSetParser;
        stringizer     = workerTuple.stringizer;

        tokenSet2NodeTokenParser = new TokenSet2NodeTokenParser(tokenSetParser, stringizer);
    }

    private NodeToken parseTokenSet2NodeToken(CWrittenTokenSetNoStroke wtSet) {
        NodeToken nodeToken = null;
        try {
            nodeToken = tokenSet2NodeTokenParser.parse2NodeToken(wtSet);
        } catch (TokenSetParserException e) {
            fail("Parsing failed due to TokenSetParserException: " + e.getMessage());
        } catch (InterruptedException e) {
            fail("Parsing failed due to InterruptedException: " + e.getMessage());
        }


        return nodeToken;
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

        NodeToken nodeToken = parseTokenSet2NodeToken(wtSet);
        assertEquals("(1 + 2)", nodeToken.getRecogResult());

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

        // Test serialize the composite token set
        TokenSetJsonTestHelper.verifyTokenSetJson(CWrittenTokenSetJsonHelper.CWrittenTokenSet2JsonObj(wtSetComposite));

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

        NodeToken nodeToken = parseTokenSet2NodeToken(wtSet);
        assertEquals("(1 + 2)", nodeToken.getRecogResult());

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

        TokenSetJsonTestHelper.verifyTokenSetJson(CWrittenTokenSetJsonHelper.CWrittenTokenSet2JsonObj(wtSetComposite));

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

        NodeToken nodeTokenNum0 = parseTokenSet2NodeToken(wtSetNum0);
        assertEquals("12", nodeTokenNum0.getRecogResult());

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

        NodeToken nodeTokenNum1 = parseTokenSet2NodeToken(wtSetNum1);
        assertEquals("34", nodeTokenNum1.getRecogResult());

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

        NodeToken nodeTokenNumer = parseTokenSet2NodeToken(wtSetNumer);

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

        NodeToken nodeTokenDenom = parseTokenSet2NodeToken(wtSetDenom);

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

        TokenSetJsonTestHelper.verifyTokenSetJson(CWrittenTokenSetJsonHelper.CWrittenTokenSet2JsonObj(wtSetComposite));

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

        NodeToken nodeTokenSymbol = parseTokenSet2NodeToken(wtSetSymbol);
        assertEquals("A", nodeTokenSymbol.getRecogResult());

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


        NodeToken nodeTokenValue =  parseTokenSet2NodeToken(wtSetValue);
        assertEquals("34", nodeTokenValue.getRecogResult());

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

        TokenSetJsonTestHelper.verifyTokenSetJson(CWrittenTokenSetJsonHelper.CWrittenTokenSet2JsonObj(wtSetComposite));

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

        NodeToken nodeTokenSummed = parseTokenSet2NodeToken(wtSetSummed);
        assertEquals("x", nodeTokenSummed.getRecogResult());

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

        NodeToken nodeTokenLB = parseTokenSet2NodeToken(wtSetLB);
        assertEquals("(x = 1)", nodeTokenLB.getRecogResult());

        // Construct upper-bound node: x = 3 + 4
        CWrittenTokenSetNoStroke wtSetUB = TestHelper.getMockTokenSet(
                new float[][]{
                        {1.4f, 0.5f, 1.5f, 0.7f}
                },
                new String[]{"3"}
        );

        assertFalse(wtSetUB.hasNodeToken());
        assertEquals(1, wtSetUB.getNumTokens());

        NodeToken nodeTokenUB = parseTokenSet2NodeToken(wtSetUB);
        assertEquals("3", nodeTokenUB.getRecogResult());

        // Construct the composite token
        AbstractToken[] writtenTokensComposite = new AbstractToken[4];

        // The fraction line: "-"
        writtenTokensComposite[0] = new CWrittenToken();
        writtenTokensComposite[0].setBounds(new float[]{1f, 1f, 2f, 2f});
        writtenTokensComposite[0].setRecogResult("gr_Si");

        writtenTokensComposite[1] = nodeTokenLB;     // LB node
        writtenTokensComposite[2] = nodeTokenUB;     // UB node
        writtenTokensComposite[3] = nodeTokenSummed; // Summed node

        CWrittenTokenSetNoStroke wtSetComposite = CWrittenTokenSetNoStroke.from(writtenTokensComposite);
        assertTrue(wtSetComposite.hasNodeToken());
        assertEquals(4, wtSetComposite.getNumTokens());

        TokenSetJsonTestHelper.verifyTokenSetJson(CWrittenTokenSetJsonHelper.CWrittenTokenSet2JsonObj(wtSetComposite));

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

        NodeToken nodeTokenSummed = parseTokenSet2NodeToken(wtSetSummed);
        assertEquals("(x ^ 5)", nodeTokenSummed.getRecogResult());

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

        NodeToken nodeTokenLB = parseTokenSet2NodeToken(wtSetLB);
        assertEquals("(x = (1 + 2))", nodeTokenLB.getRecogResult());

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

        NodeToken nodeTokenUB = parseTokenSet2NodeToken(wtSetUB);
        assertEquals("(3 + 4)", nodeTokenUB.getRecogResult());

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

        TokenSetJsonTestHelper.verifyTokenSetJson(CWrittenTokenSetJsonHelper.CWrittenTokenSet2JsonObj(wtSetComposite));

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

        NodeToken nodeTokenDenom1 = parseTokenSet2NodeToken(wtSetDenom1);
        assertEquals("12", nodeTokenDenom1.getRecogResult());

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

        NodeToken nodeTokenDenom2 = parseTokenSet2NodeToken(wtSetDenom2);
        assertEquals("34", nodeTokenDenom2.getRecogResult());

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

        NodeToken nodeTokenNumer1 = parseTokenSet2NodeToken(wtSetNumer1);
        assertEquals("56", nodeTokenNumer1.getRecogResult());

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

        NodeToken nodeTokenNumer2 = parseTokenSet2NodeToken(wtSetNumer2);
        assertEquals("78", nodeTokenNumer2.getRecogResult());

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


    @Test
    public void test_parseSubsetOfTokenSet() throws TokenSetParserException {

        CWrittenTokenSetNoStroke wtSet = TestHelper.getMockTokenSet(
                new float[][]{
                        {0f, 0f, 1f, 1f},
                        {2f, 0f, 3f, 1f},
                        {4f, 0f, 5f, 1f},
                        {6f, 0f, 7f, 1f},
                        {8f, 0f, 9f, 1f}

                },
                new String[]{"1", "2", "+", "3", "4"}
        );
        assertEquals(5, wtSet.getNumTokens());

        // 1. Parse tokens {0, 1} as "12"
        CWrittenTokenSetNoStroke wtSet2 = null;
        try {
            wtSet2 = tokenSet2NodeTokenParser.parseAsNodeToken(wtSet, new int[]{0, 1});
        } catch (TokenSetParserException e) {
            fail("");
        } catch (InterruptedException e) {
            fail("");
        }

        assertNotNull(wtSet2);
        assertEquals(4, wtSet2.getNumTokens());
        assertEquals("12", wtSet2.tokens.get(0).getRecogResult());

        assertTrue(wtSet2.tokens.get(0) instanceof NodeToken);
        for (int i = 1; i < 4; ++i) {
            assertFalse(wtSet2.tokens.get(i) instanceof NodeToken);
        }

        // 2. Parse tokens {2, 3} (originally {3, 4}) as "34"
        CWrittenTokenSetNoStroke wtSet3 = null;
        try {
            wtSet3 = tokenSet2NodeTokenParser.parseAsNodeToken(wtSet2, new int[]{2, 3});
        } catch (TokenSetParserException e) {
            fail("");
        } catch (InterruptedException e) {
            fail("");
        }

        assertNotNull(wtSet3);
        assertEquals(3, wtSet3.getNumTokens());
        assertEquals("34", wtSet3.tokens.get(0).getRecogResult());
        assertEquals("12", wtSet3.tokens.get(1).getRecogResult());

        assertTrue(wtSet3.tokens.get(0) instanceof NodeToken);
        assertTrue(wtSet3.tokens.get(1) instanceof NodeToken);
        assertFalse(wtSet3.tokens.get(2) instanceof NodeToken);

        Node finalParsedNode = null;
        String finalStringized = null;
        try {
            finalParsedNode = tokenSetParser.parse(wtSet3);
            finalStringized = stringizer.stringize(finalParsedNode);
        } catch (TokenSetParserException e) {
            fail("");
        } catch (InterruptedException e) {
            fail("");
        }

        assertNotNull(finalParsedNode);
        assertEquals("(12 + 34)", finalStringized);
    }

}
