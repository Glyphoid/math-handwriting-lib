package me.scai.handwriting;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.AssertionError;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class CWrittenTokenSetNoStroke extends CAbstractWrittenTokenSet {
	/* Member variables */
	public ArrayList<CWrittenToken> tokens = new ArrayList<CWrittenToken>();
	/* ~Member variables */
	
//	public ArrayList<float []> tokenBounds = new ArrayList<float []>();
	
	/* ************ Methods ************ */
	/* Default constructor */
	public CWrittenTokenSetNoStroke() { }
	
	/* Constructor: taking a CAbstarctWrittenTokenSet, extract a subset of the
	 * tokens and used them to form a new CWrittenTokenSetNoStroke. 
	 * Information about strokes is discarded in this construction process.
	 */
	public CWrittenTokenSetNoStroke(CWrittenTokenSetNoStroke owts, int [] indices) {
		setTokenNames(owts.tokenNames);
		
		for ( int i = 0; i < indices.length; ++i )
			addToken(owts.tokens.get(indices[i]));
		
		calcBounds();
	}
	
//	public CWrittenTokenSetNoStroke(CAbstractWrittenTokenSet owts, int [] indices) {
//		setTokenNames(owts.tokenNames);
//		
//		for ( int i = 0; i < indices.length; ++i ) {
//			float [] t_bnds = owts.getTokenBounds(indices[i]);
//			addToken(t_bnds, owts.recogWinners.get(indices[i]), owts.recogPs.get(indices[i]));
//		}
//		
//		calcBounds();
//	}
	
	public void addToken(CWrittenToken wt) {
		tokens.add(wt);
		addOneToken();
	}

//	public void addToken(float [] bounds, String t_recogWinner, double [] t_recogP) {
//		addToken(bounds, t_recogWinner, t_recogP, true);
//	}
	
//	public void addToken(float [] bounds, String t_recogWinner, double [] t_recogP, boolean bCheck) {
//		/* Input sanity checks */
//		if ( bounds.length != 4 ) {
//			System.err.println("Input bounds is not a length-4 float array");
//			return;
//		}
//		
//		if ( bCheck ) {
//			if ( tokenNames.length != t_recogP.length ) {
//				System.err.println("Input t_recogP doesn't have the same length as tokenNames");
//				return;
//			}
//			
//			/* Make sure that t_recogWinner belongs to the set tokenNames */
//			if ( !Arrays.asList(tokenNames).contains(t_recogWinner) ) {
//				System.err.println("Value of t_recogWinner (" + t_recogWinner + ") does not belong to tokenNames");
//				return;
//			}
//		}
//		
//		tokenBounds.add(bounds);
//		
//		if ( t_recogWinner != null )
//			recogWinners.add(t_recogWinner);
//		
//		if ( t_recogP != null )
//			recogPs.add(t_recogP);
//
//		addOneToken(); /* Takes care of things including incrementing nt */
//	}
		
	@Override
	public void calcBounds() {
		min_x = min_y = Float.MAX_VALUE;
		max_x = max_y = Float.MIN_VALUE;
		
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

	@Override
	public String getStringBrief() {
		/* Check if tokeNames has been configured */
//		if ( tokenBounds == null )
//			throw new IllegalStateException("tokenNames have not been configured yet");
//	
//		if ( tokenBounds.size() != recogWinners.size() || 
//		     tokenBounds.size() != recogPs.size() )
//			throw new IllegalStateException("Difference in sizes of tokens and recognition results");
		
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
//			float [] bnds = tokenBounds.get(k);
			float [] bnds = tokens.get(k).getBounds();
			str += bnds[0] + ", ";
			str += bnds[1] + ", ";
			str += bnds[2] + ", ";
			str += bnds[3];
			str += "]\n";
						
			/* Recognition winner */
//			str += "recogWinner = " + recogWinners.get(k) + "\n";
			str += "recogWinner = " + tokens.get(k).getRecogWinner() + "\n";
			
			/* Recognition Ps */
			str += "recogPs = [";
//			for (int n = 0; n < recogPs.get(k).length; ++n) {
//				str += recogPs.get(k)[n];
//				if ( n < recogPs.get(k).length - 1 )
//					str += ", ";
//			}
			for (int n = 0; n < tokens.get(k).getRecogPs().length; ++n) {
				str += tokens.get(k).getRecogPs()[n];
				if ( n < tokens.get(k).getRecogPs().length - 1 )
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
		
		tokens.remove(i);
//		tokenBounds.remove(i);
//		recogWinners.remove(i);
//		recogPs.remove(i);
		
		calcBounds();
		
		deleteOneToken();
	}
	
	/* Read from .wts file */
	public void readFromFile(String fileName) throws FileNotFoundException, IOException {
		clear();
		
		File wtsFile = new File(fileName);
		if ( !wtsFile.isFile() )
			throw new FileNotFoundException("Cannot find file for reading: " + fileName);
		
		FileInputStream fin = null;
		BufferedReader in = null;
		try {			
			fin = new FileInputStream(wtsFile);
			in = new BufferedReader(new InputStreamReader(fin));
		}
		catch ( IOException e ) {
			throw new IOException("IOException during reading of .wts file: " + fileName);
		}
		
		String line;
		String leadStr, trailStr, sepStr;		
		try {									
			/* Read token names */
			leadStr = "Token set: [";
			trailStr = "]";
			sepStr = ", ";
			
			line = in.readLine();
			assert(line.startsWith(leadStr));
			assert(line.endsWith(trailStr));
			line = line.substring(leadStr.length(), line.length() - trailStr.length());
			
			setTokenNames(line.split(sepStr));
			in.readLine();
			
			/* Read tokens with no strokes, one by one */
			while ( true ) {
				line = in.readLine();
				
				if ( line == null || line.length() == 0 )
					break;
				
				/* Bounds */
				leadStr = "bounds = [";
				trailStr = "]";
				assert(line.startsWith(leadStr));
				assert(line.endsWith(trailStr));
				line = line.substring(leadStr.length(), line.length() - trailStr.length());
				
				String [] bndsStr = line.split(sepStr);
				assert(bndsStr.length == 4);
				float [] bnds = new float[4];
				for (int i = 0; i < 4; ++i)
					bnds[i] = Float.parseFloat(bndsStr[i]);
				
				/* recogWinner */
				line = in.readLine();
				leadStr = "recogWinner = ";
				trailStr = "";
				assert(line.startsWith(leadStr));
				assert(line.endsWith(trailStr));				
				String t_recogWinner = line.substring(leadStr.length(), line.length() - trailStr.length());
				
				/* recogPs */
				line = in.readLine();
				leadStr = "recogPs = [";
				trailStr = "]";
				assert(line.startsWith(leadStr));
				assert(line.endsWith(trailStr));
				line = line.substring(leadStr.length(), line.length() - trailStr.length());
				
				String [] psStr = line.split(sepStr);
				assert(psStr.length == tokenNames.length);
				double [] t_recogPs = new double[psStr.length];
				for (int i = 0; i < psStr.length; ++i)
					t_recogPs[i] = Double.parseDouble(psStr[i]);
				
				line = in.readLine();
							
//				addToken(bnds, t_recogWinner, t_recogPs);
				CWrittenToken wt = new CWrittenToken(bnds, t_recogWinner, t_recogPs);
				wt.bNormalized = true;
				addToken(wt);
			}
			
		}
		catch ( AssertionError ae ) {
			throw new IOException("Unexpected format in input .wts file: " + fileName);
		}
		catch ( IOException ioe ) {
			throw new IOException("IOException occurred during reading input .wts file: " + fileName);
		}
		finally {
			in.close();
		}
		
		calcBounds();
	}
	
	/* Testing routine */
	public static void main(String [] args) {
		/* Read .wts file */
		final String wtsFileName = "C:\\Users\\systemxp\\Documents\\My Dropbox\\Plato\\data\\tokensets\\TS_1.wts";
		CWrittenTokenSetNoStroke wt = new CWrittenTokenSetNoStroke();
		
		try {
			wt.readFromFile(wtsFileName);
		}
		catch ( FileNotFoundException fnfe ) {
			System.err.println(fnfe.getMessage());
		}
		catch ( IOException ioe ) {
			System.err.println(ioe.getMessage());
		}
		
		System.out.println("Done reading wts file: " + wtsFileName);
	}
	
	@Override
	public void clear() {
//		tokenBounds.clear();
		
//		recogWinners.clear();
//		recogPs.clear();
		tokens.clear();
		
		min_x = min_y = Float.MAX_VALUE;
		max_x = max_y = Float.MIN_VALUE;
		
		nt = 0;
	}

	@Override
	public float[] getTokenBounds(int i) {
//		return tokenBounds.get(i);
		return tokens.get(i).getBounds();
	}
	
	@Override 
	public float[] getTokenBounds(int [] is) {
		float [] bnds = new float[4]; /* min_x, min_y, max_x, max_y */
		bnds[0] = bnds[1] = Float.POSITIVE_INFINITY;
		bnds[2] = bnds[3] = Float.NEGATIVE_INFINITY;
		
		for (int i = 0; i < is.length; ++i) {
			float [] t_bnds = getTokenBounds(is[i]);
			
			if ( t_bnds[0] < bnds[0] )	/* min_x */
				bnds[0] = t_bnds[0];
			if ( t_bnds[1] < bnds[1] )	/* min_y */
				bnds[1] = t_bnds[1];
			
			if ( t_bnds[2] > bnds[2] )	/* max_x */
				bnds[2] = t_bnds[2];
			if ( t_bnds[3] > bnds[3] )	/* max_y */
				bnds[3] = t_bnds[3];
		}
		
		return bnds;
	}

	@Override
	public String toString() {
		String s = "Token [";
		
//		for (int i = 0; i < recogWinners.size(); ++i) {
//			s += recogWinners.get(i);
//			s += "-(" + String.format("%.1f",  min_x) + ", "
//			          + String.format("%.1f",  min_y) + ", "
//					  + String.format("%.1f",  max_x) + ", "
//			          + String.format("%.1f",  max_y) + ")";
//			
//			if ( i < recogWinners.size() - 1 )
//				s += ", ";
//		}
		for (int i = 0; i < tokens.size(); ++i) {
			s += tokens.get(i).getRecogWinner();
			s += "-(" + String.format("%.1f",  min_x) + ", "
			          + String.format("%.1f",  min_y) + ", "
					  + String.format("%.1f",  max_x) + ", "
			          + String.format("%.1f",  max_y) + ")";
			
			if ( i < tokens.size() - 1 )
				s += ", ";
		}
		s += "]";
		
		return s;
	}
	
}
