package me.scai.parsetree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import me.scai.handwriting.CAbstractWrittenTokenSet;
import me.scai.handwriting.CWrittenTokenSet;

public class GraphicalProductionSet {
	private static final String commentString = "#";
	private static final String separatorString = "---";
	
	protected ArrayList<GraphicalProduction> prods 
		= new ArrayList<GraphicalProduction>(); /* List of productions */
	
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
			                       ArrayList<int []> idxPossibleHead) {
		if ( idxPossibleHead.size() != 0 ) {
			System.err.println("WARNING: Input ArrayList of int [], idxPossibleHead, is not empty.");
			idxPossibleHead.clear();
		}
		
		ArrayList<Integer> idxValidProdsList = new ArrayList<Integer>(); 
		for (int i = 0; i < prods.size(); ++i) {
			int [] t_iph = prods.get(i).evalWrittenTokenSet(tokenSet, termSet);
			if ( t_iph.length > 0 ) {
				idxValidProdsList.add(i);
				idxPossibleHead.add(t_iph);
			}
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
						int j,
						ArrayList<CAbstractWrittenTokenSet> remainingSets) {
		Node n = prods.get(i).attempt(tokenSet, j, remainingSets);
		
		/* Create head child node */
		if ( n != null && 
	         prods.get(i).rhs.length > 0 ) {
			/* TODO: hc should contain information about the 
			 * tokens that make up of the head child for further 
			 * parsing.
			 */
			Node hc = new Node(prods.get(i).rhs[0], tokenSet.recogWinners.get(j));
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

	
}
