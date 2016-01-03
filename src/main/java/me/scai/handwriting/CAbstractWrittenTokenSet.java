package me.scai.handwriting;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public abstract class CAbstractWrittenTokenSet {
    /* Member variables */
	protected int nt = 0; /* Number of tokens */
	protected String [] tokenNames = null;
	
	/* Geometric bounds of the token set */
	protected float min_x = Float.POSITIVE_INFINITY;
	protected float min_y = Float.POSITIVE_INFINITY;
	protected float max_x = Float.NEGATIVE_INFINITY;
	protected float max_y = Float.NEGATIVE_INFINITY;

    /* Constructor */
    public CAbstractWrittenTokenSet() {} // For deserialization
	
	/* Get the number of tokens */
	public int nTokens() {
		return nt;
	}
	
	/* Test if the set is empty */
	public boolean empty() {
		return (nt == 0);
	}
	
	/* Add a new token */
	public void addOneToken() {
		nt++;
	}
	
	/* Set char set: the set of possible token names.
	 * For example, these can be used in conjunction with recogPs. */
	public void setTokenNames(String [] t_tokenNames) {
		tokenNames = t_tokenNames;
	}
	
	public String [] getTokenNames() {
		return tokenNames;
	}
	
	public String getTokenName(int i) {
        if (tokenNames == null) {
            return null;
        } else {
            /* TODO: bound check */
            return tokenNames[i];
        }
	}

	public void deleteOneToken() {
		nt--;
	}
	
	public float [] getSetBounds() {
		float [] bnds = new float[4];
		
		bnds[0] = min_x;
		bnds[1] = min_y;
		bnds[2] = max_x;
		bnds[3] = max_y;
		
		return bnds;
	}
	
	public int getNumTokens() {
		return nt;
	}
	
	/* *** Abstract methods *** */
	protected abstract void calcBounds();
	protected abstract void clear();
	
	public abstract float [] getTokenBounds(int i);
	public abstract float [] getTokenBounds(int [] is);

	public abstract List<String> getTokenTermTypes(int i);

    public abstract float [] setTokenBounds(int i, final float [] newBounds);
	
}


