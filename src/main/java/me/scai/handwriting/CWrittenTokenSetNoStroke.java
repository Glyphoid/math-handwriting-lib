package me.scai.handwriting;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.AssertionError;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.List;

import me.scai.parsetree.TerminalSet;

public class CWrittenTokenSetNoStroke extends CAbstractWrittenTokenSet {
	/* Member variables */
	public ArrayList<AbstractToken> tokens = new ArrayList<>();
    public List<List<String>> tokenUuids = new ArrayList<>(); // Keeps track of the constituent written tokens

	public ArrayList<Integer> tokenIDs = new ArrayList<>();

    private boolean hasNodeToken = false;

	/* ~Member variables */
	
	/* ************ Methods ************ */
	/* Default constructor */
	public CWrittenTokenSetNoStroke() {}
	
	/* Constructor: taking a CWrittenTokenSetNoStroke, extract a subset of the
	 * tokens and used them to form a new CWrittenTokenSetNoStroke. 
	 * Information about strokes is discarded in this construction process.
	 */
	public CWrittenTokenSetNoStroke(CWrittenTokenSetNoStroke owts, int[] indices) {
		setTokenNames(owts.tokenNames);
		
		for (int i = 0; i < indices.length; ++i) {
			addToken(owts.tokens.get(indices[i]), owts.getConstituentTokenUuids(indices[i]), owts.tokenIDs.get(indices[i]));
		}

		calcBounds();
	}
	
	/* Constructor: convert a CWrittenTokenSet into a CWrittenTokenSetNoStroke */
	public CWrittenTokenSetNoStroke(CWrittenTokenSet wts, List<String> wtUuids) {
		setTokenNames(wts.tokenNames);
		
		for (int i = 0; i < wts.nTokens(); ++i) {
            List<String> constituentUuids = new ArrayList<>();
            constituentUuids.add(wtUuids.get(i)); // Length-1 list

			addToken(wts.tokens.get(i), constituentUuids, i);
		}

//        if (wts instanceof CWrittenTokenSetNoStroke) {
//            this.hasNodeToken = ((CWrittenTokenSetNoStroke) wts).hasNodeToken;
//        }
		
		calcBounds();
	}

    /* Factory method: From an array of written tokens */
//    public static CWrittenTokenSetNoStroke from(AbstractToken[] writtenTokens) {
    public static CWrittenTokenSetNoStroke from(CWrittenToken[] writtenTokens) {
        if (writtenTokens == null) {
            throw new IllegalArgumentException("Null writtenTokens array");
        }

        CWrittenTokenSet wtSet0 = new CWrittenTokenSet();

        for (CWrittenToken writtenToken : writtenTokens) {
            wtSet0.addToken(writtenToken);
        }

        return new CWrittenTokenSetNoStroke(wtSet0, TokenUuidUtils.getRandomTokenUuids(writtenTokens.length));

    }

    /* Factory method from Abstract Tokens */
    public static CWrittenTokenSetNoStroke from(AbstractToken[] abstractTokens) {
        // Generate random UUID lists
        List<List<String>> randomUuids = new ArrayList<>();
        ((ArrayList) randomUuids).ensureCapacity(abstractTokens.length);

        for (AbstractToken token : abstractTokens) {
            if (token instanceof NodeToken) {
                final int numTokens = ((NodeToken) token).getTokenSet().getNumTokens();

                randomUuids.add(TokenUuidUtils.getRandomTokenUuids(numTokens));
            } else {
                randomUuids.add(TokenUuidUtils.getRandomTokenUuids(1));
            }
        }

        return from(abstractTokens, randomUuids);
    }

    /* Factory method from Abstract Tokens */
    public static CWrittenTokenSetNoStroke from(AbstractToken[] abstractTokens,
                                                List<List<String>> constituentTokenUuids) {
        if (abstractTokens == null) {
            throw new IllegalArgumentException("Null abstract token array");
        }

        CWrittenTokenSetNoStroke r = new CWrittenTokenSetNoStroke();

        for (int i = 0; i < abstractTokens.length; ++i) {
            AbstractToken abstractToken = abstractTokens[i];

            r.addToken(abstractToken, constituentTokenUuids.get(i), i);

            if (abstractToken instanceof NodeToken) {
                r.hasNodeToken = true;
            }

        }

        r.calcBounds();

        return r;
    }

    /**
     * The token UUIDs will be omitted for performance.
     * @param token
     */
    public void addTokenWithoutUuids(AbstractToken token, int tokenID) {
        List<String> constituentUuids = null;

        addToken(token, constituentUuids, tokenID);
    }

    public void addToken(AbstractToken token, String wtUuid, int tokenID) {
        List<String> constituentUuids = new ArrayList<>();
        constituentUuids.add(wtUuid);

        addToken(token, constituentUuids, tokenID);
    }
	
	public void addToken(AbstractToken token, List<String> wtUuids, int tokenID) {
        if (token instanceof NodeToken) {
            hasNodeToken = true;
        }

		tokens.add(token);
        tokenUuids.add(wtUuids);
        tokenIDs.add(tokenID);

		addOneToken();

        assert(tokens.size() == nt);
        assert(tokenUuids.size() == nt);
        assert(tokenIDs.size() == nt);

	}

    // TODO: Test coverage
    public void addTokenWithAutoTokenID(AbstractToken token, String wtUuid) {
        addToken(token, wtUuid, getAutoTokenID());
    }

    // Get a token ID that is not duplicate with any existing ones.
    private int getAutoTokenID() {
        int maxID = Integer.MIN_VALUE;
        for (int tokenID : tokenIDs) {
            if (tokenID > maxID) {
                maxID = tokenID;
            }
        }

        return maxID == Integer.MIN_VALUE ? 0 : (maxID + 1);
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

				addToken(wt, TokenUuidUtils.getRandomTokenUuid(), k); // TODO: De-dupe

                k++;
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
	
	@Override
	public void clear() {
		tokens.clear();
        tokenUuids.clear();
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
	public List<String> getTokenTermTypes(int i) {
		return tokens.get(i).tokenTermTypes;
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

        AbstractToken[] tokens = new AbstractToken[tokenIndices.length];
        List<List<String>> UUIDs = new ArrayList<>();

        for (int i = 0; i < tokenIndices.length; ++i) {
            final int tokenIndex = tokenIndices[i];

            tokens[i] = this.tokens.get(tokenIndex);
            UUIDs.add(this.tokenUuids.get(tokenIndex));
        }

        return from(tokens, UUIDs);

    }

    // TODO: Test coverage
    public void removeToken(int i) {
        if ( i < 0 ) {
            throw new IllegalArgumentException("Deletion index is negative");
        }

        if ( i >= nTokens() ) {
            throw new IllegalArgumentException("Deletion index exceeds number of tokens");
        }

        tokens.remove(i);
        tokenUuids.remove(i);
        tokenIDs.remove(i); // Is this okay?

        calcBounds();

        // Update hasNodeToken
        hasNodeToken = false;
        for (AbstractToken token : tokens) {
            if (token instanceof NodeToken) {
                hasNodeToken = true;
                break;
            }
        }

        deleteOneToken();

        assert(tokens.size() == nt);
        assert(tokenUuids.size() == nt);
        assert(tokenIDs.size() == nt);
    }

    public List<List<String>> getConstituentTokenUuids() {
        return tokenUuids;
    }

    public List<String> getConstituentTokenUuids(int idxToken) {
        return tokenUuids.get(idxToken);
    }

    @Override
    public String getTokenName(int i) {
        return tokens.get(i).getRecogResult();
    }


}
