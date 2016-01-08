package me.scai.handwriting.ml;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.scai.handwriting.TokenRecogEngineSDV;
import me.scai.parsetree.TextHelper;

import java.io.*;
import java.net.URL;
import java.util.logging.Logger;

public class TrainTokenRecogEngineSDV {
    /* Constants */
    private static final String RESOURCES_ROOT = "main";
    private static final String RESOURCES_DIR = "resources";
    private static final String RESOURCES_CONFIG_DIR = "config";

    private static final int DEFAULT_NP_PER_STROKE = 16;
    private static final int DEFAULT_MAX_NUM_STROKES = 4;
    private static final boolean DEFAULT_INCLUDE_TOKEN_SIZE = false;
    private static final boolean DEFAULT_INCLUDE_TOKEN_WH_RATIO = true;
    private static final boolean DEFAULT_INCLUDE_TOKEN_NUM_STROKES = true;

    private static final int HIDDEN_LAYER_SIZE_2 = 0;
    private static final double TRAIN_THRESH_ERR = 0.001;
    
    private static final Logger logger = Logger.getLogger(TrainTokenRecogEngineSDV.class.getName());

    /* Member variables */
    private int hiddenLayerSize1;

    private int npPerStroke;
    private int maxNumStrokes;

    private int maxIter;

    private boolean includeTokenSize = false;
    private boolean includeTokenWHRatio = true;
    private boolean includeTokenNumStrokes = true;

    /* Methods */
    // Get the property values from Java system properties
    public TrainTokenRecogEngineSDV() {
        hiddenLayerSize1 = Integer.parseInt(System.getProperty("hiddenLayerSize")); //250
        assert(hiddenLayerSize1 > 0);

        maxIter = Integer.parseInt(System.getProperty("maxIter")); //500;
        assert(maxIter > 0);

        // Allow the overriding of token feature vector settings
        npPerStroke = System.getProperty("npPerStroke") != null ?
                Integer.parseInt(System.getProperty("npPerStroke")) :
                DEFAULT_NP_PER_STROKE;
        assert(npPerStroke > 0);

        maxNumStrokes = System.getProperty("maxNumStrokes") != null ?
                Integer.parseInt(System.getProperty("maxNumStrokes")) :
                DEFAULT_MAX_NUM_STROKES;
        assert(maxNumStrokes > 0);

        includeTokenSize = System.getProperty("maxNumStrokes") != null ?
                Boolean.parseBoolean(System.getProperty("maxNumStrokes")) :
                DEFAULT_INCLUDE_TOKEN_SIZE;

        includeTokenWHRatio = System.getProperty("includeTokenWHRatio") != null ?
                Boolean.parseBoolean(System.getProperty("maxNumStrokes")) :
                DEFAULT_INCLUDE_TOKEN_WH_RATIO;

        includeTokenNumStrokes = System.getProperty("includeTokenNumStrokes") != null ?
                Boolean.parseBoolean(System.getProperty("includeTokenNumStrokes")) :
                DEFAULT_INCLUDE_TOKEN_NUM_STROKES;

        // Report settings to console
        logger.info("Hidden layer size = " + hiddenLayerSize1);
        logger.info("Maximum number of iterations = " + maxIter);

        logger.info("Include token size = " + includeTokenSize);
        logger.info("Include token WH ratio = " + includeTokenWHRatio);
        logger.info("Include token number of strokes = " + includeTokenNumStrokes);
    }


    /* Training routine */
    public void train(String tokenDir, String dataDir, String engineDir) {
        final String tokenEngineSerFN = engineDir + File.separator +
                String.format("token_engine.sdv.hls%d.sz%d_whr%s_ns%s.ser",
                              hiddenLayerSize1,
                              includeTokenSize ? 1 : 0,
                              includeTokenWHRatio ? 1 : 0,
                              includeTokenNumStrokes ? 1 : 0);

        TokenRecogEngineSDV tokEngine = new TokenRecogEngineSDV(hiddenLayerSize1,
                                                                HIDDEN_LAYER_SIZE_2,
                                                                maxIter,
                                                                TRAIN_THRESH_ERR);

        /* Load terminal set and token degeneracy */
        final String TERMINAL_CONFIG_FN = "terminals.json";
        final URL terminalConfigUrl =  Thread.currentThread().getContextClassLoader().getResource(
                RESOURCES_ROOT  +
                File.separator + RESOURCES_DIR +
                File.separator + RESOURCES_CONFIG_DIR +
                File.separator + TERMINAL_CONFIG_FN);

        String lines;
        try {
            lines = TextHelper.readTextFileAtUrl(terminalConfigUrl);
        }
        catch ( Exception e ) {
            throw new RuntimeException("Failed to read token degeneracy data from URL: \"" + terminalConfigUrl + "\"");
        }

        JsonObject tokenDegenObj = new JsonParser().parse(lines).getAsJsonObject().get("tokenDegeneracy").getAsJsonObject();
        tokEngine.loadTokenDegeneracy(tokenDegenObj); /* TODO: Make more elegant */
        /* ~Load token degeneracy */

        String [] hardCodedTokens = {"."};
        tokEngine.setHardCodedTokens(hardCodedTokens);

        logger.info("New instance of tokEngine created.");

        int [] ivs = new int[2];
        ivs[0] = npPerStroke;
        ivs[1] = maxNumStrokes;

        boolean [] bvs = new boolean[3];
        bvs[0] = includeTokenSize;
        bvs[1] = includeTokenWHRatio;
        bvs[2] = includeTokenNumStrokes;

        tokEngine.setFeatures(ivs, bvs);

        tokEngine.train(tokenDir, dataDir);

        logger.info("Final test error rate = " + tokEngine.getTestErrorRate());

        /* Serialize to file */
        ObjectOutputStream objOutStream = null;
        try {
            objOutStream = new ObjectOutputStream(new FileOutputStream(tokenEngineSerFN));
            objOutStream.writeObject(tokEngine);
            objOutStream.flush();

        }
        catch (IOException e) {
            System.err.println("WRITE_TOKEN_ENGINE_ERROR: Failed to write token engine to file: " + tokenEngineSerFN);
        } finally {
            try {
                objOutStream.close();
            } catch (IOException e) {
                System.err.println("TOKEN_ENGINE_OUTPUT_STREAM_CLOSE_ERR: Failed to close token engine output stream");
            }
        }

        logger.info("Wrote token engine to file \"" + tokenEngineSerFN + "\"");

    }

    /* Main routine for training */
    public static void main(String[] args) {

        TrainTokenRecogEngineSDV trainer = new TrainTokenRecogEngineSDV();

        if (System.getProperty("tokenDir") == null) {
            throw new IllegalArgumentException("Missing system property: tokenDir");
        }
        final String tokenDir = System.getProperty("tokenDir");

        if (System.getProperty("engineDir") == null) {
            throw new IllegalArgumentException("Missing system property: engineDir");
        }
        final String engineDir = System.getProperty("engineDir");

        if (System.getProperty("dataDir") == null) {
            throw new IllegalArgumentException("Missing system property: dataDir");
        }
        final String dataDir = System.getProperty("dataDir");


        trainer.train(tokenDir, dataDir, engineDir);

    }
}
