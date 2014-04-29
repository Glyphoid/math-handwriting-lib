package me.scai.parsetree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import me.scai.handwriting.CAbstractWrittenTokenSet;
import me.scai.handwriting.CWrittenTokenSet;

public class GraphicalProductionSet {
	private static final String commentString = "#";
	private static final String separatorString = "---";
	
	protected ArrayList<GraphicalProduction> prods
		= new ArrayList<GraphicalProduction>(); /* List of productions */
	
	protected ArrayList<String []> terminalTypes = new ArrayList<String []>();
	/* The array of possible terminal type for each production. 
	 * Calculated by the private method: calcTermTypes() */
	
	private HashMap<String, ArrayList<String> > ntTerminalTypes = new HashMap<String, ArrayList<String> >();			
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
	
	/* Read productions from production list */
	public void readProductionsFromFile(String prodListFileName, TerminalSet termSet)
		throws FileNotFoundException, IOException {
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
		
		int idxLine = 0;
		
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
	
	/* Get the indices to the productions that are valid for the token set.
	 * Output: int []: indices to all valid productions
	 *         Side effect input argument:
	 *             idxPossibleHead: has the same length as the return value.
	 *             Contain indices to the possible heads. */
	public int [] getIdxValidProds(CAbstractWrittenTokenSet tokenSet, 
			                       TerminalSet termSet, 	
			                       String lhs,
			                       ArrayList<int [][]> idxPossibleHead) {
		if ( idxPossibleHead.size() != 0 ) {
			System.err.println("WARNING: Input ArrayList of int [], idxPossibleHead, is not empty.");
			idxPossibleHead.clear();
		}
		
		ArrayList<Integer> idxValidProdsList = new ArrayList<Integer>(); 
		for (int i = 0; i < prods.size(); ++i) {
			int [][] t_iph = prods.get(i).evalWrittenTokenSet(tokenSet, termSet);
			if ( t_iph.length == 0 )
				continue;
			
			boolean bExclude = false;
			
			/* Flags for exclusion due to extra terminal nodes */			
			String [] possibleTermTypes = terminalTypes.get(i);
			List<String> possibleTermTypesList = Arrays.asList(possibleTermTypes);
			
			for (int k = 0; k < tokenSet.nTokens(); ++k) {
				String tokenType = termSet.getTypeOfToken(tokenSet.recogWinners.get(k));
				if ( !possibleTermTypesList.contains(tokenType) ) {
					bExclude = true;
					break;
				}
			}
			
			/* Flags for exclusion due to lhs mismatch */
			if ( lhs != null )
				if ( !prods.get(i).lhs.equals(lhs) )
					bExclude = true;

			if ( bExclude )
				continue;

			idxValidProdsList.add(i);
			idxPossibleHead.add(t_iph);
		}
		
		int [] idxValidProds = new int[idxValidProdsList.size()];
		for (int i = 0; i < idxValidProdsList.size(); ++i) {
			idxValidProds[i] = idxValidProdsList.get(i);
		}
		
		return idxValidProds;
	}
	
	/* Get the count of non-head tokens in production #i */
	public int getNumNonHeadTokens(int i) {
		return prods.get(i).getNumNonHeadTokens();
	}
	
	/* Try to parse the token set using the i-th production, 
	 * given that the j-th token in the token set is used as
	 * the head. 
	 * 
	 * Return value:
	 * 	    A node with the production, geometric and other
	 *      information. null if not successful. 
	 * 
	 * Side effect input argument:
	 *     remainingSets: the remaining, node head 
	 *     token sets. null if parsing is unsuccessful.
	 *     
	 * TODO: j should be an array, to allow the head node to 
	 *       be made of more than one tokens
	 */
	public Node attempt(int i, 
						CAbstractWrittenTokenSet tokenSet, 
						int [] idxHead,
						ArrayList<CAbstractWrittenTokenSet> remainingSets, 
						float [] maxGeomScore) {
		Node n = prods.get(i).attempt(tokenSet, idxHead, remainingSets, maxGeomScore);
		
		/* Create head child node */
		if ( n != null && 
	         prods.get(i).rhs.length > 0 ) {
			/* TODO: hc should contain information about the 
			 * tokens that make up of the head child for further 
			 * parsing.
			 * hc also needs to be expanded if it is an NT. 
			 */
			Node hc = new Node(prods.get(i).rhs[0], prods.get(i).rhs[0]);
			n.addChild(hc);
			
			if ( prods.get(i).rhs.length == 2 && 
				 prods.get(i).rhs[1].equals(TerminalSet.epsString) ) {
				/* Append EPS */
				n.addChild(new Node(TerminalSet.epsString, TerminalSet.epsString));
			}
		}
		
		return n;

	}	
	
	/* Factory method */
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
	
	/* Test main */
	public static void main(String [] args) {
		final String prodSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\productions.txt";
		final String termSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\terminals.txt";
		
		TerminalSet termSet = null;
		try {
			termSet = TerminalSet.createFromFile(termSetFN);
		}
		catch ( Exception e ) {
			System.err.println(e);
		}
		
		GraphicalProductionSet gps = new GraphicalProductionSet();
		try {
			gps.readProductionsFromFile(prodSetFN, termSet);
		}
		catch ( Exception e ) {
			System.err.println(e);
		}
	}
	
	/* Get the lists of all possible heads that corresponds to all
	 * productions.
	 */
	
	private void calcTermTypes(TerminalSet termSet) {
		terminalTypes.clear();
		ntTerminalTypes.clear();		
		
		for (int i = 0; i < prods.size(); ++i)
			terminalTypes.add(calcTermTypes(i, termSet, null));
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
				if ( !gp.rhs[i].equals(TerminalSet.epsString) )
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
	
	
}
