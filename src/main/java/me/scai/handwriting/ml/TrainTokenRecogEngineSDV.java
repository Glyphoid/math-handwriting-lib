package me.scai.handwriting.ml;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.scai.handwriting.TokenRecogEngineSDV;
import me.scai.parsetree.TextHelper;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.*;
import java.net.URL;
import java.util.logging.Logger;

public class TrainTokenRecogEngineSDV {
  /* Constants */
  private static final String RESOURCES_ROOT = "main";
  private static final String RESOURCES_DIR = "resources";
  private static final String RESOURCES_CONFIG_DIR = "config";

  public static final int DEFAULT_MAX_ITER = 10000;
  public static final int DEFAULT_HIDDEN_LAYER_SIZE_1 = 250;
  public static final int DEFAULT_NP_PER_STROKE = 16;
  public static final int DEFAULT_MAX_NUM_STROKES = 4;
  public static final boolean DEFAULT_INCLUDE_TOKEN_SIZE = false;
  public static final boolean DEFAULT_INCLUDE_TOKEN_WH_RATIO = true;
  public static final boolean DEFAULT_INCLUDE_TOKEN_NUM_STROKES = true;

  private static final int HIDDEN_LAYER_SIZE_2 = 0;
  private static final double TRAIN_THRESH_ERR = 0.001;
    
  private static final Logger logger = Logger.getLogger(TrainTokenRecogEngineSDV.class.getName());

  /* Member variables */
  private int maxIter;

  private int hiddenLayerSize1;
  private int npPerStroke;
  private int maxNumStrokes;
  private boolean includeTokenSize = false;
  private boolean includeTokenWHRatio = true;
  private boolean includeTokenNumStrokes = true;

  /* Methods */
  // Get the property values from Java system properties
  public TrainTokenRecogEngineSDV(int maxIter,
                                  int hiddenLayerSize1,
                                  int npPerStroke,
                                  int maxNumStrokes,
                                  boolean includeTokenSize,
                                  boolean includeTokenWHRatio,
                                  boolean includeTokenNumStrokes) {
    this.maxIter = maxIter;
    this.hiddenLayerSize1 = hiddenLayerSize1;
    this.npPerStroke = npPerStroke;
    this.maxNumStrokes = maxNumStrokes;
    this.includeTokenSize = includeTokenSize;
    this.includeTokenWHRatio = includeTokenWHRatio;
    this.includeTokenNumStrokes = includeTokenNumStrokes;

    // Report settings to console.
    logger.info("Maximum number of iterations = " + this.maxIter);
    logger.info("Hidden layer size = " + this.hiddenLayerSize1);
    logger.info("Number of points per stroke = " + this.npPerStroke);
    logger.info("Maximum number of strokes = " + this.maxNumStrokes);
    logger.info("Include token size = " + this.includeTokenSize);
    logger.info("Include token WH ratio = " + includeTokenWHRatio);
    logger.info("Include token number of strokes = " + includeTokenNumStrokes);
  }

  /* Training routine */
  public void train(int newImageSize,
                    String tokenDir,
                    String dataDir,
                    String engineDir,
                    boolean generateIntermediateDataOnly) {
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
    } catch ( Exception e ) {
      throw new RuntimeException("Failed to read token degeneracy data from URL: \"" + terminalConfigUrl + "\"");
    }

    JsonObject tokenDegenObj = new JsonParser()
        .parse(lines)
        .getAsJsonObject()
        .get("tokenDegeneracy")
        .getAsJsonObject();
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

    if (generateIntermediateDataOnly) {
      tokEngine.generateIntermediateData(tokenDir, dataDir, newImageSize);
    } else {
      tokEngine.train(tokenDir, dataDir, newImageSize);

      logger.info("Final test error rate = " + tokEngine.getTestErrorRate());

      /* Serialize to file */
      ObjectOutputStream objOutStream = null;
      try {
        objOutStream = new ObjectOutputStream(new FileOutputStream(tokenEngineSerFN));
        objOutStream.writeObject(tokEngine);
        objOutStream.flush();
      } catch (IOException e) {
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
  }

  /* Main routine for training */
  public static void main(String[] args) {
    ArgumentParser parser = ArgumentParsers.newArgumentParser("TrainTokenRecogEngineSDV")
        .description("Train token recognition engine");
    parser.addArgument("--token_dir")
        .dest("tokenDir")
        .type(String.class)
        .help("Input token data directory");
    parser.addArgument("--data_dir")
        .dest("dataDir")
        .type(String.class)
        .help("Directory to write the intermediate-format data to");
    parser.addArgument("--engine_dir")
        .dest("engineDir")
        .type(String.class)
        .help("Directory to write the trained engine to");
    parser.addArgument("--new_image_size")
        .dest("newImageSize")
        .type(Integer.class)
        .help("Image size (along one dimension) in the new paradigm");
    parser.addArgument("--generate_intermediate_data_only")
        .dest("generateIntermediateDataOnly")
        .type(Boolean.class)
        .setDefault(false)
        .help("Only generate the intermediate data files; do not do training");

    parser.addArgument("--max_iter")
        .dest("maxIter")
        .type(Integer.class)
        .setDefault(TrainTokenRecogEngineSDV.DEFAULT_MAX_ITER)
        .help("Maximum number of training iterations");
    parser.addArgument("--hidden_layer_size_1")
        .dest("hiddenLayerSize1")
        .type(Integer.class)
        .setDefault(TrainTokenRecogEngineSDV.DEFAULT_HIDDEN_LAYER_SIZE_1)
        .help("Size of hidden layer 1");
    parser.addArgument("--np_per_stroke")
        .dest("npPerStroke")
        .type(Integer.class)
        .setDefault(TrainTokenRecogEngineSDV.DEFAULT_NP_PER_STROKE)
        .help("Number of points used to represent each stroke");
    parser.addArgument("--max_num_strokes")
        .dest("maxNumStrokes")
        .type(Integer.class)
        .setDefault(TrainTokenRecogEngineSDV.DEFAULT_NP_PER_STROKE)
        .help("Maximum number of strokes");
    parser.addArgument("--include_token_size")
        .dest("includeTokenSize")
        .type(Boolean.class)
        .setDefault(TrainTokenRecogEngineSDV.DEFAULT_INCLUDE_TOKEN_SIZE)
        .help("Whether to include token size as a feature");
    parser.addArgument("--inclue_token_wh_ratio")
        .dest("includeTokenWHRatio")
        .type(Boolean.class)
        .setDefault(TrainTokenRecogEngineSDV.DEFAULT_INCLUDE_TOKEN_WH_RATIO)
        .help("Whether to include token width-to-height ratio as a feature");
    parser.addArgument("--include_token_num_strokes")
        .dest("includeTokenNumStrokes")
        .type(Boolean.class)
        .setDefault(TrainTokenRecogEngineSDV.DEFAULT_INCLUDE_TOKEN_NUM_STROKES)
        .help("Whether to include number of strokes of a token as a feature");

    Namespace parsed = null;
    try {
        parsed = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
        parser.handleError(e);
    }

    final String tokenDir = (String) parsed.get("tokenDir");
    final String dataDir = (String) parsed.get("dataDir");
    final String engineDir = (String) parsed.get("engineDir");
    final int newImageSize = (int) parsed.get("newImageSize");
    final boolean generateIntermediateDataOnly =
        (boolean) parsed.get("generateIntermediateDataOnly");

    final int maxIter = (int) parsed.get("maxIter");
    final int hiddenLayerSize1 = (int) parsed.get("hiddenLayerSize1");
    final int npPerStroke = (int) parsed.get("npPerStroke");
    final int maxNumStrokes = (int) parsed.get("maxNumStrokes");
    final boolean includeTokenSize = (boolean) parsed.get("includeTokenSize");
    final boolean includeTokenWHRatio = (boolean) parsed.get("includeTokenWHRatio");
    final boolean includeTokenNumStrokes = (boolean) parsed.get("includeTokenNumStrokes");

    if (tokenDir == null || dataDir == null || engineDir == null) {
      parser.printHelp();
      System.exit(1);
    }

    System.out.println("=== Token engine training configuration: ===");
    System.out.println("tokenDir = " + tokenDir);
    System.out.println("dataDir = " + dataDir);
    System.out.println("engineDir = " + engineDir);
    System.out.println("newImageSize = " + newImageSize);
    System.out.println("generateIntermediateDataOnly = " + generateIntermediateDataOnly);

    System.out.println("maxIter = " + maxIter);
    System.out.println("hiddenLayerSize1 = " + hiddenLayerSize1);
    System.out.println("npPerStroke = " + npPerStroke);
    System.out.println("maxNumStrokes = " + maxNumStrokes);
    System.out.println("includeTokenSize = " + includeTokenSize);
    System.out.println("includeTokenWHRatio = " + includeTokenWHRatio);
    System.out.println("includeTokenNumStrokes = " + includeTokenNumStrokes);

    TrainTokenRecogEngineSDV trainer = new TrainTokenRecogEngineSDV(
        maxIter, hiddenLayerSize1, npPerStroke, maxNumStrokes,
        includeTokenSize, includeTokenWHRatio, includeTokenNumStrokes);
    trainer.train(newImageSize, tokenDir, dataDir, engineDir, generateIntermediateDataOnly);
  }
}
