package me.scai.parsetree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import me.scai.handwriting.CAbstractWrittenTokenSet;
import me.scai.handwriting.CWrittenTokenSetNoStroke;

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
	
	
	@Override
	public Node parse(CAbstractWrittenTokenSet tokenSet) {
		return parse(tokenSet, null);
	}
	
	/* This implements a recursive descend parser */
	public Node parse(CAbstractWrittenTokenSet tokenSet, String lhs) {
		ArrayList<int [][]> idxPossibleHead = new ArrayList<int [][]>();
		/* Determine the name of the lhs */
		
		int [] idxValidProds = gpSet.getIdxValidProds(tokenSet, termSet, lhs, idxPossibleHead);
		

		if ( idxValidProds.length == 0 ) {
			return null; /* No valid production for this token set */
		}		
		
		/* Valid productions found */
		/* Iterate through all possible productions */
		
		Node [][] nodes = new Node[idxValidProds.length][];
		float [][] maxGeomScores = new float[idxValidProds.length][];
		CAbstractWrittenTokenSet [][][] aRemainingSets = new CAbstractWrittenTokenSet[idxValidProds.length][][];
		for (int i = 0; i < idxValidProds.length; ++i) {
			nodes[i] = new Node[idxPossibleHead.get(i).length];
			maxGeomScores[i] = new float[idxPossibleHead.get(i).length];
			aRemainingSets[i] = new CAbstractWrittenTokenSet[idxPossibleHead.get(i).length][];
			
			/* Iterate through all potential heads */
			for (int j = 0; j < idxPossibleHead.get(i).length; ++j) {
				int [] idxHead = idxPossibleHead.get(i)[j];
				ArrayList<CAbstractWrittenTokenSet> remainingSets = new ArrayList<CAbstractWrittenTokenSet>();
				
				// DEBUG
//				if ( i == 1 && idxHead == 0 )
//					i = i + 0;
				
				float [] maxGeomScore = new float[1];
				Node n = gpSet.attempt(idxValidProds[i], tokenSet, idxHead, remainingSets, maxGeomScore);
				/* If the head child is a terminal, replace tokenName with the actual name of the token */
				if ( n != null && termSet.isTypeTerminal(n.ch[0].termName) )
					n.ch[0].termName = tokenSet.recogWinners.get(idxPossibleHead.get(i)[j][0]);

				nodes[i][j] = n;
				maxGeomScores[i][j] = maxGeomScore[0];

				aRemainingSets[i][j] = new CAbstractWrittenTokenSet[remainingSets.size()];
				remainingSets.toArray(aRemainingSets[i][j]);
				
			}
		}
		
		/* Select the maximum geometric score */
		int [] idxMax2 = MathHelper.indexMax2D(maxGeomScores); /* TODO: Resolve ties */
		Node n = nodes[idxMax2[0]][idxMax2[1]];
		CAbstractWrittenTokenSet [] remSets = aRemainingSets[idxMax2[0]][idxMax2[1]];
		
		if ( n.isTerminal() ) {
			return n; /* Bottom-out condition for recursion */
		}
		else {
			/* Call recursively */			
			
			/* In case the head child of n is a non-terminal (NT), it needs 
			 * to be parsed.
			 */
			/* Determine if the head child is an NT */
			String headChildType = gpSet.getRHS(idxValidProds[idxMax2[0]])[0];
			boolean bHeadChildNT = !termSet.isTypeTerminal(headChildType); 
			if ( bHeadChildNT ) {
				int [] headChildTokenIdx = idxPossibleHead.get(idxMax2[0])[idxMax2[1]];
				CAbstractWrittenTokenSet headChildTokenSet = new CWrittenTokenSetNoStroke(tokenSet, headChildTokenIdx);
				
				n.ch[0] = parse(headChildTokenSet, headChildType);
			}
			
			int nValidChildren = 0;
			for (int k = 0; k < remSets.length; ++k) {
				String requiredType = n.getRHSTypes()[n.ch.length];
				Node cn = parse(remSets[k], requiredType);
				if ( cn != null ) {
					String actualType = cn.prodSumString.split(" ")[0];
					if ( requiredType.equals(actualType) ) {
						n.addChild(cn);
						nValidChildren++;
					}
//					else {
//						return null;
//					}
				}
			}
			
			if ( nValidChildren == remSets.length )
				return n;
			else
				return null;
		}

	}

	
	/* Testing routine */
	public static void main(String [] args) {
		/* TS_3: 34- (Grammatical error) TODO */
		/* TS_7: 345 (Geometric error: height difference too big) */
		/* TS_8: 69 (Geometric error: height difference too big) */
		/* TS_9: .28 (Geometric error: vertical alignment) */
		/* TS_5: 23 (Geometric error) */
		int [] tokenSetNums           = {1, 2, 4, 6, 10, 13, 
				                         15, 27, 36, 48, 49,
				                         50, 67, 68, 69, 70, 
				                         72, 73, 74, 75, 76};
		String [] tokenSetTrueStrings = {"12", "236", "77", "36", "(21 - 3)", "009", 
										 "100", "(5 / 8)", "(23 / 4)", "8.3", "4.0", 
										 "0.01", "2", "0", "1.20", "0.02", 
										 "-1", "-1.2", "-0.11", "-12", "-13.9"};

//		int [] tokenSetNums = {76};
//		String [] tokenSetTrueStrings = {"(21 - 3)"};
		
		final String tokenSetPrefix = "C:\\Users\\scai\\Dropbox\\Plato\\data\\tokensets\\TS_";
		final String tokenSetSuffix = ".wts";
		final String prodSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\productions.txt";
		final String termSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\terminals.txt";
		
		/* Create written token set */
		CWrittenTokenSetNoStroke wts = new CWrittenTokenSetNoStroke();
		
		TokenSetParser tokenSetParser = new TokenSetParser(termSetFN, prodSetFN);
		
		/* Create token set parser */
		for (int i = 0; i < tokenSetNums.length; ++i) {
			String tokenSetFN = tokenSetPrefix + tokenSetNums[i] + tokenSetSuffix;
			try {
				wts.readFromFile(tokenSetFN);
			}
			catch ( FileNotFoundException fnfe ) {
				System.err.println(fnfe.getMessage());
			}
			catch ( IOException ioe ) {
				System.err.println(ioe.getMessage());
			}
		
			Node parseRoot = tokenSetParser.parse(wts, null);
			
			String stringized = ParseTreeStringizer.stringize(parseRoot);
			boolean checkResult = stringized.equals(tokenSetTrueStrings[i]);
			String checkResultStr = checkResult ? "PASS" : "FAIL";
			System.out.println("File " + tokenSetNums[i] + ": " +
							   "\"" + stringized + "\"" + "\t[" + checkResultStr + "]");
		}
		
	}
}
