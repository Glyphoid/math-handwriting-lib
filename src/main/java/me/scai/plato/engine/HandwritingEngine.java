package me.scai.plato.engine;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.scai.handwriting.*;
import me.scai.parsetree.HandwritingEngineException;
import me.scai.parsetree.TokenSetParserOutput;
import me.scai.parsetree.evaluation.PlatoVarMap;
import me.scai.parsetree.evaluation.ValueUnion;

public interface HandwritingEngine {
    /* Add stroke to the token set */
    void addStroke(CStroke stroke)
        throws HandwritingEngineException;
    
    /* Remove the last token */
    void removeLastToken()
        throws HandwritingEngineException;

    /* Remove i-th token */
    void removeToken(int idxToken)
        throws HandwritingEngineException;

    /* Move a token
     * @param tokenIdx  Index to the abstract token (not written token) to be moved
     * @param newBounds Length-4 float array to describe the new bounds
     * @returns  old bounds
     */
    float[] moveToken(int tokenIdx, float [] newBounds)
        throws HandwritingEngineException;
    
    /* Merge strokes with specified indices as a single token */
    void mergeStrokesAsToken(int [] strokeInds)
        throws HandwritingEngineException;
    
    /* Force set the recognition winner of a given token */
    void forceSetRecogWinner(int tokenIdx, String tokenName)
        throws HandwritingEngineException;
    
    /* Clear all strokes */
    void clearStrokes()
        throws HandwritingEngineException;
    
    /* Get the abstract token set: Could contain node tokens */
    CAbstractWrittenTokenSet getTokenSet();

    /* Get the entire written token set.: Never contain node tokens. All are the base-level written tokens */
    CWrittenTokenSet getWrittenTokenSet();

    /* Get the constituent strokes of tokens, respectively */
    List<int []> getTokenConstStrokeIndices();
    
    /* Perform parsing on the entire token set */
    TokenSetParserOutput parseTokenSet()
        throws HandwritingEngineException;

    /* Perform parsing on selected tokens, causing the creation of a NodeToken (if successful) */
    TokenSetParserOutput parseTokenSet(int[] tokenIndices)
            throws HandwritingEngineException;

    /* Get the graphical production set */
    JsonArray getGraphicalProductions();

    /* Get the bounds of tokens: abstract tokens (There could be NodeTokens) */
    float[] getTokenBounds(int tokenIdx)
            throws HandwritingEngineException;

    /* Get the bounds of tokens: basic written tokens (There will never be NodeTokens) */
    float[] getWrittenTokenBounds(int tokenIdx)
        throws HandwritingEngineException;

    /* Get the currently defined items */
    PlatoVarMap getVarMap()
        throws HandwritingEngineException;

    /* Get the currently defined item of the specified key */
    ValueUnion getFromVarMap(String varName)
        throws HandwritingEngineException;

    /* Inject state data */
//    void extractStateData()
//        throws

    void injectState(JsonObject stateData)
        throws HandwritingEngineException;

    /* Undo and redo */
    StrokeCuratorUserAction getLastStrokeCuratorUserAction();
    void undoStrokeCuratorUserAction();
    void redoStrokeCuratorUserAction();

    boolean canUndoStrokeCuratorUserAction();
    boolean canRedoStrokeCuratorUserAction();

    public void removeEngine()
        throws HandwritingEngineException;

    /**
     * @return All possible toke names (Not their display names)
     */
    List<String> getAllTokenNames()
        throws HandwritingEngineException;

    /**
     * Obtain the UUIDs of the written tokens in the stroke curator
     * @return   UUIDs of the written tokens
     */
    List<String> getWrittenTokenUUIDs();

    /**
     * Obtain the UUIDs of the tokens that comprise each abstract token
     * @return List of UUID lists
     */
    List<List<String>> getConstituentWrittenTokenUUIDs();



}

