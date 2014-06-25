package me.scai.handwriting;

import java.util.ArrayList;
import java.lang.IllegalStateException;

public class CWrittenTokenSet extends CAbstractWrittenTokenSet {
	public ArrayList<String> recogWinners = new ArrayList<String>();
	public ArrayList<double []> recogPs = new ArrayList<double []>();
	
	public ArrayList<CWrittenToken> tokens = new ArrayList<CWrittenToken>();
	
	/* Constructor */
	public CWrittenTokenSet() {
	}
	
	/* Add a token at the end: without any recognition results */
	public void addToken(CWrittenToken wt) {
		/* TODO: Merge into addToken(int i, CWrittenToken wt) */
		if ( !wt.bNormalized )
			wt.normalizeAxes();
		
		float [] bounds = wt.getBounds();
		if ( min_x > bounds[0] ) 
			min_x = bounds[0];
		if ( min_y > bounds[1] ) 
			min_y = bounds[1];
		if ( max_x < bounds[2] ) 
			max_x = bounds[2];
		if ( max_y < bounds[3] ) 
			max_y = bounds[3];
		
		tokens.add(wt);
		addOneToken();
	}
	
	/* Add a token at specified location: without any recognition results */
	public void addToken(int i, CWrittenToken wt) {
		if ( !wt.bNormalized )
			wt.normalizeAxes();
		
		float [] bounds = wt.getBounds();
		if ( min_x > bounds[0] )
			min_x = bounds[0];
		if ( min_y > bounds[1] ) 
			min_y = bounds[1];
		if ( max_x < bounds[2] ) 
			max_x = bounds[2];
		if ( max_y < bounds[3] ) 
			max_y = bounds[3];
		
		tokens.add(i, wt);
		addOneToken();
	}
	
	/* Add a token at the end: with recognition results */
	public void addToken(CWrittenToken wt, String t_recogWinner, double [] t_recogP) {
		addToken(wt);
		addTokenRecogRes(t_recogWinner, t_recogP);
	}
	
	/* Add a token at specified location: with recognition results */
	public void addToken(int i, CWrittenToken wt, String t_recogWinner, double [] t_recogP) {
		addToken(i, wt);
		addTokenRecogRes(i, t_recogWinner, t_recogP);
	}
	
	/* Add recognition results for a token, at the end:
	 * including the winner of the recognition and the detailed p-values.
	 */
	public void addTokenRecogRes(String t_recogWinner, double [] t_recogP) {
		recogWinners.add(t_recogWinner);
		recogPs.add(t_recogP);
	}
	
	/* Add recognition results for a token, at the specified location: 
	 * including the winner of the recognition and the detailed p-values.
	 */
	public void addTokenRecogRes(int i, String t_recogWinner, double [] t_recogP) {
		recogWinners.add(i, t_recogWinner);
		recogPs.add(i, t_recogP);
	}
	
	
	/* (Re-)calculate the bounds */
	@Override
	protected void calcBounds() {
		min_x = min_y = Float.MAX_VALUE;
		max_x = max_y = Float.MAX_VALUE;
		
		for (int i = 0; i < tokens.size(); ++i) {
			float [] bounds = tokens.get(i).getBounds();
			
			if ( min_x > bounds[0] )
				min_x = bounds[0];
			if ( min_y > bounds[1] ) 
				min_y = bounds[1];
			if ( max_x < bounds[2] ) 
				max_x = bounds[2];
			if ( max_y < bounds[3] ) 
				max_y = bounds[3];
		}
	}
	
	/* Delete a token */
	public void deleteToken(int i) {
		if ( i < 0 ) {
			System.err.println("Deletion index is negative");
			return;
		}
		
		if ( i >= nTokens() ) {
			System.err.println("Deletion index exceeds number of tokens");
			return;
		}
		
		tokens.remove(i);
		recogWinners.remove(i);
		recogPs.remove(i);
		
		calcBounds();
		
		deleteOneToken();
	}
	
//	/* Clear all tokens */
//	public void clearToken() {
//		tokens.clear();
//		
//		recogWinners.clear();
//		recogPs.clear();
//		
//		min_x = min_y = Float.MAX_VALUE;
//		max_x = max_y = Float.MIN_VALUE;
//		
//		nt = 0;
//	}
	
	@Override
	public void clear() {
		tokens.clear();
		
		recogWinners.clear();
		recogPs.clear();
		
		min_x = min_y = Float.MAX_VALUE;
		max_x = max_y = Float.MIN_VALUE;
		
		nt = 0;
	}
	
	/* Get descriptive string: brief format, without the detailed stroke data */
	@Override
	public String getStringBrief() {
		/* Check if tokeNames has been configured */
		if ( tokenNames == null )
			throw new IllegalStateException("tokenNames have not been configured yet");
	
		if ( tokens.size() != recogWinners.size() || 
		     tokens.size() != recogPs.size() )
			throw new IllegalStateException("Difference in sizes of tokens and recognition results");
		
		String str = "";
		
		/* Write token names */
		str += "Token set: [";
		for (int k = 0; k < tokenNames.length; ++k) {
			str += tokenNames[k];
			if ( k < tokenNames.length - 1 )
				str += ", ";
		}
		str += "]\n";
		str += "\n";
		
		/* Write tokens */
		for (int k = 0; k < nt; ++k) {
			/* Bound */
			str += "bounds = [";			
			float [] bnds = tokens.get(k).getBounds();
			str += bnds[0] + ", ";
			str += bnds[1] + ", ";
			str += bnds[2] + ", ";
			str += bnds[3];
			str += "]\n";
						
			/* Recognition winner */
			str += "recogWinner = " + recogWinners.get(k) + "\n";
			
			/* Recognition Ps */
			str += "recogPs = [";
			for (int n = 0; n < recogPs.get(k).length; ++n) {
				str += recogPs.get(k)[n];
				if ( n < recogPs.get(k).length - 1 )
					str += ", ";
			}
			str += "]\n";
			str += "\n";
		}
		
		
		return str;
	}

	@Override
	public float [] getTokenBounds(int i) {
		return tokens.get(i).getBounds();
	}
	
	@Override 
	public float[] getTokenBounds(int [] is) {
		float [] bnds = new float[4]; /* min_x, min_y, max_x, max_y */
		bnds[0] = bnds[1] = Float.POSITIVE_INFINITY;
		bnds[2] = bnds[3] = Float.NEGATIVE_INFINITY;
		
		for (int i = 0; i < is.length; ++i) {
			float [] t_bnds = getTokenBounds(is[i]);
			
			if ( t_bnds[0] < bnds[0] ) /* min_x */
				bnds[0] = t_bnds[0];
			if ( t_bnds[1] < bnds[1] ) /* min_y */
				bnds[1] = t_bnds[1];
			
			if ( t_bnds[2] > bnds[2] ) /* max_x */
				bnds[2] = t_bnds[2];
			if ( t_bnds[3] > bnds[3] ) /* max_y */
				bnds[3] = t_bnds[3];
		}
		
		return bnds;
	}
	
	
}
