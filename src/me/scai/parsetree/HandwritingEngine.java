package me.scai.parsetree;

import java.util.List;

import me.scai.handwriting.CStroke;
import me.scai.handwriting.CWrittenTokenSet;
import com.google.gson.JsonObject;

public interface HandwritingEngine {
    /* Add stroke to the token set */
    public void addStroke(CStroke stroke);
    
    /* Remove the last token */
    public void removeLastToken();
    
    /* Merge strokes with specified indices as a single token */
    public void mergeStrokesAsToken(int [] strokeInds);
    
    /* Force set the recognition winner of a given token */
    public void forceSetRecogWinner(int tokenIdx, String tokenName);
    
    /* Clear all strokes */
    public void clearStrokes();
    
    /* Get the entire written token set */
    public CWrittenTokenSet getTokenSet();
    
    /* Get the constituent strokes of tokens, respectively */
    public List<int []> getTokenConstStrokeIndices();
    
    /* Perform token set parsing */
    public JsonObject parseTokenSet();
}
