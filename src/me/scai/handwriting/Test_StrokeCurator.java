package me.scai.handwriting;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by scai on 5/31/2015.
 */
public class Test_StrokeCurator {
    private static final String RESOURCES_DIR = "resources";
    private static final String RESOURCES_CONFIG_DIR = "config";
    private static final String STROKE_CURATOR_CONFIG_FILE = "stroke_curator_config.json";

    private static final String RESOURCES_TOKEN_ENGINE_DIR = "token_engine";
    private static final String TOKEN_ENGINE_FILE_NAME = "token_engine.sdv.sz0_whr0_ns1.ser";

    private StrokeCurator curator;

    @Before
    public void setup() {
        URL strokeCuratorConfigUrl = this.getClass().getClassLoader().getResource(File.separator + RESOURCES_DIR +
                File.separator + RESOURCES_CONFIG_DIR +
                File.separator + STROKE_CURATOR_CONFIG_FILE);

        URL tokenEngineFileUrl = this.getClass().getClassLoader().getResource(File.separator + RESOURCES_DIR +
                File.separator + RESOURCES_TOKEN_ENGINE_DIR +
                File.separator + TOKEN_ENGINE_FILE_NAME);

        TokenRecogEngine tokenRecogEngine = null;
        try {
            tokenRecogEngine = readTokenEngine(tokenEngineFileUrl);
        } catch (Exception exc) {
            fail("Failed to read token engine");
        }

        curator = new StrokeCuratorConfigurable(strokeCuratorConfigUrl, tokenRecogEngine);
    }

    //TODO : refactor, avoid duplication
    private TokenRecogEngine readTokenEngine(final URL tokenEngineFileUrl)
            throws IOException, ClassNotFoundException{
        ObjectInputStream objInStream = null;
        boolean readSuccessful = false;
        TokenRecogEngine tokEngine = null;
        try {
            objInStream = new ObjectInputStream(new BufferedInputStream(tokenEngineFileUrl.openStream()));

            tokEngine = (TokenRecogEngineSDV) objInStream.readObject();

            readSuccessful = true;
        } finally {
            try {
                objInStream.close();
            } catch (IOException e) {
                //TODO
            }
        }

        return tokEngine;
    }

    @Test
    public void testUnmergeStroke1() {
        /* Add 1st stroke of "=" */
        CStroke stroke0 = new CStroke(0.0f, 0.0f);
        stroke0.addPoint(10f, 0.0f);
        stroke0.addPoint(20f, 0.0f);
        stroke0.addPoint(30f, 0.0f);
        stroke0.addPoint(40f, 0.0f);

        curator.addStroke(stroke0);

        assertEquals(curator.getWrittenTokenRecogWinners().size(), 1);
        assertEquals(curator.getWrittenTokenRecogWinners().get(0), "-");

        /* Add 2nd stroke of "=" */
        CStroke stroke1 = new CStroke(0.0f, 10f);
        stroke1.addPoint(10f, 10f);
        stroke1.addPoint(20f, 10f);
        stroke1.addPoint(30f, 10f);
        stroke1.addPoint(40f, 10f);

        curator.addStroke(stroke1);

        assertEquals(curator.getWrittenTokenRecogWinners().size(), 1);
        assertEquals(curator.getWrittenTokenRecogWinners().get(0), "=");

        /* Unmerge the 2nd stroke */
        curator.mergeStrokesAsToken(new int[] {1});

        assertEquals(curator.getWrittenTokenRecogWinners().size(), 2);
        assertEquals(curator.getWrittenTokenRecogWinners().get(0), "-");
        assertEquals(curator.getWrittenTokenRecogWinners().get(1), "-");
    }

    @Test
    public void testUnmergeStroke2() {
        /* Add 1st stroke of "T" */
        CStroke stroke0 = new CStroke(0.0f, 0.0f);
        stroke0.addPoint(10f, 0.0f);
        stroke0.addPoint(20f, 0.0f);
        stroke0.addPoint(30f, 0.0f);
        stroke0.addPoint(40f, 0.0f);

        curator.addStroke(stroke0);

        assertEquals(curator.getWrittenTokenRecogWinners().size(), 1);
        assertEquals(curator.getWrittenTokenRecogWinners().get(0), "-");

        /* Add 2nd stroke of "T" */
        CStroke stroke1 = new CStroke(20f, 0f);
        stroke1.addPoint(20f, 10f);
        stroke1.addPoint(20f, 20f);
        stroke1.addPoint(20f, 30f);
        stroke1.addPoint(20f, 40f);

        curator.addStroke(stroke1);

        assertEquals(curator.getWrittenTokenRecogWinners().size(), 1);
        assertEquals(curator.getWrittenTokenRecogWinners().get(0), "T");

        /* Add 1st stroke of "=" */
        CStroke stroke2 = new CStroke(50.0f, 0.0f);
        stroke2.addPoint(60f, 0.0f);
        stroke2.addPoint(70f, 0.0f);
        stroke2.addPoint(80f, 0.0f);
        stroke2.addPoint(90f, 0.0f);

        curator.addStroke(stroke2);

        assertEquals(curator.getWrittenTokenRecogWinners().size(), 2);
        assertEquals(curator.getWrittenTokenRecogWinners().get(0), "T");
        assertEquals(curator.getWrittenTokenRecogWinners().get(1), "-");

        /* Add 1st stroke of "=" */
        CStroke stroke3 = new CStroke(50.0f, 20f);
        stroke3.addPoint(60f, 20f);
        stroke3.addPoint(70f, 20f);
        stroke3.addPoint(80f, 20f);
        stroke3.addPoint(90f, 20f);

        curator.addStroke(stroke3);

        assertEquals(curator.getWrittenTokenRecogWinners().size(), 2);
        assertEquals(curator.getWrittenTokenRecogWinners().get(0), "T");
        assertEquals(curator.getWrittenTokenRecogWinners().get(1), "=");
        assertEquals(curator.getNumStrokes(), 4);

        /* Unmerge the 2nd stroke of "=" */
        curator.mergeStrokesAsToken(new int[] {3});

        assertEquals(curator.getWrittenTokenRecogWinners().size(), 3);
        assertEquals(curator.getWrittenTokenRecogWinners().get(0), "T");
        assertEquals(curator.getWrittenTokenRecogWinners().get(1), "-");
        assertEquals(curator.getWrittenTokenRecogWinners().get(2), "-");
    }

}
