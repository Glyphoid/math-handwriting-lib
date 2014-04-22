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
		
		/* Make sure that t_recogWinner belongs to the set tokenNames */
		if ( !Arrays.asList(tokenNames).contains(t_recogWinner) ) {
			System.err.println("Value of t_recogWinner (" + t_recogWinner + ") does not belong to tokenNames");
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
	void readFromFile(String fileName) throws FileNotFoundException, IOException {
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
				
				addToken(bnds, t_recogWinner, t_recogPs);
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
	
}
