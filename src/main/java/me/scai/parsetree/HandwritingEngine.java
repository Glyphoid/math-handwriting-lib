package me.scai.parsetree;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.scai.handwriting.CStroke;
import me.scai.handwriting.CWrittenTokenSet;
import me.scai.handwriting.StrokeCuratorUserAction;
import me.scai.parsetree.evaluation.PlatoVarMap;
import me.scai.parsetree.evaluation.ValueUnion;

public interface HandwritingEngine {
    /* Add stroke to the token set */
    public void addStroke(CStroke stroke)
        throws HandwritingEngineException;
    
    /* Remove the last token */
    public void removeLastToken()
        throws HandwritingEngineException;

    /* Remove i-th token */
    public void removeToken(int idxToken)
        throws HandwritingEngineException;

    /* Move a token
     * @returns  old bounds
     */
    public float[] moveToken(int tokenIdx, float [] newBounds)
        throws HandwritingEngineException;
    
    /* Merge strokes with specified indices as a single token */
    public void mergeStrokesAsToken(int [] strokeInds)
        throws HandwritingEngineException;
    
    /* Force set the recognition winner of a given token */
    public void forceSetRecogWinner(int tokenIdx, String tokenName)
        throws HandwritingEngineException;
    
    /* Clear all strokes */
    public void clearStrokes()
        throws HandwritingEngineException;
    
    /* Get the entire written token set */
    public CWrittenTokenSet getTokenSet();
    
    /* Get the constituent strokes of tokens, respectively */
    public List<int []> getTokenConstStrokeIndices();
    
    /* Perform parsing on the entire token set */
    public TokenSetParserOutput parseTokenSet()
        throws HandwritingEngineException;

    /* Perform parsing on selected tokens, causing the creation of a NodeToken (if successful) */
    public TokenSetParserOutput parseTokenSet(int[] tokenIndices)
            throws HandwritingEngineException;

    /* Get the graphical production set */
    public JsonArray getGraphicalProductions();

    /* Get the bounds of a token */
    public float[] getTokenBounds(int tokenIdx)
        throws HandwritingEngineException;

    /* Get the currently defined items */
    public PlatoVarMap getVarMap()
        throws HandwritingEngineException;

    /* Get the currently defined item of the specified key */
    public ValueUnion getFromVarMap(String varName)
        throws HandwritingEngineException;

    /* Inject state data */
    public void injectState(JsonObject stateData)
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
    public List<String> getAllTokenNames()
        throws HandwritingEngineException;


}

