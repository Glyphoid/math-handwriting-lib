package me.scai.handwriting;

import com.google.gson.JsonParser;
import me.scai.plato.helpers.CStrokeJsonHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class Test_StrokeCuratorConfig {
    private static final String configFilePath  = "C:\\Users\\scai\\Dropbox\\Plato\\test-data\\stroke_curator_config_testCase_1.json";
    private static final String tokenEnginePath = "C:\\Users\\scai\\Dropbox\\Plato\\test-data\\token_engine.sdv.sz0_whr0_ns1.ser";
    private static final String TOKEN_ENGINE_TYPE = "SDV";

    private TokenRecogEngine tokEngine = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

        URL tokenEngineFileUrl = new File(tokenEnginePath).toURI().toURL();

        ObjectInputStream objInStream = null;
        boolean readSuccessful = false;
        try {
//			objInStream = new ObjectInputStream(new FileInputStream(
//					tokenEngineFN));
            objInStream = new ObjectInputStream(new BufferedInputStream(tokenEngineFileUrl.openStream()));

            if (TOKEN_ENGINE_TYPE.equals("SDV")) {
                tokEngine = (TokenRecogEngineSDV) objInStream.readObject();
            } else if (TOKEN_ENGINE_TYPE.equals("IMG")) {
                tokEngine = (TokenRecogEngineIMG) objInStream.readObject();
            } else {
                throw new RuntimeException("Unrecognized token engine type: " + TOKEN_ENGINE_TYPE);
            }

            readSuccessful = true;
            // objInStream.close();
        } catch (IOException e) {
            fail();
        } catch (ClassNotFoundException e) {
            fail();
        } finally {
            try {
                objInStream.close();
            } catch (IOException e) {
                fail();
            }
        }

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
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

