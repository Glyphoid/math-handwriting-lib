package me.scai.parsetree;

import java.util.List;

import me.scai.handwriting.CStroke;
import me.scai.handwriting.CWrittenTokenSet;
import com.google.gson.JsonObject;

public interface HandwritingEngine {
    /* Add stroke to the token set */
    public void addStroke(CStroke stroke)
        throws HandwritingEngineException;
    
    /* Remove the last token */
    public void removeLastToken()
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
    
    /* Perform token set parsing */
    public TokenSetParserOutput parseTokenSet()
        throws HandwritingEngineException;

    /* Get the bounds of a token */
    public float[] getTokenBounds(int tokenIdx)
        throws HandwritingEngineException;
}
