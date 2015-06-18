package me.scai.parsetree;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.net.URL;

import me.scai.handwriting.CWrittenTokenSetNoStroke;
import me.scai.parsetree.evaluation.ParseTreeEvaluator;
import me.scai.parsetree.evaluation.ParseTreeEvaluatorException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class Test_TokenSetParser {   
	public static final String errStr = ParseTreeStringizer.parsingErrString;
	public static final double evalResEqualityAbsTol = 1e-9;
	
	private static final String RESOURCES_DIR = "resources";
	private static final String TERMINALS_FILE_NAME = "terminals.json";
	private static final String PRODUCTIONS_FILE_NAME = "productions.txt";
	private static final String RESOURCES_CONFIG_DIR = "config";
	
	String[] singleOutIdx = {};
	Test_QADataSet qaDataSet = new Test_QADataSet();
	
	String tokenSetSuffix = ".wts";
	String tokenSetPrefix = null;
	URL prodSetFN = null;
	URL termSetFN = null;
	
	GraphicalProductionSet gpSet;
	TerminalSet termSet;
	TokenSetParser tokenSetParser;
	ParseTreeStringizer stringizer;
	ParseTreeEvaluator evaluator;
	ParseTreeMathTexifier mathTexifier;
	
	/* Constructor */
	public Test_TokenSetParser() {
	    String hostName;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
			if (hostName.toLowerCase().equals("ceres")) {
				tokenSetPrefix = "C:\\Users\\scai\\Dropbox\\Plato\\data\\tokensets\\TS_";
//				prodSetFN = "C:\\Users\\scai\\Dropbox\\javaWS\\handwriting\\graph_lang\\productions.txt";
//				termSetFN = "C:\\Users\\scai\\Dropbox\\javaWS\\handwriting\\graph_lang\\terminals.txt";
			} else {
				tokenSetPrefix = "C:\\Users\\scai\\Dropbox\\Plato\\data\\tokensets\\TS_";
//				prodSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\productions.txt";
//				termSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\terminals.txt";
			}
		} catch (Exception e) {
			System.err.println("Cannot determine host name");
		}
		
		prodSetFN = Thread.currentThread().getContextClassLoader().getResource(File.separator + RESOURCES_DIR + 
						                        File.separator + RESOURCES_CONFIG_DIR + 
						                        File.separator + PRODUCTIONS_FILE_NAME);
		termSetFN = Thread.currentThread().getContextClassLoader().getResource(File.separator + RESOURCES_DIR + 
								                File.separator + RESOURCES_CONFIG_DIR + 
								                File.separator + TERMINALS_FILE_NAME);
		
		try {
//	      termSet = TerminalSet.createFromUrl(termSetFN);
	        termSet = TerminalSet.createFromJsonAtUrl(termSetFN);
	    } catch (Exception e) {
	        System.err.println(e.getMessage());
	        System.err.flush();
	    }
		
		gpSet = null;
        try {
//          gpSet = GraphicalProductionSet.createFromFile(prodSetFN, termSet);
            gpSet = GraphicalProductionSet.createFromUrl(prodSetFN, termSet);
        } catch (FileNotFoundException fnfe) {
            System.err.println(fnfe.getMessage());
            System.err.flush();
            throw new RuntimeException(
                    "Error occurred during the creation of graphical production set from file: File not found");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.flush();
            throw new RuntimeException(
                    "Error occurred during the creation of graphical production set from file: File I/O exception");
        }
        
        tokenSetParser = new TokenSetParser(termSet, gpSet, 0.90f);
        stringizer = gpSet.genStringizer();
        evaluator = gpSet.genEvaluator();
        mathTexifier = new ParseTreeMathTexifier(gpSet, termSet);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		evaluator.clearUserData();
	}

	private void testParser(String suiteName) {
	    /* Create written token set */
        CWrittenTokenSetNoStroke wts = new CWrittenTokenSetNoStroke();
        
//      NodeInternalGeometry nodeInternalGeom = new NodeInternalGeometry(termSet); //DEBUG
        
        /* Create token set parser */
        int nPass = 0;
        int nTested = 0;
        long totalParsingTime_ms = 0;

        QADataEntry [] entries = qaDataSet.QADataSuites.get(suiteName).getEntries();
        
        for (int i = 0; i < entries.length; ++i) {
            // for (int i = 0; i < tokenSetNums.length; ++i) {
            String tokenSetFileName = entries[i].getTokenSetFileName();
            String tokenSetTrueString = entries[i].getCorrectParseRes();
            String tokenSetTrueMathTex = entries[i].getCorrectMathTex();
            Object tokenSetTrueEvalRes = entries[i].getCorrectEvalRes();

            /* Single out option */
            if (singleOutIdx != null && singleOutIdx.length > 0) {
                List<String> singleOutList = Arrays.asList(singleOutIdx);
                if (!singleOutList.contains(tokenSetFileName))
                    continue;
            }

            String tokenSetFN = tokenSetPrefix + tokenSetFileName
                    + tokenSetSuffix;

            try {
                wts.readFromFile(tokenSetFN);
            } catch (FileNotFoundException fnfe) {
                System.err.println(fnfe.getMessage());
                System.err.flush();
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
                System.err.flush();
            }

            /* Parse graphically */
            long millis_0 = System.currentTimeMillis();

            Node parseRoot = tokenSetParser.parse(wts); /* Parsing action */

            long millis_1 = System.currentTimeMillis();

            long parsingTime = millis_1 - millis_0;
            totalParsingTime_ms += parsingTime;
            
//          List<float []> allBounds = nodeInternalGeom.getMajorTokenBounds(parseRoot); //DEBUG
//          float maxTokenWidth = nodeInternalGeom.getMaxMajorTokenWidth(parseRoot);

            String stringized = stringizer.stringize(parseRoot);
            String evalResStr = null;
            Object evalRes = null;
            if (!stringized.contains(errStr)) {
                try {
                    evalRes = evaluator.eval(parseRoot);
                    evalResStr = evaluator.evalRes2String(evalRes);
                }
                catch (ParseTreeEvaluatorException exc) {
                    evalResStr = "[Evaluator exception occurred]";
                }
                
//              if (!evalRes.getClass().equals(Double.class)) {
//                  throw new RuntimeException(
//                          "Unexpected return type from evaluator");
//              }
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
            if (tokenSetTrueEvalRes != null) {
                if (tokenSetTrueEvalRes.getClass().equals(Double.class)) {
                    /* Check type match */
                    assertTrue(evalRes.getClass().equals(Double.class) || evalRes.getClass().equals(String.class));
                    
                    double evalResDbl;
                    if (evalRes.getClass().equals(String.class)) {
                        evalResDbl = Double.parseDouble((String) evalRes);
                    }
                    else {
                        evalResDbl = (Double) evalRes;
                    }
                    
                    assertTrue(MathHelper.equalsTol(evalResDbl, (Double) tokenSetTrueEvalRes, evalResEqualityAbsTol));
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
}
