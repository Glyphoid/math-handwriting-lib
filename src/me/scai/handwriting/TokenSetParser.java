package me.scai.handwriting;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import me.scai.parsetree.Node;
import me.scai.parsetree.TerminalSet;
import me.scai.parsetree.GraphicalProductionSet;

public class TokenSetParser implements ITokenSetParser {
	protected TerminalSet termSet = null;
	protected GraphicalProductionSet gpSet = null;
	
	/* Methods */
	
	/* Constructors */
	public TokenSetParser(String terminalSetFN, 
			              String graphicalProductionSetFN) {
		try {
			termSet = TerminalSet.createFromFile(terminalSetFN);
		}
		catch ( Exception e ) {
			System.err.println(e.getMessage());
		}
		
		try {
			gpSet = GraphicalProductionSet.createFromFile(graphicalProductionSetFN, termSet);
		}
		catch ( FileNotFoundException fnfe ) {
			System.err.println(fnfe.getMessage());
		}
		catch ( IOException e ) {
			System.err.println(e.getMessage());
		}		
	}
	
	
	/* This implements a recursive descend parser */
	@Override
	public Node parse(CAbstractWrittenTokenSet tokenSet) {
		ArrayList<int []> idxPossibleHead = new ArrayList<int []>();
		int [] idxValidProds = gpSet.getIdxValidProds(tokenSet, termSet, idxPossibleHead);

		if ( idxValidProds.length == 0 ) {
			return null; /* No valid production for this token set */
		}
		
		/* Valid productions found */
		/* Iterate through all possible productions */
		for (int i = 0; i < idxValidProds.length; ++i) {
			
			/* Iterate through all potential heads */
			for (int j = 0; j < idxPossibleHead.get(i).length; ++j) {
				
				ArrayList<CAbstractWrittenTokenSet> remainingSets = new ArrayList<CAbstractWrittenTokenSet>();
				Node n = gpSet.attempt(i, tokenSet, j, remainingSets);
				 
				if ( n != null ) {
					if ( n.isTerminal() ) {
						return n; /* Bottom out condition for recursion */
					}
					else {
						/* Call recursively */
						
						/* TODO: in case the head child of n is not a
						 * terminal, should parse further.
						 */
						
						int nValidChildren = 0;
						for (int k = 0; k < remainingSets.size(); ++k) {
							Node cn = parse(remainingSets.get(k));
							if ( cn != null ) {
								n.addChild(cn);
								nValidChildren++;
							}
						}
						
						if ( nValidChildren == remainingSets.size() )
							return n;
						else
							return null;
					}
				}
				
					
			}
		}
		
		return null;
	}

	
	/* Testing routine */
	public static void main(String [] args) {
		final String tokenSetFN = "C:\\Users\\scai\\Dropbox\\Plato\\data\\tokensets\\TS_2.wts";
		final String prodSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\productions.txt";
		final String termSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\terminals.txt";			
		
		/* Create written tkoen set */
		CWrittenTokenSetNoStroke wts = new CWrittenTokenSetNoStroke();
		try {
			wts.readFromFile(tokenSetFN);
		}
		catch ( FileNotFoundException fnfe ) {
			System.err.println(fnfe.getMessage());
		}
		catch ( IOException ioe ) {
			System.err.println(ioe.getMessage());
		}
		
		/* Create token set parser */		
		TokenSetParser tokenSetParser = new TokenSetParser(termSetFN, prodSetFN);
		
		Node parseRoot = tokenSetParser.parse(wts);
	}
}
