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

    @Test
    public void testGetSerialization() {
        CStroke[] strokesToAdd = new CStroke[2];

        CStroke stroke0 = new CStroke();
        stroke0.addPoint(0.0f, 0.0f);
        stroke0.addPoint(10.0f, 0.1f);
        stroke0.addPoint(20.0f, 0.0f);
        stroke0.addPoint(30.0f, 0.05f);

        CStroke stroke1 = new CStroke();
        stroke1.addPoint(0.0f, 10.0f);
        stroke1.addPoint(10.0f, 10.95f);
        stroke1.addPoint(20.0f, 10.0f);
        stroke1.addPoint(30.0f, 10.02f);

        strokesToAdd[0] = stroke0;
        strokesToAdd[1] = stroke1;

        URL configFileUrl = null;
        try {
            configFileUrl = new File(configFilePath).toURI().toURL();
        } catch (MalformedURLException exc) {
            fail();
        }

        StrokeCurator curator = new StrokeCuratorConfigurable(configFileUrl, tokEngine);

        for (CStroke strokeToAdd : strokesToAdd) {
            curator.addStroke(strokeToAdd);
        }

        List<String> serializedStrokes = curator.getSerializedStrokes();

        assertEquals(strokesToAdd.length, serializedStrokes.size());
        for (int i = 0; i < serializedStrokes.size(); ++i) {
            String serializedStroke = serializedStrokes.get(i);

            try {
                CStroke stroke = CStrokeJsonHelper.json2CStroke(serializedStroke);
                assertEquals(strokesToAdd[i].nPoints(), stroke.nPoints());
            } catch (CStrokeJsonHelper.CStrokeJsonConversionException exc) {
                fail("Failed due to " + exc.getMessage());
            }
        }

        String serializedTokenSet = curator.getSerializedTokenSet();
        assertNotNull(serializedTokenSet);
        assertFalse(serializedTokenSet.isEmpty());

        String serializedWtConstStrokeIndices = curator.getSerializedConstStrokeIndices();
        assertNotNull(serializedWtConstStrokeIndices);
        assertFalse(serializedWtConstStrokeIndices.isEmpty());

        String serializedState = curator.getStateSerializationString();
        assertNotNull(serializedState);
        assertFalse(serializedState.isEmpty());

        /* Deserializatoin / injection */
        StrokeCurator curatorPrime = new StrokeCuratorConfigurable(configFileUrl, tokEngine);

        curatorPrime.injectSerializedState((new JsonParser()).parse(serializedState).getAsJsonObject());

        assertEquals(curator.getNumStrokes(), curatorPrime.getNumStrokes());
        assertEquals(curator.getNumTokens(), curatorPrime.getNumTokens());

        assertEquals(curator.getWrittenTokenSet().recogWinners.size(), curatorPrime.getWrittenTokenSet().recogWinners.size());
        assertEquals(curator.getWrittenTokenSet().recogPs.size(), curatorPrime.getWrittenTokenSet().recogPs.size());

        assertEquals(curator.getWrittenTokenConstStrokeIndices().size(), curatorPrime.getWrittenTokenConstStrokeIndices().size());
        List<int[]> constStrokeIndices = curator.getWrittenTokenConstStrokeIndices();
        List<int[]> primeConstStrokeIndices = curatorPrime.getWrittenTokenConstStrokeIndices();
        for (int i = 0; i < constStrokeIndices.size(); ++i) {
            assertArrayEquals(constStrokeIndices.get(i), primeConstStrokeIndices.get(i));
        }

    }


}

