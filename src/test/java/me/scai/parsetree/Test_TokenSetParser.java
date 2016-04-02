package me.scai.parsetree;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import me.scai.handwriting.CWrittenTokenSetNoStroke;
import me.scai.handwriting.TestHelper;
import me.scai.parsetree.evaluation.ParseTreeEvaluator;
import me.scai.parsetree.evaluation.Undefined;
import me.scai.parsetree.evaluation.exceptions.ParseTreeEvaluatorException;

import org.junit.After;
import org.junit.Test;


public class Test_TokenSetParser {
	public static final String errStr = ParseTreeStringizer.STRINGIZATION_FAILED_STRING;
	public static final double evalResEqualityAbsTol = 1e-9;

	String[] singleOutIdx = {};
	Test_QADataSet qaDataSet = new Test_QADataSet();
	
	private String tokenSetSuffix = ".wts";
	private String tokenSetPathPrefix = null;

    private TokenSetParser tokenSetParser;
    private ParseTreeStringizer stringizer;
    private ParseTreeEvaluator evaluator;
    private ParseTreeMathTexifier mathTexifier;
	
	/* Constructor */
    public Test_TokenSetParser() {
        tokenSetPathPrefix = System.getProperty("tokenSetPathPrefix");
        if (tokenSetPathPrefix == null || tokenSetPathPrefix.isEmpty()) {
            fail("tokenSetPathPrefix is undefined (use e.g., tokenSetPathPrefix=C:\\Users\\scai\\Dropbox\\Plato\\data\\tokensets\\TS_");
        }

        TestHelper.WorkerTuple workerTuple = TestHelper.getTestWorkerTuple();

        tokenSetParser = workerTuple.tokenSetParser;
        stringizer     = workerTuple.stringizer;
        evaluator      = workerTuple.evaluator;
        mathTexifier   = workerTuple.mathTexifier;

    }

	@After
	public void tearDown() throws Exception {
		evaluator.clearUserData();
        System.gc();
	}

	private void testParser(String suiteName) {
	    /* Create written token set */
        CWrittenTokenSetNoStroke wts = new CWrittenTokenSetNoStroke();

        /* Create token set parser */
        int nPass = 0;
        int nTested = 0;
        long totalParsingTime_ms = 0;

        QADataEntry[] entries = qaDataSet.QADataSuites.get(suiteName).getEntries();
        
        for (int i = 0; i < entries.length; ++i) {
            // for (int i = 0; i < tokenSetNums.length; ++i) {
            String tokenSetFileName = entries[i].getTokenSetFileName();
            String tokenSetTrueString = entries[i].getCorrectParseRes();
            String tokenSetTrueMathTex = entries[i].getCorrectMathTex();
            double[] tokenSetTrueEvalResRange = entries[i].getCorrectEvalResRange();
            Object tokenSetTrueEvalRes = entries[i].getCorrectEvalRes();


            /* Single out option */
            if (singleOutIdx != null && singleOutIdx.length > 0) {
                List<String> singleOutList = Arrays.asList(singleOutIdx);
                if (!singleOutList.contains(tokenSetFileName))
                    continue;
            }

            String tokenSetFN = tokenSetPathPrefix + tokenSetFileName + tokenSetSuffix;

            try {
                wts.readFromFile(tokenSetFN);
            } catch (FileNotFoundException fnfe) {
                System.err.println(fnfe.getMessage());
                System.err.flush();
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
                System.err.flush();
            }

            /* Disable grammar productions, if specified */
            boolean toEnableAllProductions = false;
            String[] grammarNodesToDisable = entries[i].getGrammarNodesToDisable();
            if (grammarNodesToDisable != null && grammarNodesToDisable.length > 0) {
                toEnableAllProductions = true;
                for (String grammarNode : grammarNodesToDisable) {
                    int numDisabled =
                            tokenSetParser.getGraphicalProductionSet().disableProductionsByGrammarNodeName(grammarNode);
                    System.out.println("Disabled " + numDisabled + " production(s) with grammar node name " + grammarNode);
                }
            }

            /* Parse graphically */
            long millis_0 = System.currentTimeMillis();

            Node parseRoot = null;
            try {
                parseRoot = tokenSetParser.parse(wts); /* Parsing action */
            } catch (TokenSetParserException exc) {

            } catch (InterruptedException exc) {
                fail("Failed due to InterruptedException: " + exc.getMessage());
            }

            long millis_1 = System.currentTimeMillis();

            long parsingTime = millis_1 - millis_0;
            totalParsingTime_ms += parsingTime;

            String stringized = stringizer.stringize(parseRoot);
            String evalResStr = null;
            Object evalRes = null;
            if (!stringized.contains(errStr)) {
                try {
                    evalRes = evaluator.eval(parseRoot);
                    evalResStr = evaluator.evalRes2String(evalRes);
                } catch (ParseTreeEvaluatorException exc) {
                    evalResStr = "Evaluation error: " + exc.getMessage();
                }

            }
            
            /* Check stringizer output */
            if ( !stringized.equals(tokenSetTrueString) ) {
                System.err.println("Mismatch: \"" + stringized + "\" != \"" + 
                                   tokenSetTrueString + "\"");
            }
            assertEquals(tokenSetTrueString, stringized);

            boolean checkResult = stringized.equals(tokenSetTrueString);
                    
            /* Check Math TeXifier output */
            String texOut = "";
            if (tokenSetTrueMathTex != null) {
                texOut = mathTexifier.texify(parseRoot);
                
                assertEquals(tokenSetTrueMathTex, texOut);
                checkResult = checkResult && texOut.equals(tokenSetTrueMathTex);
            }
            
            /* Check eval result */
            if (tokenSetTrueEvalRes != null && tokenSetTrueEvalRes.getClass().equals(String.class)) {

                assertTrue(tokenSetTrueEvalRes instanceof String);

                // Assert that error occurred during parse-tree evaluation
                assertTrue(evalResStr.contains((String) tokenSetTrueEvalRes));

            } else if (tokenSetTrueEvalResRange != null || tokenSetTrueEvalRes != null) {
                /* Check type match */
                assertTrue(evalRes.getClass().equals(Undefined.class) ||
                           evalRes.getClass().equals(Double.class) ||
                           evalRes.getClass().equals(String.class) ||
                           evalRes.getClass().equals(Boolean.class));

                if (evalRes.getClass().equals(Undefined.class)) {
                    assertTrue(tokenSetTrueEvalRes instanceof Undefined);
                    assertEquals(tokenSetTrueEvalRes, evalRes);

                } else if (evalRes.getClass().equals(Boolean.class)) {
                    assertEquals((Boolean) tokenSetTrueEvalRes, evalRes);

                } else {
                    double evalResDbl;
                    if (evalRes.getClass().equals(String.class)) {
                        evalResDbl = Double.parseDouble((String) evalRes);
                    } else {
                        evalResDbl = (Double) evalRes;
                    }

                    if (tokenSetTrueEvalResRange != null) {
                        assertTrue(evalResDbl >= tokenSetTrueEvalResRange[0]);
                        assertTrue(evalResDbl <= tokenSetTrueEvalResRange[1]);
                    } else {
                        assertTrue(MathHelper.equalsTol((Double) tokenSetTrueEvalRes, evalResDbl, evalResEqualityAbsTol));
                    }
                }

            }
            
            
            String checkResultStr = checkResult ? "PASS" : "FAIL";
            nPass += checkResult ? 1 : 0;

            String strPrint = "[" + checkResultStr + "] " + "(" + parsingTime
                    + " ms) " + "File " + tokenSetFileName + ": " + "\""
                    + stringized + "\"";            
            if (!checkResult) {
                strPrint += " <> " + " \"" + tokenSetTrueString + "\"";
            }

            strPrint += " {Value = " + evalResStr + "}";
            
            if (tokenSetTrueMathTex != null) {
                String mathTexMatch = checkResult ? "match" : "MISMATCH";
                strPrint += "\n  Math TeX " + mathTexMatch  + ": " +
                        "(output | truth) = (\"" + texOut + "\" | \"" + tokenSetTrueMathTex + "\")";
            }

            if (checkResult) {
                System.out.println(strPrint);
                System.out.flush();
            } else {
                System.err.println(strPrint);
                System.err.flush();
            }

            /* If necessray, re-enable all grammar productions */
            if (toEnableAllProductions) {
                tokenSetParser.getGraphicalProductionSet().enableAllProductions();
                System.out.println("Enabled all productions.");
            }

            nTested++;
        }

        System.out.println("Tested: " + nTested + "; Passed: " + nPass
                + "; Failed: " + (nTested - nPass));
        System.out.println("Total parsing time = " + totalParsingTime_ms
                + " ms");
        System.out.flush();
        
	}
	
	@Test
	public void testParser_basicDecimalNumber() {
	    testParser("basicDecimalNumber");
	}
	
	@Test
    public void testParser_negativePositiveSigns() {
        testParser("negativePositiveSigns");
    }
	
	@Test
	public void testParser_basicNumberAlgebra() {
        testParser("basicNumberAlgebra");
    }
	
	@Test 
	public void testParser_compositeNumberAlgebra() {
        testParser("compositeNumberAlgebra");
    }
	
	@Test 
    public void testParser_algebraWithParentheses() {
        testParser("algebraWithParentheses");
    }
	
	@Test
	public void testParser_fraction() {
        testParser("fraction");
    }
	
	@Test
	public void testParser_exponentiation() {
	    testParser("exponentiation");
	}
	
	@Test
	public void testParser_sqrt() {
	    testParser("sqrt");
	}
	
	@Test
	public void testParser_symbols() {
	    testParser("symbols");
	}

    @Test
    public void testParser_symbolsStateful() {
        testParser("symbolsStateful");
    }
	
	@Test
	public void testParser_function() {
	    testParser("function");
	}

    @Test
    public void testParser_basicMatrix() {
        testParser("basicMatrix");
    }

    @Test
    public void testParser_matrixWithHoles() {
        testParser("matrixWithHoles");
    }

	@Test
    public void testParser_basicMatrixStateful() {
        testParser("basicMatrixStateful");
    }
	
	@Test
    public void testParser_definedFunctions() {
        testParser("definedFunctions");
    }
	
	@Test
    public void testParser_functionEvaluationContextClosure() {
        testParser("functionEvaluationContextClosure");
    }
	
	@Test
    public void testParser_sigmaPiTerm() {
        testParser("sigmaPiTerm");
    }
	
	@Test
	public void testParser_sigmaPiTermEvaluationContextClosure() {
	    testParser("sigmaPiTermEvaluationContextClosure");
	}

    @Test
    public void testParser_defIntegTerm() {
        testParser("defIntegTerm");
    }

    @Test
    public void testParser_performance() {
        testParser("performance");
    }

    @Test
    public void testParser_predefinedConstants() {
        testParser("predefinedConstants");
    }

    @Test
    public void testParser_logicalExpressions() {
        testParser("logicalExpressions");
    }

    @Test
    public void testParser_ifStatements() {
        testParser("ifStatements");
    }

    @Test
    public void testParser_piecewiseFunctionStateful() {
        testParser("piecewiseFunctionStateful");
    }

    @Test
    public void testParser_incorrectSyntax() {
        testParser("incorrectSyntax");
    }

    @Test
    public void testParser_evaluationError() {
        testParser("evaluationError");
    }
}
