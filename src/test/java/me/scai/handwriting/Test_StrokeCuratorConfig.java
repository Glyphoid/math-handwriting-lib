package me.scai.handwriting;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;

import static org.junit.Assert.*;

public class Test_StrokeCuratorConfig {
    private StrokeCuratorConfig config;

    private URL tokenEngineFileUrl;

    private static final String TOKEN_ENGINE_TYPE = "SDV";

    private TokenRecogEngine tokEngine = null;

	@Before
	public void setUp() throws Exception {

        final String testResourcesPath = "test" + File.separator + "resources";

        final URL strokeCuratorConfigUrl = this.getClass().getClassLoader().getResource(testResourcesPath +
                File.separator + "config" + File.separator + "stroke_curator_config_test_1.json");
        config = StrokeCuratorConfig.fromJsonFileAtUrl(strokeCuratorConfigUrl);

        tokenEngineFileUrl = this.getClass().getClassLoader().getResource(testResourcesPath +
                File.separator + "token_engine" + File.separator + "token_engine.sdv.sz0_whr1_ns1.ser");

//        URL tokenEngineFileUrl = new File(tokenEnginePath).toURI().toURL();

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
//        StrokeCuratorConfig config = StrokeCuratorConfig.fromJsonFile(configFilePath);

		assertEquals(config.tokenPairRules.size(), 2);

        assertEquals("http://127.0.0.1:11610/glyphoid/token-recog", config.getRemoteTokenEngineUrl());
				
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

