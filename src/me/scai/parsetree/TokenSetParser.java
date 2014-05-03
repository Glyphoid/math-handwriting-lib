package me.scai.parsetree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

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
	
	private float evalGeometry(CAbstractWrittenTokenSet tokenSet, 
			                   int [] idxValidProds, 
			                   ArrayList<int [][]> idxPossibleHead, 
			                   Node [][] nodes,
			                   float [][] maxGeomScores, 
			                   CAbstractWrittenTokenSet [][][] aRemainingSets) {
		for (int i = 0; i < idxValidProds.length; ++i) {
			nodes[i] = new Node[idxPossibleHead.get(i).length];
			maxGeomScores[i] = new float[idxPossibleHead.get(i).length];
			aRemainingSets[i] = new CAbstractWrittenTokenSet[idxPossibleHead.get(i).length][];
			
			/* Iterate through all potential heads */
			for (int j = 0; j < idxPossibleHead.get(i).length; ++j) {
				int [] idxHead = idxPossibleHead.get(i)[j];
				ArrayList<CAbstractWrittenTokenSet> remainingSets = new ArrayList<CAbstractWrittenTokenSet>();
				
				// DEBUG
				if ( i == 2 )
					i = i + 0;
				
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
		
		/* Get return value */
		int [] idxMax2 = MathHelper.indexMax2D(maxGeomScores); /* TODO: Resolve ties */
		float maxScore = maxGeomScores[idxMax2[0]][idxMax2[1]];
		
		/* Look for flags that indicate the need for further parsing
		 * and parse them further. Loop until all of them are gotten 
		 * rid of through recursive calls.
		 */
		while ( maxScore == GraphicalProduction.flagNTNeedsParsing ) {
			int i = idxMax2[0];
			int j = idxMax2[1];
			
			GraphicalProduction c_prod = gpSet.prods.get(idxValidProds[i]);
			String c_lhs = c_prod.rhs[0];			
			ArrayList<int [][]> c_idxPossibleHead = new ArrayList<int [][]>();
			int [] c_idxValidProds = gpSet.getIdxValidProds(tokenSet, termSet, c_lhs, c_idxPossibleHead);
			
			if ( c_idxValidProds == null || c_idxValidProds.length == 0 ) {
				maxGeomScores[i][j] = 0.0f; /* Necessary? */
			}
			else {
				Node [][] c_nodes = new Node[c_idxValidProds.length][];
				float [][] c_maxGeomScores = new float[c_idxValidProds.length][];
				CAbstractWrittenTokenSet [][][] c_aRemainingSets = new CAbstractWrittenTokenSet[c_idxValidProds.length][][];
				
				/* Recursive call */
				float c_maxScore = evalGeometry(tokenSet, c_idxValidProds, c_idxPossibleHead, 
					                            c_nodes, c_maxGeomScores, c_aRemainingSets);
				maxGeomScores[i][j] = c_maxScore;
			}
			
			/* Re-calculate the maximum */
			idxMax2 = MathHelper.indexMax2D(maxGeomScores);
			maxScore = maxGeomScores[idxMax2[0]][idxMax2[1]];
		}
		
		/* This solution may not be 100% bullet proof */
		return maxScore;
	}
	
	/* This implements a recursive descend parser */
	public Node parse(CAbstractWrittenTokenSet tokenSet, String lhs) {
		ArrayList<int [][]> idxPossibleHead = new ArrayList<int [][]>();
		/* Determine the name of the lhs */
		
		//DEBUG
		int nt = tokenSet.getNumTokens(); 
		String tStr = tokenSet.toString();
		if ( lhs.equals("MULTIPLICATION") ) {
			nt = nt + 0;
		}
		
		int [] idxValidProds = gpSet.getIdxValidProds(tokenSet, termSet, lhs, idxPossibleHead);
		
		if ( idxValidProds.length == 0 ) {
			return null; /* No valid production for this token set */
		}		
		
		/* Geometric evaluation */
		Node [][] nodes = new Node[idxValidProds.length][];
		float [][] maxGeomScores = new float[idxValidProds.length][];
		CAbstractWrittenTokenSet [][][] aRemainingSets = new CAbstractWrittenTokenSet[idxValidProds.length][][];
		
		evalGeometry(tokenSet, idxValidProds, idxPossibleHead, 
				     nodes, maxGeomScores, aRemainingSets);
		
		/* Select the maximum geometric score */
		int [] idxMax2 = MathHelper.indexMax2D(maxGeomScores);
		
		//n.setGeometricScore(maxGeomScores[idxMax2[0]][idxMax2[1]]);
		
		/* Search for ties */
		float maxScore = maxGeomScores[idxMax2[0]][idxMax2[1]];
		int [][] idxTieMax = MathHelper.findTies2D(maxGeomScores, maxScore);
		float [] childGeometricScores = new float[idxTieMax.length];
		
		/* Iterate through all members of the tie and find the best one */
		Node n;
		CAbstractWrittenTokenSet [] remSets;
		 
		if ( idxTieMax.length > 1 )
			n = null; //DEBUG
		
		for (int i = 0; i < idxTieMax.length; ++i) {
			int idx0 = idxTieMax[i][0];
			int idx1 = idxTieMax[i][1];
			
			n = nodes[idx0][idx1];
			remSets = aRemainingSets[idx0][idx1];
		
			if ( n.isTerminal() ) {
				childGeometricScores[i] = 1.0f;
				//return n; /* Bottom-out condition for recursion */
			}
			else {
				ArrayList<Float>  t_childGeometricScores = new ArrayList<Float>();
				/* Call recursively */
				
				/* In case the head child of n is a non-terminal (NT), it needs 
				 * to be parsed.
				 */
				/* Determine if the head child is an NT */
				String headChildType = gpSet.getRHS(idxValidProds[idx0])[0];
				boolean bHeadChildNT = !termSet.isTypeTerminal(headChildType); 
				if ( bHeadChildNT ) {
					int [] headChildTokenIdx = idxPossibleHead.get(idx0)[idx1];
					CAbstractWrittenTokenSet headChildTokenSet = new CWrittenTokenSetNoStroke(tokenSet, headChildTokenIdx);
					
					//DEBUG
					if ( headChildTokenSet.toString().equals("Token [5, +, -, 3]") && headChildType.equals("EXPR_LV1") )
						nt = nt + 0;
					if ( headChildTokenSet.toString().equals("Token [5, +, -, 3]") && headChildType.equals("ADDITION") )
						nt = nt + 0;
					if ( headChildTokenSet.toString().equals("Token [-]") && headChildType.equals("EXPR_LV2") )
						nt = nt + 0;
					if ( headChildTokenSet.toString().equals("Token [-]") && headChildType.equals("EXPR_LV1") )
						nt = nt + 0;
					if ( headChildTokenSet.toString().equals("Token [-]") && headChildType.equals("DECIMAL_NUMBER") )
						nt = nt + 0;
					
					/* Recursive call */
					n.ch[0] = parse(headChildTokenSet, headChildType); 
					
					if ( n.ch[0] != null )
						t_childGeometricScores.add(n.getGeometricScore());						
					else
						t_childGeometricScores.add(0.0f);
				}
				
				int nValidChildren = 0;
				for (int k = 0; k < remSets.length; ++k) {
					String requiredType = n.getRHSTypes()[n.ch.length];
					
					/* Recursive call */
					//DEBUG
					if ( remSets[k].toString().equals("Token [5, +, -, 3]") && requiredType.equals("EXPR_LV2") )
						nt = nt + 0;
					
					if ( remSets[k].toString().equals("Token [-]") && requiredType.equals("EXPR_LV2") )
						nt = nt + 0;
					
					Node cn = parse(remSets[k], requiredType);
					if ( cn != null ) {
						String actualType = cn.prodSumString.split(" ")[0];
						if ( requiredType.equals(actualType) ) {
							n.addChild(cn);
							nValidChildren++;
						}
					}
				}
				
				if ( nValidChildren == remSets.length ) {
					for (int h = 0; h < n.ch.length; ++h)
						t_childGeometricScores.add(n.ch[h].getGeometricScore());
					
					childGeometricScores[i] = MathHelper.arrayMean(t_childGeometricScores);
				}
				else {
//					return null;
					childGeometricScores[i] = 0.0f;
				}
			}
			
		}
		
		/* setGeometricScore() and return */
		if ( idxTieMax.length > 1 )
			n = null; //DEBUG
		
		/* What if there is still a tie? TODO */
		int idxBreakTie = MathHelper.indexMax(childGeometricScores); 
		int idx0 = idxTieMax[idxBreakTie][0];
		int idx1 = idxTieMax[idxBreakTie][1];
		
		n = nodes[idx0][idx1];
		n.setGeometricScore(maxGeomScores[idx0][idx1]);
		
		//DEBUG
		if ( n.prodSumString.equals("DECIMAL_NUMBER --> MINUS_OP DECIMAL_NUMBER") )
			nt = nt + 0;
		
		return n;
	}
	
	/* Testing routine */
	public static void main(String [] args) {
		/* TS_3: 34- (Grammatical error) TODO */
		/* TS_7: 345 (Geometric error: height difference too big) */
		/* TS_8: 69 (Geometric error: height difference too big) */
		/* TS_9: .28 (Geometric error: vertical alignment) */
		/* TS_5: 23 (Geometric error) */
		
		int [] tokenSetNums           = {1, 2, 4, 6, 9, 10, 
									     11, 12, 13, 14, 
				                         15, 18, 21, 22,                    
				                         27, 28, 29, 
				                         32, 34, 36, 37, 
				                         41, 42, 43, 44, 45, 
				                         48, 49,
				                         50, 51, 52, 53, 54, 55, 
				                         56, 57, 58, 59, 
				                         //60, 
				                         67, 68, 69, 70, 
				                         72, 73, 74, 75, 76, 
				                         83, 84, 85, 86, 88, 89, 
				                         90, 91};
		String [] tokenSetTrueStrings = {"12", "236", "77", "36", "-28", "(21 - 3)",  
							             "(21 + 3)", "(21 - 5)", "009", "900", 
										 "100", "(56 - 3)", "(29 / 3)", "--3", 
										 "(5 / 8)", "((5 / 8) / 9)", "(3 / (2 / 7))", 
										 "(1 - (2 / 3))", "(4 / (5 + (2 / 3)))", "(23 / 4)", "((5 + 9) / ((3 / 2) - 1))", 
										 "((4 - 2) / 3)", "((7 - 8) / 10)", "((3 + 1) / 4)", "(72 / 3)",  "((8 - 3) / 4)", 
										 "8.3", "4.0", 
									 	 "0.01", "-53", "-7.4", "(8.1 / 0.9)", "(-1 / -3.2)", "(-4.2 / (7 + 3))", 
									 	 "(5 * 3)", "(3 * 4)",  "(-2 * 8)", "(2 * -3)", 
									 	 //"(2 * +3)",
									 	 "2", "0", "1.20", "0.02", 
										 "-1", "-1.2", "-0.11", "-12", "-13.9", 
										 "(0 + 0)", "(1.3 + 4)", "(4 + 2.1)", "(2.0 + 1.1)", "(-1 + -3)", "(-3.0 + -1)", 
										 "((1 + 2) + 3)", "((2 - 3) - 4)"};		
		/* Single out for debugging */
		Integer [] singleOutIdx = {};
		/* Crash: 60: (2 * +3), superfluous plus sign
		 * Error: 91: (2 - 3) - 4 vs. 2 - (3 - 4) needs some sort of geometric biaser? */

		final String tokenSetPrefix = "C:\\Users\\scai\\Dropbox\\Plato\\data\\tokensets\\TS_";
		final String tokenSetSuffix = ".wts";
		final String prodSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\productions.txt";
		final String termSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\terminals.txt";
		
		/* Create written token set */
		CWrittenTokenSetNoStroke wts = new CWrittenTokenSetNoStroke();
		
		TokenSetParser tokenSetParser = new TokenSetParser(termSetFN, prodSetFN);
		
		/* Create token set parser */
		int nPass = 0;
		int nTested = 0;
		for (int i = 0; i < tokenSetNums.length; ++i) {
			/* Single out option */
			if ( singleOutIdx != null && singleOutIdx.length > 0 ) {
				List<Integer> singleOutList = Arrays.asList(singleOutIdx); 
				if ( !singleOutList.contains(tokenSetNums[i]) )
					continue;
			}
			
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
		
			/* Parse graphically */
			Node parseRoot = tokenSetParser.parse(wts, "ROOT");
			/* TODO: replace with parse(wts) */
			
			String stringized = ParseTreeStringizer.stringize(parseRoot);
			boolean checkResult = stringized.equals(tokenSetTrueStrings[i]);
			String checkResultStr = checkResult ? "PASS" : "FAIL";
			nPass += checkResult ? 1 : 0; 
			System.out.println("[" + checkResultStr + "] " + "File " + tokenSetNums[i] + ": " +
							   "\"" + stringized + "\"");
			
			nTested ++;
		}
		
		System.out.println("Tested: " + nTested + 
				           "; Passed: " + nPass + 
				           "; Failed: " + (nTested - nPass));
		
	}
}
