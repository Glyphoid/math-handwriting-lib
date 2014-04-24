package me.scai.parsetree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
	public GraphicalProductionSet(String prodListFileName) {
		/* TODO */
	}
	
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
