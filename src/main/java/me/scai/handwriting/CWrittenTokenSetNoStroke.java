package me.scai.handwriting;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.AssertionError;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Iterator;

import me.scai.parsetree.TerminalSet;

public class CWrittenTokenSetNoStroke extends CAbstractWrittenTokenSet {
	/* Member variables */
	public ArrayList<AbstractToken> tokens = new ArrayList<>();
	public ArrayList<Integer> tokenIDs = new ArrayList<>();

    private boolean hasNodeToken = false;

	/* ~Member variables */
	
	/* ************ Methods ************ */
	/* Default constructor */
	public CWrittenTokenSetNoStroke() { }
	
	/* Constructor: taking a CWrittenTokenSetNoStroke, extract a subset of the
	 * tokens and used them to form a new CWrittenTokenSetNoStroke. 
	 * Information about strokes is discarded in this construction process.
	 */
	public CWrittenTokenSetNoStroke(CWrittenTokenSetNoStroke owts, int [] indices) {
		setTokenNames(owts.tokenNames);
		
		for (int i = 0; i < indices.length; ++i) {
			addToken(owts.tokens.get(indices[i]));
			tokenIDs.add(owts.tokenIDs.get(indices[i]));
		}
		
		calcBounds();
	}
	
	/* Constructor: convert a CWrittenTokenSet into a CWrittenTokenSetNoStroke */
	public CWrittenTokenSetNoStroke(CWrittenTokenSet wts) {
		setTokenNames(wts.tokenNames);
		
		for (int i = 0; i < wts.nTokens(); ++i) {
			addToken(wts.tokens.get(i));
			tokenIDs.add(i);
		}
		
		calcBounds();
	}

    /* Factory method: From an array of written tokens */
    public static CWrittenTokenSetNoStroke from(AbstractToken[] writtenTokens) {
        if (writtenTokens == null) {
            throw new IllegalArgumentException("Null writtenTokens array");
        }

        CWrittenTokenSet wtSet0 = new CWrittenTokenSet();

        for (AbstractToken writtenToken : writtenTokens) {
            wtSet0.addToken(writtenToken);
        }

        return new CWrittenTokenSetNoStroke(wtSet0);

    }
	
	public void addToken(AbstractToken wt) {
        if (wt instanceof NodeToken) {
            hasNodeToken = true;
        }

		tokens.add(wt);

		addOneToken();
	}

		
	@Override
	public void calcBounds() {
		min_x = min_y = Float.POSITIVE_INFINITY;
		max_x = max_y = Float.NEGATIVE_INFINITY;
		
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

    // TODO: Since this is not used, should it be removed?
//	public void deleteToken(int i) {
//		if ( i < 0 ) {
//			System.err.println("Deletion index is negative");
//			return;
//		}
//
//		if ( i >= nTokens() ) {
//			System.err.println("Deletion index exceeds number of tokens");
//			return;
//		}
//
//		tokens.remove(i);
//
//		calcBounds();
//
//		deleteOneToken();
//	}
	
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
			int k = 0;
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
				tokenIDs.add(k++);
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
		tokenIDs.clear();
		
		min_x = min_y = Float.POSITIVE_INFINITY;
		max_x = max_y = Float.NEGATIVE_INFINITY;
		
		nt = 0;
	}

	@Override
	public float[] getTokenBounds(int i) {
//		return tokenBounds.get(i);
		return tokens.get(i).getBounds();
	}

    @Override
    public float[] setTokenBounds(int i, final float[] newBounds) {
        float[] oldBounds = tokens.get(i).getBounds();

        tokens.get(i).setBounds(newBounds);

        return oldBounds;
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
	
	/* Perform type getting operation on all tokens */
	public void getAllTokensTerminalTypes(TerminalSet termSet) {
		for (int i = 0; i < tokens.size(); ++i) {
			tokens.get(i).getTokenTerminalType(termSet);
		}
	}
	
	@Override
	public String getTokenTermType(int i) {
		return tokens.get(i).tokenTermType;
	}

	public String toString() {
		String s = "";
		
		for (int i = 0; i < tokens.size(); ++i) {
			s += tokenIDs.get(i);
			
			if ( i < tokens.size() - 1 )
				s += ",";
		}
		
		return s;
	}

    @Override
    public int hashCode() {
        int hc = 0;

        for (int i = 0; i < tokens.size(); ++i) {
            hc = 127 * hc + tokenIDs.get(i);
        }

        return hc;
    }

    public boolean hasNodeToken() {
        return hasNodeToken;
    }

    /**
     * Form a new token set from a subset of the tokens
     * @param tokenIndices  Indices to the tokens to be parsed into a node token (0-based)
     * @return The new token set
     */
    public CWrittenTokenSetNoStroke fromSubset(int[] tokenIndices) {
        if (tokenIndices == null) {
            throw new IllegalArgumentException("Null token indices");
        }

        final int numTokensToParse = tokenIndices.length;
        if (numTokensToParse == 0) {
            throw new IllegalArgumentException("Input token indices array is empty");
        }

        AbstractToken[] tokensToParse = new AbstractToken[numTokensToParse];
        for (int i = 0; i < tokenIndices.length; ++i) {
            tokensToParse[i] = tokens.get(tokenIndices[i]); // TODO: Mutability and concurrency?
        }

        return from(tokensToParse);
    }

}
