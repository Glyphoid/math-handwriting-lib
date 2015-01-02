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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Test_TokenSetParser {
	public static final String errStr = ParseTreeStringizer.parsingErrString;
	
	private static final String RESOURCES_DIR = "resources";
	private static final String TERMINALS_FILE_NAME = "terminals.txt";
	private static final String PRODUCTIONS_FILE_NAME = "productions.txt";
	private static final String RESOURCES_CONFIG_DIR = "config";
	
	String[] singleOutIdx = {};
	Test_QADataSet qaDataSet = null;
	
	String tokenSetSuffix = ".wts";
	String tokenSetPrefix = null;
	URL prodSetFN = null;
	URL termSetFN = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
		
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
		
		qaDataSet = new Test_QADataSet();
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testParser() {
		/* Create written token set */
		CWrittenTokenSetNoStroke wts = new CWrittenTokenSetNoStroke();
		
		TerminalSet termSet = null;
		try {
//			termSet = TerminalSet.createFromFile(termSetFN);
			termSet = TerminalSet.createFromUrl(termSetFN);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.err.flush();
		}
		
		GraphicalProductionSet gpSet = null;
		try {
//			gpSet = GraphicalProductionSet.createFromFile(prodSetFN, termSet);
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
		
		TokenSetParser tokenSetParser = new TokenSetParser(termSet, gpSet,
				0.90f);
		ParseTreeStringizer stringizer = gpSet.genStringizer();
		ParseTreeEvaluator evaluator = gpSet.genEvaluator();
		
		/* Create token set parser */
		int nPass = 0;
		int nTested = 0;
		long totalParsingTime_ms = 0;

		for (int i = 0; i < qaDataSet.entries.length; ++i) {
			// for (int i = 0; i < tokenSetNums.length; ++i) {
			String tokenSetFileName = qaDataSet.entries[i].tokenSetFileName;
			String tokenSetTrueString = qaDataSet.entries[i].correctParseRes;

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

			String stringized = stringizer.stringize(parseRoot);
			Object evalRes = null;
			if (!stringized.contains(errStr)) {
				try {
					evalRes = evaluator.eval(parseRoot);
				}
				catch (Exception exc) {
					fail("Failed due to evaluator exception");
				}
				
				if (!evalRes.getClass().equals(Double.class)) {
					throw new RuntimeException(
							"Unexpected return type from evaluator");
				}
			}
			
			if ( !stringized.equals(tokenSetTrueString) ) {
				System.err.println("Mismatch: \"" + stringized + "\" != \"" + 
			                       tokenSetTrueString + "\"");
			}
			assertEquals(stringized, tokenSetTrueString);

			boolean checkResult = stringized.equals(tokenSetTrueString);
			String checkResultStr = checkResult ? "PASS" : "FAIL";
			nPass += checkResult ? 1 : 0;

			String strPrint = "[" + checkResultStr + "] " + "(" + parsingTime
					+ " ms) " + "File " + tokenSetFileName + ": " + "\""
					+ stringized + "\"";
			if (!checkResult)
				strPrint += " <> " + " \"" + tokenSetTrueString + "\"";

			strPrint += " {Value = " + evalRes + "}";

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

}
