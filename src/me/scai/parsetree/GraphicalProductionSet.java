package me.scai.parsetree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.net.URL;

import me.scai.handwriting.CWrittenTokenSetNoStroke;

public class GraphicalProductionSet {
	private static final String commentString = "#";
	private static final String separatorString = "---";
	
	public ArrayList<GraphicalProduction> prods
		= new ArrayList<GraphicalProduction>(); /* List of productions */
	
//	protected ArrayList<String []> terminalTypes = new ArrayList<String []>();
	protected String [][] terminalTypes;
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
		int idxLine = 0;
		
		/* Remove the empty lines at the end */
		lines = TextHelper.removeTrailingEmptyLines(lines);
		
		while ( idxLine < lines.length ) {
			assert(lines[idxLine].startsWith(separatorString));
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
				
				prods.add(GraphicalProduction.genFromStrings(pLines, termSet));
			}
			catch ( Exception e ) {
				System.err.println(e.getMessage());
			}
		}
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
	
	/* Get the indices to the productions that are valid for the token set.
	 * Output: int []: indices to all valid productions
	 *         Side effect input argument:
	 *             idxPossibleHead: has the same length as the return value.
	 *             Contain indices to the possible heads. */
	public int [][] getIdxValidProds(CWrittenTokenSetNoStroke tokenSet,
			                         int [] searchSubsetIdx, 
			                         TerminalSet termSet, 	
			                         String lhs,
			                         ArrayList<int [][]> idxPossibleHead, 
			                         boolean bDebug) {
		/* TODO: Make use of geomShortcuts */
		
		if ( bDebug )
			System.out.println("Calling getIdxValidProds on token set: " + tokenSet);
		
		if ( idxPossibleHead.size() != 0 ) {
			System.err.println("WARNING: Input ArrayList of int [], idxPossibleHead, is not empty.");
			idxPossibleHead.clear();
		}
	
		ArrayList<Integer> idxValidProdsList_woExclude = new ArrayList<Integer>();
		ArrayList<Integer> idxValidProdsList = new ArrayList<Integer>(); 
		
		final boolean bFast = false;		//DEBUG
		int [] searchIdx = null;		/* TODO: Create a member variable, so that this arracy doesn't need to be created every time */
		if ( searchSubsetIdx == null || !bFast ) { //DEBUG
			searchIdx = new int[prods.size()];
			for ( int i = 0; i < searchIdx.length; ++i)
				searchIdx[i] = i;
		}
//		else {
//			searchIdx = searchSubsetIdx;
//		}
		
//		for (int i = 0; i < prods.size(); ++i) {
		for (int i = 0; i < searchIdx.length; ++i) {
			int prodIdx = searchIdx[i];
			
			int [][] t_iph = evalWrittenTokenSet(prodIdx, tokenSet, termSet);
			if ( t_iph == null || t_iph.length == 0 )
				continue;
			
			/* Flags for exclusion due to lhs mismatch */
			if ( lhs != null && !prods.get(prodIdx).lhs.equals(lhs) )
				continue;
			
			idxValidProdsList_woExclude.add(prodIdx);
			
			boolean bExclude = false;
			
			/* Flags for exclusion due to extra terminal nodes */
			String [] possibleTermTypes = terminalTypes[prodIdx];
			List<String> possibleTermTypesList = Arrays.asList(possibleTermTypes);
			
			for (int k = 0; k < tokenSet.nTokens(); ++k) {
//				String tokenType = termSet.getTypeOfToken(tokenSet.recogWinners.get(k));
				String tokenType = termSet.getTypeOfToken(tokenSet.tokens.get(k).getRecogWinner());
				if ( !possibleTermTypesList.contains(tokenType) ) {
					bExclude = true;
					break;
				}
			}
			
			if ( bExclude )
				continue;

			idxValidProdsList.add(prodIdx);
			idxPossibleHead.add(t_iph);
		}
		
		int [][] indices2 = new int[2][];
		indices2[0] = new int[idxValidProdsList.size()];
		indices2[1] = new int[idxValidProdsList_woExclude.size()];
		
//		int [] idxValidProds = new int[idxValidProdsList.size()];
		for (int i = 0; i < idxValidProdsList.size(); ++i)
//			idxValidProds[i] = idxValidProdsList.get(i);
			indices2[0][i] = idxValidProdsList.get(i);
		
		for (int i = 0; i < idxValidProdsList_woExclude.size(); ++i) 
			indices2[1][i] = idxValidProdsList_woExclude.get(i);
		
//		if ( idxValidProdsList_woExclude.size() > idxValidProdsList.size() )	//DEBUG
//			System.out.println("Without exclusion: " + idxValidProdsList_woExclude.size() + 
//					           "; with exclusion: " + idxValidProdsList.size());
		
//		return idxValidProds;
		return indices2;
	}
	
	
	/* Evaluate whether a token set meets the requirement of this production, 
	 * i.e., has the head node available. 
	 * NOTE: this method does _not_ exclude productions that have extra head nodes.
	 * E.g., for production "DIGIT_STRING --> DIGIT DIGIT_STRING", the only 
	 * type of head node involved is DIGIT. So if a token set includes another
	 * type of head node, e.g., POINT ("."), it is invalid for this production
	 * but will still be included in the output. 
	 * 
	 * Return value: boolean: will contain all indices (within the token set)
	 * of all tokens that can potentially be the head.
	 *  */
	public int [][] evalWrittenTokenSet(int prodIdx,  
										CWrittenTokenSetNoStroke wts,
			                            TerminalSet termSet) {
		/* TODO: Deal with a production in which none of the rhs items are terminal */
		/* Can use MathHelper.getFullDiscreteSpace() */
		GraphicalProduction t_prod = prods.get(prodIdx);
		
		ArrayList<ArrayList<Integer>> possibleHeadIdx = new ArrayList<ArrayList<Integer>>();
		String headNodeType = t_prod.rhs[0];
		
		if ( termSet.isTypeTerminal(headNodeType) ) {
			/* The head node is a terminal (T). So each possible head 
			 * is just a single token in the token set.
			 */
			for (int i = 0; i < wts.nTokens(); ++i) {
//				String tTokenName = wts.recogWinners.get(i);
				String tTokenName = wts.tokens.get(i).getRecogWinner();
				String tTokenType = termSet.getTypeOfToken(tTokenName);
				if ( tTokenType.equals(headNodeType) ) {
					ArrayList<Integer> t_possibleHeadIdx = new ArrayList<Integer>();
					t_possibleHeadIdx.add(i);
					
					possibleHeadIdx.add(t_possibleHeadIdx);
				}
			}
			
		}
		else {
			/* The head node is a non-terminal (NT). 
			 */
			if ( t_prod.rhs.length == 1 ) {
				/* The rhs is an NT head node. In this case, we need to
				 * check whether this entire token is potentially suitable
				 * for the production specified by this NT rhs, in a
				 * recursive way. If the answer is no, will return empty.
				 */
				
				String t_lhs = t_prod.rhs[0];
				/* Find all productions that fit this lhs */
				boolean anyRHSMatch = false;
				for (int i = 0; i < prods.size(); ++i) { 
					if ( prods.get(i).lhs.equals(t_lhs) ) {
						int [][] t_t_iph = evalWrittenTokenSet(i, wts, termSet);
						if ( t_t_iph != null && t_t_iph.length > 0 )
							anyRHSMatch = true;
					}
				}
				
				if ( !anyRHSMatch ) {
					return null;
				}
				else {
					/* There is only one rhs, that is, the NT, and the RHS 
					 * has potential matches to the token set. So all tokens 
					 * in the token set should belong to the head. */
					ArrayList<Integer> t_possibleHeadIdx = new ArrayList<Integer>();
					for (int i = 0; i < wts.nTokens(); ++i)
						t_possibleHeadIdx.add(i);
				
					possibleHeadIdx.add(t_possibleHeadIdx);
				}
			}
			else {
				/* There are rhs items other than the head NT. */
				int [][] combs = null;

				if ( t_prod.geomShortcut.existsBipartite() ) {
					combs = t_prod.geomShortcut.getPartitionBipartite(wts, true);
				}
				else if ( t_prod.geomShortcut.existsTripartiteNT1T2() ) {
					combs = t_prod.geomShortcut.getPartitionTripartiteNT1T2(wts);
					/* TODO */
//					/* This is the case in which the head node is NT,  
//					 * and there are two non-heads
//					 */
//					int [] iHead = new iHead;
//					combs = t_prod.geomShortcut.getPartitionTripartite(wts, true)
				}
				else {
					combs = MathHelper.getFullDiscreteSpace(2, wts.nTokens());
				}
				/* TODO: Discard the partitions that don't make sense to speed things up */
				
				for (int i = 0; i < combs.length; ++i) {
					ArrayList<Integer> t_possibleHeadIdx = new ArrayList<Integer>();
					
					for (int j = 0; j < combs[i].length; ++j)
						if ( combs[i][j] == 1 )
							t_possibleHeadIdx.add(j);
					
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
	
	/* Get the lists of all possible heads that corresponds to all
	 * productions.
	 */
	
	private void calcTermTypes(TerminalSet termSet) {
//		terminalTypes.clear();
//		ntTerminalTypes.clear();
		terminalTypes = new String[prods.size()][];
		
		for (int i = 0; i < prods.size(); ++i) 
//			terminalTypes.add(calcTermTypes(i, termSet, null));
			terminalTypes[i] = calcTermTypes(i, termSet, null);
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
	private String [] calcTermTypes(int ip, 
			                        TerminalSet termSet, 
			                        boolean [] visited) {		
		if ( visited == null )
			 visited = new boolean[prods.size()];
//		for (int i = 0; i < prods.size(); ++i)
//			bVisited.add(false);
		
		if ( ip < 0 || ip >= prods.size() )
			throw new IllegalArgumentException("Invalid input production index");
		
		if ( visited.length != prods.size() )
			throw new IllegalArgumentException("Invalid input boolean array (visited)");
		
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
							String [] childTermTypes = calcTermTypes(j, termSet, visited);
							
							for (int k = 0; k < childTermTypes.length; ++k) {
								termTypesList.add(childTermTypes[k]);
								
								
//								ntTerminalTypes.get(gp.rhs[i]).add(childTermTypes[k]);
							}
						}
//					}
				}
				 
			}
		}
		
		Set<String> uniqueTermTypes = new HashSet<String>(termTypesList);
		String [] termTypes = new String[uniqueTermTypes.size()];
		uniqueTermTypes.toArray(termTypes);
		return termTypes;
	}
	
	public String [] getRHS(int i) {
		return prods.get(i).rhs;
	}
	
	public ParseTreeStringizer genStringizer() {
		return new ParseTreeStringizer(this);
	}
	
	public ParseTreeEvaluator genEvaluator() {
		return new ParseTreeEvaluator(this);
	}
}
