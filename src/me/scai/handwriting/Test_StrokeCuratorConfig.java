package me.scai.handwriting;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Test_StrokeCuratorConfig {

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
	}

	@Test
	public void test() {
		final String configFilePath = "C:\\Users\\scai\\Dropbox\\javaWS\\handwriting\\graph_lang\\stroke_curator_config_testCase_1.json";
		
		StrokeCuratorConfig config = StrokeCuratorConfig.fromJsonFile(configFilePath);
		
		assertEquals(config.tokenPairRules.size(), 2);
				
		assertEquals(config.tokenPairRules.get(0).tokenA, "-");
		assertEquals(config.tokenPairRules.get(0).tokenB, "-");		
		assertEquals(config.tokenPairRules.get(0).predicaments.size(), 4);
		assertEquals(config.tokenPairRules.get(0).predicaments.get(0), "relativeLengthDifference < 0.5");
		assertEquals(config.tokenPairRules.get(0).predicaments.get(1), "relativeLeftXOffset < 0.2");
		assertEquals(config.tokenPairRules.get(0).predicaments.get(2), "relativeRightXOffset < 0.2");
		assertEquals(config.tokenPairRules.get(0).predicaments.get(3), "numTokensInBetween == 0");
		assertEquals(config.tokenPairRules.get(0).recommendation, "mergeAs: \"=\"");
		
		assertEquals(config.tokenPairRules.get(1).tokenA, "tick");
		assertEquals(config.tokenPairRules.get(1).tokenB, "-");		
		assertEquals(config.tokenPairRules.get(1).predicaments.size(), 0);
		assertEquals(config.tokenPairRules.get(1).recommendation, "mergeAs: \"root\"");
		
		assertEquals(config.mergePartners.size(), 1);
		assertEquals(config.mergePartners.get("root").size(), 0);
	}

}
