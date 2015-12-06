package me.scai.parsetree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.net.URL;

import me.scai.handwriting.*;
import me.scai.parsetree.evaluation.ParseTreeEvaluator;

public class GraphicalProductionSet {
	private static final String commentString = "#";
	private static final String separatorString = "---";
	
	public ArrayList<GraphicalProduction> prods = new ArrayList<>(); /* List of productions */
    public ArrayList<String> prodSumStrings = new ArrayList<>(); /* List of productions */

    private List<List<String>> terminalTypes;
    private Map<Integer, Set<Integer>> transitiveChildrenProdMap; // Key: production index; Value: all children production indices.
    private Set<Integer> visitedProdIndices;
	
	public TerminalSet terminalSet;
	private int [] searchIdx = null;
	/* The array of possible terminal type for each production. 
	 * Calculated by the private method: calcTermTypes() */
	
//	private HashMap<String, ArrayList<String> > ntTerminalTypes = new HashMap<String, ArrayList<String> >();			
	/* Used during calcTermTypes(int i) */
	
	/* Methods */
	/* Constructor */
	/* Default constructors: no argument --> empty production list */
	public GraphicalProductionSet() {
		
	}
	
	/* Constructor with a production list file name */
//	public GraphicalProductionSet(String prodListFileName) {
//		/* TODO */
//	}
	
	/* Get the number of productions */
	public int numProductions() {
		return prods.size();
	}
	
	
	private void readProductionsFromLines(String [] lines, TerminalSet termSet) {
		terminalSet = termSet;
		
		int idxLine = 0;
	
		/* Remove the empty lines at the end */
		lines = TextHelper.removeTrailingEmptyLines(lines);
		
//		System.out.println("Number of lines = " + lines.length); //DEBUG
		while ( idxLine < lines.length ) {
//			System.out.println("Line # = " + idxLine); //DEBUG
//			System.out.println("Current line = \"" + lines[idxLine] + "\""); //DEBUG
			
			//assert(lines[idxLine].startsWith(separatorString));
			idxLine++;
			
			ArrayList<String> pLines = new ArrayList<String>();
			while ( idxLine < lines.length && 
					lines[idxLine].length() != 0 ) {
				pLines.add(lines[idxLine]);
				idxLine++;
			}
			
			/* Construct a new production from the list of strings */			
			try {
				if ( pLines.get(0).startsWith(separatorString) )
					pLines.remove(0);

                GraphicalProduction gp = GraphicalProduction.genFromStrings(pLines, termSet);
				prods.add(gp);
                prodSumStrings.add(gp.sumString);
			}
			catch ( Exception e ) {
				System.err.println(e.getMessage());
			}
		}
		
//		final boolean bFast = false;		//DEBUG
//		int [] searchIdx = null;		/* TODO: Create a member variable, so that this array doesn't need to be created every time */
//		if ( searchSubsetIdx == null || !bFast ) { //DEBUG
		searchIdx = new int[prods.size()];
		for (int i = 0; i < searchIdx.length; ++i) {
			searchIdx[i] = i;
		}
//		}
//		else {
//			searchIdx = searchSubsetIdx;
//		}
	}
	
	/* Read productions from production list file */
	public void readProductionsFromFile(String prodListFileName, TerminalSet termSet)
		throws FileNotFoundException, IOException 
	{
		String [] lines;
		try {
			lines = TextHelper.readLinesTrimmedNoComment(prodListFileName, commentString);
		}
		catch ( FileNotFoundException fnfe ) {
			throw fnfe;
		}
		catch ( IOException ioe ) {
			throw ioe;
		}
		
		readProductionsFromLines(lines, termSet);
	}
	
	/* Read productions from production list file at a URL */
	public void readProductionsFromUrl(URL prodListFileUrl, TerminalSet termSet)
		throws FileNotFoundException, IOException 
	{
		String [] lines;
		try {
			lines = TextHelper.readLinesTrimmedNoCommentFromUrl(prodListFileUrl, commentString);
		}
		catch ( IOException ioe ) {
			throw ioe;
		}
		
		readProductionsFromLines(lines, termSet);
	}

//    public int [][] getIdxValidProds(NodeToken nodeToken,
//                                     ArrayList<int [][]> idxPossibleHead) {
//
//    }

	/** Get the indices to the productions that are valid for the token set.
	 * @param  termSet: Terminal set
     * @param  lhs:     Name of the graphical production LHS (e.g., "ROOT")
     * @param  idxPossibleHead:  Side effect input argument:
     *             idxPossibleHead: has the same length as the return value.
     *             Contain indices to the possible heads.
	 * @return int []: indices to all valid productions
     */
	public int[][] getIdxValidProds(CWrittenTokenSetNoStroke tokenSet,
			                         int [] searchSubsetIdx, 
			                         TerminalSet termSet, 
			                         String lhs,
			                         ArrayList<int [][]> idxPossibleHead, 
			                         boolean bDebug) throws InterruptedException {
		/* TODO: Make use of geomShortcuts */
		
		if ( bDebug )
			System.out.println("Calling getIdxValidProds on token set: " + tokenSet);
		
		if ( idxPossibleHead.size() != 0 ) {
			System.err.println("WARNING: Input ArrayList of int [], idxPossibleHead, is not empty.");
			idxPossibleHead.clear();
		}
	
		ArrayList<Integer> idxValidProdsList_woExclude = new ArrayList<>();
		ArrayList<Integer> idxValidProdsList = new ArrayList<>();

		for (int prodIdx : searchIdx) {

            /* Flags for exclusion due to lhs mismatch */
            if ( lhs != null && !prods.get(prodIdx).lhs.equals(lhs) ) {
                continue;
            }

			int[][] iph = evalWrittenTokenSet(prodIdx, tokenSet, termSet); // Get indices to possible head (iph)
			if ( iph == null || iph.length == 0 ) {
				continue;
			}

			idxValidProdsList_woExclude.add(prodIdx);
			
			boolean bExclude = false;
			
			/* Flags for exclusion due to extra terminal nodes */
            List<String> possibleTermTypesList = terminalTypes.get(prodIdx);
			
			for (int k = 0; k < tokenSet.nTokens(); ++k) {
                AbstractToken token = tokenSet.tokens.get(k);

                if (token instanceof CWrittenToken) {
                    String tokenName = token.getRecogResult();

                    if ( !termSet.typeListContains(possibleTermTypesList, tokenName) ) {
                        bExclude = true;
                        break;
                    }
                } else if (token instanceof NodeToken) {
                    //TODO: Implement: Exclusion based on the grammar graph
                } else {
                    throw new IllegalStateException("Unsupported AbstractNode subtype");
                }

			}
			
			if ( bExclude ) {
				continue;
			}

			idxValidProdsList.add(prodIdx);
			idxPossibleHead.add(iph);
		}
		
		int[][] indices2 = new int[2][];
		indices2[0] = new int[idxValidProdsList.size()];
		indices2[1] = new int[idxValidProdsList_woExclude.size()];

		for (int i = 0; i < idxValidProdsList.size(); ++i) {
            indices2[0][i] = idxValidProdsList.get(i);
        }
		
		for (int i = 0; i < idxValidProdsList_woExclude.size(); ++i) {
            indices2[1][i] = idxValidProdsList_woExclude.get(i);
        }


		return indices2;
	}
	
	
	/** Evaluate whether a token set meets the requirement of this production,
	 * i.e., has the head node available. 
	 * NOTE: this method does _not_ exclude productions that have extra head nodes.
	 * E.g., for production "DIGIT_STRING --> DIGIT DIGIT_STRING", the only 
	 * type of head node involved is DIGIT. So if a token set includes another
	 * type of head node, e.g., POINT ("."), it is invalid for this production
	 * but will still be included in the output. 
	 * 
	 * @return int[][]: will contain all indices (within the token set)
	 * of all tokens that can potentially be the head.
	 *  */
	public int [][] evalWrittenTokenSet(int prodIdx,        /* TODO: Change the return type to Array<Array<Integer>> */
										CWrittenTokenSetNoStroke wts,
			                            TerminalSet termSet) throws InterruptedException {
        Thread.sleep(0); // For effectiveness of timeout

        /* TODO: Generalize to written token sets with NodeToken */
		/* TODO: Deal with a production in which none of the rhs items are terminal */
		GraphicalProduction prod = prods.get(prodIdx);
		
		ArrayList<ArrayList<Integer>> possibleHeadIdx = new ArrayList<>();
		String headNodeType = prod.rhs[0];
		
		if ( termSet.isTypeTerminal(headNodeType) ) {
			/* The head node is a terminal (T). So each possible head 
			 * is just a single token in the token set.
			 */
			for (int i = 0; i < wts.nTokens(); ++i) {
                String tTokenName = null;

                if (wts.tokens.get(i) instanceof NodeToken &&
                    !((NodeToken) wts.tokens.get(i)).isPotentiallyTerminal()) {
                    // This is a NodeToken with >1 tokens, so this can't be a terminal
                    continue;
                }

                if (wts.tokens.get(i) instanceof NodeToken) {
                    CAbstractWrittenTokenSet wtSet = ((NodeToken) wts.tokens.get(i)).getTokenSet();

                    tTokenName = wtSet.getNumTokens() == 1 ? wtSet.getTokenName(0) : null;
                } else {
                    tTokenName = wts.tokens.get(i).getRecogResult();
                }

				if ( termSet.match(tTokenName, headNodeType) ) {
					ArrayList<Integer> t_possibleHeadIdx = new ArrayList<>();
					t_possibleHeadIdx.add(i);
					
					possibleHeadIdx.add(t_possibleHeadIdx);
				}
			}
			
		} else {
			/* The head node is a non-terminal (NT). */
			if ( prod.rhs.length == 1 ) {
				/* The rhs is an NT head node. In this case, we need to
				 * check whether this entire token is potentially suitable
				 * for the production specified by this NT rhs, in a
				 * recursive way. If the answer is no, will return empty.
				 */

                if (wts.hasNodeToken()) { // This token set contains at least one NodeToken // TODO: Verify logic
                    Set<Integer> childProdIndices = transitiveChildrenProdMap.get(prodIdx);

                    boolean allTokensFoundMatch = true;
                    for (AbstractToken token : wts.tokens) {
                        if (token instanceof NodeToken) {

                            List<Integer> matchingProdIndices = ((NodeToken) token).getMatchingGraphicalProductionIndices(this);
                            boolean tokenFoundMatch = false;

                            for (int mpi : matchingProdIndices) {
                                if (childProdIndices.contains(mpi)) {
                                    tokenFoundMatch = true;
                                    break;
                                }
                            }

                            if ( !tokenFoundMatch ) {
                                allTokensFoundMatch = false;
                                break;
                            }
                        }
                    }

                    if ( !allTokensFoundMatch ) {
                        return null;
                    } else{
                        /* There is only one rhs, that is, the NT, and the RHS
                         * has potential matches to the token set. So all tokens
                         * in the token set should belong to the head. */
                        ArrayList<Integer> t_possibleHeadIdx = new ArrayList<>();
                        for (int i = 0; i < wts.nTokens(); ++i) {
                            t_possibleHeadIdx.add(i);
                        }

                        possibleHeadIdx.add(t_possibleHeadIdx);
                    }

                } else {
                    String lhs = prod.rhs[0];

				    /* Find all productions that fit this lhs */
                    boolean anyRHSMatch = false;
                    for (int i = 0; i < prods.size(); ++i) {
                        if (prods.get(i).lhs.equals(lhs)) {
                            int[][] t_t_iph = evalWrittenTokenSet(i, wts, termSet);
                            if (t_t_iph != null && t_t_iph.length > 0) {
                                anyRHSMatch = true;
                                break;
                            }
                        }
                    }

                    if (!anyRHSMatch) {
                        return null;
                    } else {
                        /* There is only one rhs, that is, the NT, and the RHS
                         * has potential matches to the token set. So all tokens
                         * in the token set should belong to the head. */
                        ArrayList<Integer> t_possibleHeadIdx = new ArrayList<>();
                        for (int i = 0; i < wts.nTokens(); ++i) {
                            t_possibleHeadIdx.add(i);
                        }

                        possibleHeadIdx.add(t_possibleHeadIdx);
                    }
                }

			} else {
				/* There are rhs items other than the head NT. */
                // TODO: Accommodate NodeToken

				int[][] combs = null;
				if ( prod.geomShortcut.existsBipartite() ) {
					combs = prod.geomShortcut.getPartitionBipartite(wts, true);
				} else if ( prod.geomShortcut.existsTripartiteNT1T2() ) {
					combs = prod.geomShortcut.getPartitionTripartiteNT1T2(wts);
				} else {
                    if (wts.nTokens() > 3) {
                        int iii = 111; //DEBUG DEL
                    }

					combs = MathHelper.getFullDiscreteSpace(2, wts.nTokens()); // Binary divide between head and non-head
                    /* TODO: Discard the partitions that don't make sense to speed things up.
                     * For example, interlocking partitions. */
				}

				
				for (int i = 0; i < combs.length; ++i) {
					ArrayList<Integer> t_possibleHeadIdx = new ArrayList<>();
					
					for (int j = 0; j < combs[i].length; ++j) {
                        if (combs[i][j] == 1) {
                            t_possibleHeadIdx.add(j);
                        }
                    }
					
					possibleHeadIdx.add(t_possibleHeadIdx);	
				}
			}
		}
		
		int [][] idx = new int[possibleHeadIdx.size()][];
		for (int i = 0; i < possibleHeadIdx.size(); ++i) {
			idx[i] = new int[possibleHeadIdx.get(i).size()];
			
			for (int j = 0; j < possibleHeadIdx.get(i).size(); ++j)
				idx[i][j] = possibleHeadIdx.get(i).get(j);
		}
		
		return idx;
	}
	
	
	/* Get the count of non-head tokens in production #i */
	public int getNumNonHeadTokens(int i) {
		return prods.get(i).getNumNonHeadTokens();
	}
	
	/* Factory method: From file */
	public static GraphicalProductionSet createFromFile(String prodListFileName, TerminalSet termSet)
		throws FileNotFoundException, IOException {
		GraphicalProductionSet gpSet = new GraphicalProductionSet();
		try {
			gpSet.readProductionsFromFile(prodListFileName, termSet);
		}
		catch ( FileNotFoundException fnfe ) {
			throw fnfe;
		}
		catch ( IOException ioe ) {
			throw ioe;
		}
		
		gpSet.calcTermTypes(termSet);
		
		return gpSet;
	}
	
	/* Factory method: From URL */
	public static GraphicalProductionSet createFromUrl(URL prodListFileUrl, TerminalSet termSet)
		throws FileNotFoundException, IOException {
		GraphicalProductionSet gpSet = new GraphicalProductionSet();
		try {
			gpSet.readProductionsFromUrl(prodListFileUrl, termSet);
		}
		catch ( FileNotFoundException fnfe ) {
			throw fnfe;
		}
		catch ( IOException ioe ) {
			throw ioe;
		}
		
		gpSet.calcTermTypes(termSet);
        gpSet.calcTransitiveChildrenProdMap();
		
		return gpSet;
	}
	
	/* Test main */
//	public static void main(String [] args) {
//		final String prodSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\productions.txt";
//		final String termSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\terminals.txt";
//		
//		TerminalSet termSet = null;
//		try {
//			termSet = TerminalSet.createFromFile(termSetFN);
//		}
//		catch ( Exception e ) {
//			System.err.println(e);
//		}
//		
//		GraphicalProductionSet gps = new GraphicalProductionSet();
//		try {
//			gps.readProductionsFromFile(prodSetFN, termSet);
//		}
//		catch ( Exception e ) {
//			System.err.println(e);
//		}
//	}

    private void calcTransitiveChildrenProdMap() {
        transitiveChildrenProdMap = new HashMap<>();

        for (int i = 0; i < prods.size(); ++i) {
            visitedProdIndices = new HashSet<>();
            getTransitiveChildrenProdIndices(i);
        }
    }

    /**
     * Get the children productions of a given production.
     * @param prodIdx
     * @return
     */
    private Set<Integer> getTransitiveChildrenProdIndices(int prodIdx) {
        visitedProdIndices.add(prodIdx); // Prevent going in loops

        Set<Integer> result = new HashSet<>(); // TODO: Performance fine tune?

        GraphicalProduction gp = prods.get(prodIdx);

        List<String> rhsList = new ArrayList<>();
        for (int i = 0; i < gp.rhs.length; ++i) {
            if ( !gp.rhsIsTerminal[i] ) {
                rhsList.add(gp.rhs[i]);
            }
        }

        if (rhsList.isEmpty()) { // No NT children: return an empty list
            transitiveChildrenProdMap.put(prodIdx, result);
            return result;
        }

        for (int i = 0; i < prods.size(); ++i) {
//            if (i == prodIdx) {
//                continue;
//            }

            GraphicalProduction gp1 = prods.get(i);

            if (rhsList.contains(gp1.lhs)) {
                // Add the production itself
                result.add(i);

                // Transitively add all its children
//                if ( !gp.lhs.equals(gp1.lhs) ) {  // How about right recursion?
                if ( !visitedProdIndices.contains(i) ) {  // How about right recursion?
                    if (transitiveChildrenProdMap.containsKey(i)) {
                        result.addAll(transitiveChildrenProdMap.get(i)); // Use caching
                    } else {
                        result.addAll(getTransitiveChildrenProdIndices(i));
                    }
                }
            }
        }

        transitiveChildrenProdMap.put(prodIdx, result);
        return result;
    }

	
	/* Get the lists of all possible heads that corresponds to all
	 * productions.
	 */
	private void calcTermTypes(TerminalSet termSet) {
        terminalTypes = new ArrayList<>();

		for (int i = 0; i < prods.size(); ++i) {
            terminalTypes.add(calcTermTypes(i, termSet, null));
		}
	}

	/* Get the list of all possible heads contained within a valid 
	 * token, for the i-th production in the production list.
	 * This function is called by calcTermTypes().
	 * 
	 * Input ip: index to the production within the set.
	 * 
	 * Algorithm: recursively goes down the production hierarchy, 
	 * until a production that contains only terminals (including 
	 * EPS) is met. 
	 */
	private List<String> calcTermTypes(int ip,
			                        TerminalSet termSet, 
			                        boolean [] visited) {		
		if ( visited == null ) {
			 visited = new boolean[prods.size()];
		}
//		for (int i = 0; i < prods.size(); ++i)
//			bVisited.add(false);
		
		if ( ip < 0 || ip >= prods.size() ) {
			throw new IllegalArgumentException("Invalid input production index");
		}
		
		if ( visited.length != prods.size() ) {
			throw new IllegalArgumentException("Invalid input boolean array (visited)");
		}
		
		visited[ip] = true; /* To prevent infinite loops */
		GraphicalProduction gp = prods.get(ip);
		
		ArrayList<String> termTypesList = new ArrayList<String>();
		
		for (int i = 0; i < gp.rhs.length; ++i) {
			if ( termSet.isTypeTerminal(gp.rhs[i]) ) {
//				if ( !gp.rhs[i].equals(TerminalSet.epsString) )
				termTypesList.add(gp.rhs[i]);
			}
			else {
//				if ( ntTerminalTypes.keySet().contains(gp.rhs[i]) ) {
//					/* Re-use previous results */					
//					ArrayList<String> childTermTypes = ntTerminalTypes.get(gp.rhs[i]);
//					for (int j = 0; j < childTermTypes.size(); ++j)
//						termTypesList.add(childTermTypes.get(j));
//				}
//				else {
				for (int j = 0; j < visited.length; ++j) {						
					if ( gp.rhs[i].equals(prods.get(j).lhs) && !visited[j] ) {
                        List<String> childTermTypes = calcTermTypes(j, termSet, visited);

                        termTypesList.addAll(childTermTypes);

					}
				}
			}
		}
		
		Set<String> uniqueTermTypes = new HashSet<>(termTypesList);

        ArrayList<String> termTypes = new ArrayList<>();
        termTypes.ensureCapacity(uniqueTermTypes.size());

        termTypes.addAll(uniqueTermTypes);

		return termTypes;
	}
	
	public ParseTreeStringizer genStringizer() {
		return new ParseTreeStringizer(this);
	}
	
	public ParseTreeEvaluator genEvaluator() {
		return new ParseTreeEvaluator(this);
	}
}
