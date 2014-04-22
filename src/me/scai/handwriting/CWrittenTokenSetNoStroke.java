package me.scai.handwriting;

import java.util.ArrayList;

public class CWrittenTokenSetNoStroke extends CAbstractWrittenTokenSet {
	public ArrayList<float []> tokenBounds = new ArrayList<float []>();
	
	public void addToken(float [] bounds, String t_recogWinner, double [] t_recogP) {
		/* Input sanity checks */
		if ( bounds.length != 4 ) {
			System.err.println("Input bounds is not a length-4 float array");
			return;
		}
		
		if ( tokenNames.length != t_recogP.length ) {
			System.err.println("Input t_recogP doesn't have the same length as tokenNames");
			return;
		}
		
		tokenBounds.add(bounds);
		recogWinners.add(t_recogWinner);
		recogPs.add(t_recogP);
				
		addOneToken(); /* Takes care of things including incrementing nt */
	}
	
	@Override
	protected void calcBounds() {
		min_x = min_y = Float.MAX_VALUE;
		max_x = max_y = Float.MAX_VALUE;
		
		for (int i = 0; i < tokenBounds.size(); ++i) {
			float [] bounds = tokenBounds.get(i);
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

	@Override
	public String getStringBrief() {
		/* Check if tokeNames has been configured */
		if ( tokenBounds == null )
			throw new IllegalStateException("tokenNames have not been configured yet");
	
		if ( tokenBounds.size() != recogWinners.size() || 
		     tokenBounds.size() != recogPs.size() )
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
			float [] bnds = tokenBounds.get(k);
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


	public void deleteToken(int i) {
		if ( i < 0 ) {
			System.err.println("Deletion index is negative");
			return;
		}
		
		if ( i >= nTokens() ) {
			System.err.println("Deletion index exceeds number of tokens");
			return;
		}
		
		tokenBounds.remove(i);
		recogWinners.remove(i);
		recogPs.remove(i);
		
		calcBounds();
		
		deleteOneToken();
	}
	
	/* Read from .wts file */
	void readFromFile(String fileName) throws IOException {
		
	}
	
}
