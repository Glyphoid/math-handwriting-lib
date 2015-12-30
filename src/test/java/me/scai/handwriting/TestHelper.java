package me.scai.handwriting;

import me.scai.parsetree.*;
import me.scai.parsetree.evaluation.ParseTreeEvaluator;

import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TestHelper {
    public static final String TEST_ROOT_DIR          = "main";
    private static final String RESOURCES_DIR         = "resources";
    private static final String TERMINALS_FILE_NAME   = "terminals.json";
    private static final String PRODUCTIONS_FILE_NAME = "productions.txt";
    private static final String RESOURCES_CONFIG_DIR  = "config";

    public static class WorkerTuple {
        public GraphicalProductionSet gpSet;
        public TerminalSet termSet;
        public TokenSetParser tokenSetParser;
        public ParseTreeStringizer stringizer;
        public ParseTreeEvaluator evaluator;
        public ParseTreeMathTexifier mathTexifier;

        public WorkerTuple() {}
    }

    public static WorkerTuple getTestWorkerTuple() {
        final URL prodSetFN = Thread.currentThread().getContextClassLoader().getResource(File.separator + TEST_ROOT_DIR +
                File.separator + RESOURCES_DIR +
                File.separator + RESOURCES_CONFIG_DIR +
                File.separator + PRODUCTIONS_FILE_NAME);
        final URL termSetFN = Thread.currentThread().getContextClassLoader().getResource(File.separator + TEST_ROOT_DIR +
                File.separator + RESOURCES_DIR +
                File.separator + RESOURCES_CONFIG_DIR +
                File.separator + TERMINALS_FILE_NAME);

        WorkerTuple workerTuple = new WorkerTuple();

        try {
            workerTuple.termSet = TerminalSet.createFromJsonAtUrl(termSetFN);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.flush();
        }

        try {
            workerTuple.gpSet = GraphicalProductionSet.createFromUrl(prodSetFN, workerTuple.termSet);
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

        workerTuple.tokenSetParser = new TokenSetParser(workerTuple.termSet, workerTuple.gpSet, 0.90f);
        workerTuple.stringizer = workerTuple.gpSet.genStringizer();
        workerTuple.evaluator = workerTuple.gpSet.genEvaluator();
        workerTuple.mathTexifier = new ParseTreeMathTexifier(workerTuple.gpSet, workerTuple.termSet);

        return workerTuple;
    }


    public static List<String> getGraphicalProductionSumStrings(GraphicalProductionSet gpSet, List<Integer> indices) {
        Iterator<Integer> iter = indices.iterator();

        List<String> r = new LinkedList<>();

        while (iter.hasNext()) {
            r.add(gpSet.prodSumStrings.get(iter.next()));
        }

        return r;
    }

    public static CWrittenToken getMockWrittenToken(float[] bounds, String recogResult) {
        CWrittenToken writtenToken = new CWrittenToken();

        writtenToken.bNormalized = true;
        writtenToken.setBounds(bounds);
        writtenToken.setRecogResult(recogResult);

        return writtenToken;
    }

    public static CWrittenTokenSetNoStroke getMockTokenSet(float[][] boundsArray, String[] tokenNames) {
        int nTokens = boundsArray.length;
        if (tokenNames.length != nTokens) {
            throw new IllegalArgumentException("Length mismatch between bounds array and token names array");
        }

        AbstractToken[] writtenTokens = new AbstractToken[nTokens];

        // Token "1"
        for (int i = 0; i < nTokens; ++i) {
            float[] bounds = boundsArray[i];
            if (bounds == null || bounds.length != 4) {
                throw new IllegalArgumentException("Illegal bounds at index " + i);
            }

            CWrittenToken newToken = new CWrittenToken();

            newToken.setBounds(bounds);
            newToken.setRecogResult(tokenNames[i]);
            newToken.bNormalized = true;

            writtenTokens[i] = newToken;

        }

        return CWrittenTokenSetNoStroke.from(writtenTokens);
    }

    public static TokenRecogEngine readTokenEngine() throws IOException, ClassNotFoundException{

        final String RESOURCES_TOKEN_ENGINE_DIR = "token_engine";
        final String TOKEN_ENGINE_FILE_NAME = "token_engine.sdv.sz0_whr1_ns1.ser";

        URL tokenEngineFileUrl = TestHelper.class.getClassLoader().getResource(File.separator + TEST_ROOT_DIR +
                File.separator + RESOURCES_DIR +
                File.separator + RESOURCES_TOKEN_ENGINE_DIR +
                File.separator + TOKEN_ENGINE_FILE_NAME);

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

    public static CStroke getMockStroke(float[] xs, float[] ys) {
        assert(xs.length == ys.length);
        assert(xs.length > 0);

        CStroke stroke = new CStroke(xs[0], ys[0]);

        for (int i = 1; i < xs.length; ++i) {
            stroke.addPoint(xs[i], ys[i]);
        }

        return stroke;
    }
}
