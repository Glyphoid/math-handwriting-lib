package me.scai.handwriting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.scai.parsetree.TextHelper;

import java.io.*;
import java.net.URL;
import java.util.Properties;

public class Train_TokenRecogEngineSDV {
    private static final String TEST_ROOT_DIR = "test";
    private static final String RESOURCES_DIR = "resources";
    private static final String RESOURCES_CONFIG_DIR = "config";

    /* Training routine */
    public static void main(String[] args) {
		/* Token engine settings */
        final boolean bLoadEngineFromDisk = false; /* false: train; true: test */

        final int hiddenLayerSize1 = 185; /* Orig: 100 --> 140 */
        final int hiddenLayerSize2 = 0;
        final int trainMaxIter = 400;
        final double trainThreshErr = 0.001;

        final int t_npPerStroke = 16;
        final int t_maxNumStrokes = 4;

        final boolean bIncludeTokenSize = false;
        final boolean bIncludeTokenWHRatio = true;
        final boolean bIncludeTokenNumStrokes = true;

        Properties props = new Properties();
//        URL url = TokenRecogEngineSDV.class.getResource("/resources/handwriting.properties");
        URL url = Thread.currentThread().getContextClassLoader().getResource(File.separator + TEST_ROOT_DIR  +
                File.separator + RESOURCES_DIR + File.separator + "handwriting.properties");

        try {
            props.load(url.openStream());
        } catch (Exception exc) {
            System.err.println("Failed to load property file: " + exc.getMessage());
        }

        final String letterDir = props.getProperty("letterDir");
        final String tokenEngineSerFN = props.getProperty("tokenEngineSerFN");
		/* ~Token engine settings */

		/* TODO: Make both TokenRecogEngine and TokenRecogEngineSDV inherit from a
		 * common parent class.
		 */
        if ( !bLoadEngineFromDisk ) {
            TokenRecogEngineSDV tokEngine = new TokenRecogEngineSDV(hiddenLayerSize1,
                    hiddenLayerSize2,
                    trainMaxIter,
                    trainThreshErr);

			/* Load terminal set and token degeneracy */
            final String TERMINAL_CONFIG_FN = "terminals.json";
            final URL terminalConfigFN =  Thread.currentThread().getContextClassLoader().getResource(
                    File.separator + TEST_ROOT_DIR +
                    File.separator + RESOURCES_DIR +
                    File.separator + RESOURCES_CONFIG_DIR +
                    File.separator + TERMINAL_CONFIG_FN);

            String lines;
            try {
                lines = TextHelper.readTextFileAtUrl(terminalConfigFN);
            }
            catch ( Exception e ) {
                throw new RuntimeException("Failed to read token degeneracy data from URL: \"" + terminalConfigFN + "\"");
            }

            JsonObject tokenDegenObj = new JsonParser().parse(lines).getAsJsonObject().get("tokenDegeneracy").getAsJsonObject();
            tokEngine.loadTokenDegeneracy(tokenDegenObj); /* TODO: Make more elegant */
			/* ~Load token degeneracy */

            String [] hardCodedTokens = {"."};
            tokEngine.setHardCodedTokens(hardCodedTokens);

            System.out.println("New instance of tokEngine created.");

            int [] ivs = new int[2];
            ivs[0] = t_npPerStroke;
            ivs[1] = t_maxNumStrokes;

            boolean [] bvs = new boolean[3];
            bvs[0] = bIncludeTokenSize;
            bvs[1] = bIncludeTokenWHRatio;
            bvs[2] = bIncludeTokenNumStrokes;

            tokEngine.setFeatures(ivs, bvs);
            tokEngine.train(letterDir);

			/* Test run on all the previous data */
			/* Get the list of all .wt files */
            float errRate = tokEngine.getTestErrRate();
            System.out.println("Final test error rate = " + errRate);

			/* Serialize to file */
            ObjectOutputStream objOutStream = null;
            try {
                objOutStream = new ObjectOutputStream(new FileOutputStream(tokenEngineSerFN));
                objOutStream.writeObject(tokEngine);
                objOutStream.flush();

            }
            catch (IOException e) {
                System.err.println("WRITE_TOKEN_ENGINE_ERROR: Failed to write token engine to file: " + tokenEngineSerFN);
            }
            finally {
                try {
                    objOutStream.close();
                }
                catch (IOException e) {
                    System.err.println("TOKEN_ENGINE_OUTPUT_STREAM_CLOSE_ERR: Failed to close token engine output stream");
                }
            }

            System.out.println("Wrote token engine to file \"" + tokenEngineSerFN + "\"");
        }
        else {
            ObjectInputStream objInStream = null;
            TokenRecogEngine tokEngine = null;
            try {
                objInStream = new ObjectInputStream(new FileInputStream(tokenEngineSerFN));
                tokEngine = (TokenRecogEngineSDV) objInStream.readObject();
				/* TODO: Add option of TokenRecogEngineSDV */

                //objInStream.close();
            }
            catch (IOException e) {
                System.err.println("READ_TOKEN_ENGINE_ERROR: Failed to read token engine from file: "
                        + tokenEngineSerFN);
            }
            catch (ClassNotFoundException e) {
                System.err.println("READ_TOKEN_ENGINE_ERROR: Failed to read token engine from file: "
                        + tokenEngineSerFN);
            }
            finally {
                try {
                    objInStream.close();
                }
                catch (IOException e) {
                    System.err.println("TOKEN_ENGINE_INPUT_STREAM_CLOSE_ERR: Failed to close token engine input stream");
                }
            }

            CWrittenToken wt = new CWrittenToken();

            //String data_dir = "C:\\Users\\systemxp\\Documents\\Dropbox\\Plato\\data\\letters\\";
            //String token_num = "1099";
            String data_dir = "C:\\Users\\systemxp\\Documents\\Dropbox\\Plato\\data\\letters\\new2\\";
            String token_num = "365";
            String wt_fn = data_dir + tokEngine.wt_file_prefix + token_num + tokEngine.wt_file_suffix;
            String im_fn = data_dir + tokEngine.im_file_prefix + token_num + tokEngine.im_file_suffix;

            File wt_f = new File(wt_fn);
            File im_f = new File(im_fn);

            CHandWritingTokenImageData t_imData = null;
            try {
                wt.readFromFile(wt_f);
                t_imData =
                        CHandWritingTokenImageData.readImFile(im_f,
                                bIncludeTokenSize,
                                bIncludeTokenWHRatio,
                                bIncludeTokenNumStrokes);
                wt.width = t_imData.w;
                wt.height = t_imData.h;
            }
            catch (IOException ioe) {
                throw new RuntimeException();
            }

            double [] ps = new double[tokEngine.tokenNames.size()];
            int recogIdx = tokEngine.recognize(wt, ps);
            String recogToken = tokEngine.getTokenName(recogIdx);

            System.out.println(wt_fn + ": true label = \"" + t_imData.tokenName + "\" recognized as \"" + recogToken + "\"");
        }

    }
}
