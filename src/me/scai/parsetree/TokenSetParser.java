package me.scai.parsetree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import me.scai.handwriting.CAbstractWrittenTokenSet;
import me.scai.handwriting.CWrittenTokenSetNoStroke;

public class TokenSetParser implements ITokenSetParser {
	protected TerminalSet termSet = null;
	protected GraphicalProductionSet gpSet = null;
	
	/* Properties */
	private int drillDepthLimit = Integer.MAX_VALUE; 	/* No limit on levels of recursive drill */
//	private int drillDepthLimit = 3;	/* Limiting it to a specific number runs without errors, but may cause wrong parsing */
	private int currDrillDepth = 0;	/* Thread-safe? */
	
	private boolean bDebug = false;
	private boolean bDebug2 = false;
	private boolean bUseHashMaps = false;		/* For getIdxValidProds() */
	private boolean bUseHashMaps2 = true;		/* For evalGeometry() */
	
	/* Temporary variables for parsing */
	private HashMap<String, int []> tokenSetLHS2IdxValidProdsMap;
	private HashMap<String, ArrayList<int [][]>> tokenSetLHS2IdxPossibleHeadsMap;
	
	private HashMap<String, Float> evalGeom2MaxScoreMap;
	private HashMap<String, Node [][]> evalGeom2NodesMap;
	private HashMap<String, float [][]> evalGeom2ScoresMap;
	private HashMap<String, CAbstractWrittenTokenSet [][][]> evalGeom2RemSetsMap;
	private HashMap<String, int []> evalGeom2IdxMaxMap;
	
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
	
	public void setDebug(boolean t_bDebug) {
		bDebug = t_bDebug;
	}
	
	public void init() {
		tokenSetLHS2IdxValidProdsMap = new HashMap<String, int []>();
		tokenSetLHS2IdxPossibleHeadsMap = new HashMap<String, ArrayList<int [][]>>();
		
		evalGeom2MaxScoreMap = new HashMap<String, Float>();
		evalGeom2NodesMap = new HashMap<String, Node [][]>();
		evalGeom2ScoresMap = new HashMap<String, float [][]>();
		evalGeom2RemSetsMap = new HashMap<String, CAbstractWrittenTokenSet [][][]>();
		evalGeom2IdxMaxMap = new HashMap<String, int []>();
	}
	
	@Override
	public Node parse(CAbstractWrittenTokenSet tokenSet) {
		init();
		
		return parse(tokenSet, "ROOT");
	}
	
	private float evalGeometry(CAbstractWrittenTokenSet tokenSet, 
			                   int [] idxValidProds, 
			                   ArrayList<int [][]> idxPossibleHead, 
			                   Node [][] nodes, 
			                   float [][] maxGeomScores, 
			                   CAbstractWrittenTokenSet [][][] aRemainingSets, 
			                   int [] idxMax) {
		final boolean bDebug = false;
		
		String tHashKey = tokenSet.toString() + "@" + MathHelper.intArray2String(idxValidProds);
		
		if ( this.bUseHashMaps2 && evalGeom2MaxScoreMap.containsKey(tHashKey) ) {
			if ( this.bDebug2 )
				System.out.println("Hash map contains key: " + tHashKey);
			
			Node [][] r_nodes = evalGeom2NodesMap.get(tHashKey);
			for (int i = 0; i < r_nodes.length; ++i)
				nodes[i] = r_nodes[i];
			
			float [][] r_maxGeomScores = evalGeom2ScoresMap.get(tHashKey);
			for (int i = 0; i < r_nodes.length; ++i)
				maxGeomScores[i] = r_maxGeomScores[i];
				
			CAbstractWrittenTokenSet [][][] r_aRemainingSets = evalGeom2RemSetsMap.get(tHashKey);
			for (int i = 0; i < r_aRemainingSets.length; ++i)
				aRemainingSets[i] = r_aRemainingSets[i];
			
			int [] r_idxMax = evalGeom2IdxMaxMap.get(tHashKey);
			for (int i = 0; i < r_idxMax.length; ++i)
				idxMax[i] = r_idxMax[i];
			
			return evalGeom2MaxScoreMap.get(tHashKey);
		}
		
		if ( this.bDebug2 )
			System.out.println("evalGeometry: " + tHashKey);
		
		for (int i = 0; i < idxValidProds.length; ++i) {
			int nrhs =  gpSet.prods.get(idxValidProds[i]).rhs.length;
			/* Number of right-hand size elements, including the head */
					
			nodes[i] = new Node[idxPossibleHead.get(i).length];
			maxGeomScores[i] = new float[idxPossibleHead.get(i).length];
			aRemainingSets[i] = new CAbstractWrittenTokenSet[idxPossibleHead.get(i).length][];
			
			/* Iterate through all potential heads */
			for (int j = 0; j < idxPossibleHead.get(i).length; ++j) {
				int [] idxHead = idxPossibleHead.get(i)[j];
				
				//ArrayList<CAbstractWrittenTokenSet> AL_remainingSets = new ArrayList<CAbstractWrittenTokenSet>(); //PerfTweak old
				CAbstractWrittenTokenSet [] remainingSets = new CAbstractWrittenTokenSet[nrhs - 1];
				
				float [] maxGeomScore = new float[1];
				
				if ( idxHead.length == 0 ) {
					/* The head must not be an empty */
//					throw new RuntimeException("TokenSetParser.evalGeometry encountered empty idxHead");
					nodes[i][j] = null;
					maxGeomScores[i][j] = 0.0f;
				}
				else {
//					Node n = gpSet.attempt(idxValidProds[i], tokenSet, idxHead, AL_remainingSets, remainingSets, maxGeomScore);		//PerfTweak old
					Node n = gpSet.attempt(idxValidProds[i], tokenSet, idxHead, remainingSets, maxGeomScore);		//PerfTweak old
					
					if ( remainingSets.length == 1 && remainingSets[0] == null )
						remainingSets = new CAbstractWrittenTokenSet[0];	/* TODO: Get rid of this illogical-looking statement */
					
//					if ( remainingSets.size() > nrhs - 1 ) //DEBUG //PerfTweak old
//						System.err.println("Incorrect number of elements in remainingSets"); //DEBUG
					
					/* If the head child is a terminal, replace tokenName with the actual name of the token */
					if ( n != null && termSet.isTypeTerminal(n.ch[0].termName) )
						n.ch[0].termName = tokenSet.recogWinners.get(idxPossibleHead.get(i)[j][0]);
					
					if ( currDrillDepth < drillDepthLimit 
					     && maxGeomScore[0] != 0.0f 
					     && nrhs > 1 ) {
						/* Drill one level down: get the maximum geometric scores from its children */
						float [] d_scores = new float[nrhs];
						for (int k = 0; k < nrhs; ++k) {
							/* Iterate through all rhs items, including the head and the non-heads. */							
							
							ArrayList<int [][]> d_idxPossibleHead = new ArrayList<int [][]>();
								
							String d_lhs;
							d_lhs = gpSet.prods.get(idxValidProds[i]).rhs[k];
							
							if ( d_lhs.equals(TerminalSet.epsString) ) {
								d_scores[k] = 1.0f;	/* Is this correct? TODO */
								continue;
							}
													
							if ( termSet.isTypeTerminal(d_lhs) ) {
								int nTokens;
								if ( k == 0 )
									nTokens = idxHead.length;
								else
									//nTokens = remainingSets.get(k - 1).nTokens(); //PerfTweak old
									nTokens = remainingSets[k - 1].nTokens();
								
								if ( nTokens == 1 )
									d_scores[k] = 1.0f; 
								else
									d_scores[k] = 0.0f;
								
								continue;
							}
							
							CAbstractWrittenTokenSet d_tokenSet;
							if ( k == 0 ) {
								/* Head */
								CWrittenTokenSetNoStroke d_tokenSetNoStroke = new CWrittenTokenSetNoStroke();
								d_tokenSetNoStroke.setTokenNames(tokenSet.getTokenNames());
					    		
					    		for (int m = 0; m < idxHead.length; ++m) { 	/* TODO: Wrap in a new constructor */
					    			int irt = idxHead[m];
					    			
					    			d_tokenSetNoStroke.addToken(tokenSet.getTokenBounds(irt), 
					    					            		tokenSet.recogWinners.get(irt), 
					    					            		tokenSet.recogPs.get(irt), 
					    					            		false);
					    			/* The last input argument sets bCheck to false for speed */
					    			/* Is this a dangerous action? */
					    		}
					    		
								d_tokenSet = d_tokenSetNoStroke; /* Automatic upcast */
							}
							else {
								/* Non-head */
								//d_tokenSet = remainingSets.get(k - 1);	//PerfTweak old;
								d_tokenSet = remainingSets[k - 1];
							}
							
							if ( bDebug )
								System.out.println("Drilling down from level " + currDrillDepth + 
									           	   " to level " + (currDrillDepth + 1) + ": " +
									           	   gpSet.prods.get(idxValidProds[i]).lhs + 
									               " --> " + d_lhs);
						
							int [] d_idxValidProds = null;
							if ( bUseHashMaps ) {
								String hashKey = d_tokenSet.toString() + "@" + d_lhs;
								if ( !tokenSetLHS2IdxValidProdsMap.containsKey(hashKey) ) {								
									d_idxValidProds = gpSet.getIdxValidProds(d_tokenSet, termSet, d_lhs, d_idxPossibleHead, this.bDebug);
									
									/* Store results in hash maps */
									tokenSetLHS2IdxValidProdsMap.put(hashKey, d_idxValidProds);
									tokenSetLHS2IdxPossibleHeadsMap.put(hashKey, d_idxPossibleHead);
								}
								else {
									if ( this.bDebug ) 
										System.out.println("Hash map getting: " + hashKey);
									/* Retrieve results from hash maps */
									d_idxValidProds = tokenSetLHS2IdxValidProdsMap.get(hashKey);
									d_idxPossibleHead = tokenSetLHS2IdxPossibleHeadsMap.get(hashKey);
								}
							}
							else {
								d_idxValidProds = gpSet.getIdxValidProds(d_tokenSet, termSet, d_lhs, d_idxPossibleHead, this.bDebug);
							}
							
							if ( d_idxValidProds.length == 0 ) {
								d_scores[k] = 0.0f;
								continue;
							}
							
							Node [][] d_nodes = new Node[d_idxValidProds.length][];
							float [][] d_c_maxGeomScores = new float[d_idxValidProds.length][];
							CAbstractWrittenTokenSet [][][] d_aRemainingSets = new CAbstractWrittenTokenSet[d_idxValidProds.length][][];
							int [] d_t_idxMax2 = new int[2];
														
							currDrillDepth++; /* To check: thread-safe? */		
							float d_maxGeomScore = evalGeometry(d_tokenSet, d_idxValidProds, d_idxPossibleHead, 
																d_nodes, d_c_maxGeomScores, d_aRemainingSets, d_t_idxMax2);
							currDrillDepth--;
							d_scores[k] = d_maxGeomScore; 
							/* Is this right? Or should maxGeomScore be multiplied by the geometric mean of the d_maxGeomScores's? TODO */
							
						}
						
						maxGeomScore[0] *= MathHelper.geometricMean(d_scores);
					}
					
					nodes[i][j] = n;
					maxGeomScores[i][j] = maxGeomScore[0];
	
//					aRemainingSets[i][j] = new CAbstractWrittenTokenSet[AL_remainingSets.size()]; 	//PerfTweak old key
//					AL_remainingSets.toArray(aRemainingSets[i][j]);		//PerfTweak old key
					aRemainingSets[i][j] = remainingSets;	//PerfTweak new key
				}
				
			}
		}
		
		/* Get return value */
		int [] idxMax2 = MathHelper.indexMax2D(maxGeomScores); /* TODO: Resolve ties */
		float maxScore = maxGeomScores[idxMax2[0]][idxMax2[1]];
		
		/* Look for flags that indicate the need for further parsing
		 * and parse them further. Loop until all of them are gotten 
		 * rid of through recursive calls.
		 */
		/* We probably don't need to store all the aRemainingSets.
		 * This can probably reduce the number of GC and speed things up. 
		 * Make this an option? */
		while ( maxScore == GraphicalProduction.flagNTNeedsParsing ) {
			int i = idxMax2[0];
			int j = idxMax2[1];
			
			GraphicalProduction c_prod = gpSet.prods.get(idxValidProds[i]);
			String c_lhs = c_prod.rhs[0];			
			ArrayList<int [][]> c_idxPossibleHead = new ArrayList<int [][]>();
			
			int [] c_idxValidProds = null;
			if ( bUseHashMaps ) {
				String hashKey = tokenSet.toString() + "@" + c_lhs;
				if ( !tokenSetLHS2IdxValidProdsMap.containsKey(tokenSet.toString()) ) {
					c_idxValidProds = gpSet.getIdxValidProds(tokenSet, termSet, c_lhs, c_idxPossibleHead, this.bDebug);
					                  
					/* Store results in hash maps */
					tokenSetLHS2IdxValidProdsMap.put(hashKey, c_idxValidProds);
					tokenSetLHS2IdxPossibleHeadsMap.put(hashKey, c_idxPossibleHead);
				}
				else {
					if ( this.bDebug )
						System.out.println("Hash map getting: " + hashKey);
					
					/* Retrieve results from hash maps */
					c_idxValidProds = tokenSetLHS2IdxValidProdsMap.get(hashKey);
					c_idxPossibleHead = tokenSetLHS2IdxPossibleHeadsMap.get(hashKey);
				}
			}
			else {
				c_idxValidProds = gpSet.getIdxValidProds(tokenSet, termSet, c_lhs, c_idxPossibleHead, this.bDebug);
				
				//DEBUG
//				System.out.println("Found " + c_idxValidProds.length + " valid productions and " 
//						           + c_idxPossibleHead.size() + " valid possible heads");
				//~DEBUG
			}
			
			if ( c_idxValidProds == null || c_idxValidProds.length == 0 ) {
				maxGeomScores[i][j] = 0.0f; /* Necessary? */
			}
			else {
				Node [][] c_nodes = new Node[c_idxValidProds.length][];
				float [][] c_maxGeomScores = new float[c_idxValidProds.length][];
				CAbstractWrittenTokenSet [][][] c_aRemainingSets = new CAbstractWrittenTokenSet[c_idxValidProds.length][][];
				
				/* Recursive call */				
				int [] c_idxMax2 = new int[2];
				float c_maxScore = evalGeometry(tokenSet, c_idxValidProds, c_idxPossibleHead, 
					                            c_nodes, c_maxGeomScores, c_aRemainingSets, c_idxMax2);
				
				maxGeomScores[i][j] = c_maxScore;
				
//				GraphicalProduction gp = gpSet.prods.get(i);
			}
			
			/* Re-calculate the maximum */
			idxMax2 = MathHelper.indexMax2D(maxGeomScores);
			maxScore = maxGeomScores[idxMax2[0]][idxMax2[1]];
		}
		
		/* This solution may not be 100% bullet proof. TODO */
		if ( idxMax.length != 2 )
			throw new RuntimeException("idxMax does not have a length of 2");
		
		idxMax[0] = idxMax2[0];
		idxMax[1] = idxMax2[1];
		
		
		/* Optional: store result in hash map */
		if ( this.bUseHashMaps2 ) {
			evalGeom2MaxScoreMap.put(tHashKey, maxScore);
			evalGeom2NodesMap.put(tHashKey, nodes);
			evalGeom2ScoresMap.put(tHashKey, maxGeomScores);
			evalGeom2RemSetsMap.put(tHashKey, aRemainingSets);
			evalGeom2IdxMaxMap.put(tHashKey, idxMax);
		}
		
		return maxScore;
	}
	
	/* This implements a recursive descend parser */
	private Node parse(CAbstractWrittenTokenSet tokenSet, String lhs) {
		/* Input sanity check */		
		if ( tokenSet == null )
			System.err.println("Parsing null token set!");
		
		final boolean bTentative = true; // DEBUG
		
		ArrayList<int [][]> idxPossibleHead = new ArrayList<int [][]>();
		/* Determine the name of the lhs */
		
//		int nt = tokenSet.getNumTokens(); // DEBUG
//		if ( lhs.equals("EXPONENTIATION") ) { // DEBUG
//			nt = nt + 0; // DEBUG
//		}  // DEBUG
		
		int [] idxValidProds = null;
		if ( bUseHashMaps ) {
			String hashKey = tokenSet.toString() + "@" + lhs;
			if ( !tokenSetLHS2IdxValidProdsMap.containsKey(hashKey) ) {
				idxValidProds = gpSet.getIdxValidProds(tokenSet, termSet, lhs, idxPossibleHead, this.bDebug);
				
				/* Store results in hash maps */
				tokenSetLHS2IdxValidProdsMap.put(hashKey, idxValidProds);
				tokenSetLHS2IdxPossibleHeadsMap.put(hashKey, idxPossibleHead);
			}
			else {
				if ( this.bDebug )
					System.out.println("Hash map getting: " + hashKey);
				
				/* Retrieve results from hash maps */
				idxValidProds = tokenSetLHS2IdxValidProdsMap.get(hashKey);
				idxPossibleHead = tokenSetLHS2IdxPossibleHeadsMap.get(hashKey);
			}
		}
		else {
			//DEBUG
//			if ( lhs.equals("EXPONENTIATION") )
//				lhs = lhs;
			//~DEBUG
			idxValidProds = gpSet.getIdxValidProds(tokenSet, termSet, lhs, idxPossibleHead, this.bDebug);
		}
		
		if ( idxValidProds.length == 0 ) {
			return null; /* No valid production for this token set */
		}
		
		/* Geometric evaluation */
		Node [][] nodes = new Node[idxValidProds.length][];
		float [][] maxGeomScores = new float[idxValidProds.length][];
		CAbstractWrittenTokenSet [][][] aRemainingSets = new CAbstractWrittenTokenSet[idxValidProds.length][][];
		
		int [] t_idxMax2 = new int[2];
		currDrillDepth = 0;
		evalGeometry(tokenSet, idxValidProds, idxPossibleHead, 
				     nodes, maxGeomScores, aRemainingSets, t_idxMax2);
		
		/* Select the maximum geometric score */		
		int [] idxMax2 = MathHelper.indexMax2D(maxGeomScores);
		
		//n.setGeometricScore(maxGeomScores[idxMax2[0]][idxMax2[1]]);
		
		/* Search for ties */
		float maxScore = maxGeomScores[idxMax2[0]][idxMax2[1]];
		if ( maxScore == 0.0 )
			return null;
		
		int [][] idxTieMax = MathHelper.findTies2D(maxGeomScores, maxScore);
		float [] childGeometricScores = new float[idxTieMax.length];
		
		/* Iterate through all members of the tie and find the best one */
		Node n;
		CAbstractWrittenTokenSet [] remSets;
		 
//		if ( idxTieMax.length > 1 )
//			n = null; //DEBUG
		
		for (int i = 0; i < idxTieMax.length; ++i) {
			int idx0 = idxTieMax[i][0];
			int idx1 = idxTieMax[i][1];
			
			n = nodes[idx0][idx1];
			remSets = aRemainingSets[idx0][idx1];
			
			// Find out how many valid token sets there are in remSets
			int nValidRemSets = 0;
			if ( remSets != null ) {
				nValidRemSets = remSets.length - 1;		//PerfTweak new
				while ( nValidRemSets >= 0 && remSets[nValidRemSets] == null )
					nValidRemSets--;
				nValidRemSets++;
			}
			
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
//					if ( headChildTokenSet.toString().equals("Token [5, +, -, 3]") && headChildType.equals("EXPR_LV1") )
//						nt = nt + 0;
					
					/* Recursive call */
					n.ch[0] = parse(headChildTokenSet, headChildType); 
					
					if ( n.ch[0] != null )
						t_childGeometricScores.add(n.getGeometricScore());						
					else
						t_childGeometricScores.add(0.0f);
				}
				
				int nValidChildren = 0;
//				for (int k = 0; k < remSets.length; ++k) { //PerfTweak old
				for (int k = 0; k < nValidRemSets; ++k) { //PerfTweak old
					String requiredType = n.getRHSTypes()[n.ch.length];
					
					/* Recursive call */
					//DEBUG
//					if ( remSets[k].toString().equals("Token [5, +, -, 3]") && requiredType.equals("EXPR_LV2") )
//						nt = nt + 0;
//					
//					if ( remSets[k].toString().equals("Token [-]") && requiredType.equals("EXPR_LV2") )
//						nt = nt + 0;
					
					Node cn = parse(remSets[k], requiredType);		/* Recursive call */
					if ( cn != null ) {
						nValidChildren++;
						
						String actualType = cn.prodSumString.split(" ")[0];
						if ( requiredType.equals(actualType) ) {
							n.addChild(cn);
						}
					}
						
				}
				
				if ( nValidChildren == remSets.length ) {
					for (int h = 0; h < n.ch.length; ++h) {
						if ( !bTentative ) {
							t_childGeometricScores.add(n.ch[h].getGeometricScore());
						}
						else {
							if ( h < n.ch.length && n.ch[h] != null ) {
								if ( n.ch[h].isTerminal )
									t_childGeometricScores.add(1.0f);
								else
									t_childGeometricScores.add(n.ch[h].getGeometricScore()); // Why is it zero? DEBUG
							}
							else {
								t_childGeometricScores.add(0.0f);
							}
						}
					}
					
					childGeometricScores[i] = MathHelper.arrayMean(t_childGeometricScores);
				}
				else {
//					return null;
					childGeometricScores[i] = 0.0f;
				}
			}
			
		}
		
		/* setGeometricScore() and return */
//		if ( idxTieMax.length > 1 )
//			n = null; //DEBUG
		
		/* What if there is still a tie? TODO */
		int idxBreakTie = MathHelper.indexMax(childGeometricScores); 
		
		if ( bTentative ) {
			float maxChildGeometricScore = childGeometricScores[idxBreakTie];
			if ( maxChildGeometricScore == 0.0f ) {				
				return null;
			}
		}
		
		int idx0 = idxTieMax[idxBreakTie][0];
		int idx1 = idxTieMax[idxBreakTie][1];
		
		n = nodes[idx0][idx1];
		n.setGeometricScore(maxGeomScores[idx0][idx1]);
		
		return n;
	}
	
	/* Testing routine */
	public static void main(String [] args) {
		/* TS_3: 34- (Grammatical error) TODO */
		/* TS_7: 345 (Geometric error: height difference too big) */
		/* TS_8: 69 (Geometric error: height difference too big) */
		/* TS_9: .28 (Geometric error: vertical alignment) */
		/* TS_5: 23 (Geometric error) */
		
		final String errStr = ParseTreeStringizer.parsingErrString;
		
		int [] tokenSetNums           = {1, 2, 4, 6, 9, 10, 
									     11, 12, 13, 14, 
				                         15, 18, 21, 22, 
				                         23, 24, 103, 104, 106,	107, 108,		/* Exponentiation */
				                         27, 28, 29, 
				                         32, 34, 36, 37, 
				                         41, 42, 43, 44, 45, 
				                         48, 49,
				                         50, 51, 52, 53, 54, 55, 
				                         56, 57, 58, 59, 
				                         60, 
				                         67, 68, 69, 70, 
				                         72, 73, 74, 75, 76, 
				                         83, 84, 85, 86, 88, 89, 
				                         90, 91, 100, 101, 
				                         98, 99}; /* Begins token sets with syntax errors */
		String [] tokenSetTrueStrings = {"12", "236", "77", "36", "-28", "(21 - 3)",  
							             "(21 + 3)", "(21 - 5)", "009", "900", 
										 "100", "(56 - 3)", "(29 / 3)", "--3", 
										 "(9 ^ 3)", "(2 ^ -3)", "(68 ^ 75)", "(2 ^ 34)", "(258 ^ 76)", "(256 ^ 481)", "(289 ^ 643)", /* Exponentiation */
										 "(5 / 8)", "((5 / 8) / 9)", "(3 / (2 / 7))", 
										 "(1 - (2 / 3))", "(4 / (5 + (2 / 3)))", "(23 / 4)", "((5 + 9) / ((3 / 2) - 1))", 
										 "((4 - 2) / 3)", "((7 - 8) / 10)", "((3 + 1) / 4)", "(72 / 3)",  "((8 - 3) / 4)", 
										 "8.3", "4.0", 
									 	 "0.01", "-53", "-7.4", "(8.1 / 0.9)", "(-1 / -3.2)", "(-4.2 / (7 + 3))", 
									 	 "(5 * 3)", "(3 * 4)",  "(-2 * 8)", "(2 * -3)", 
									 	 "(2 * +3)",
									 	 "2", "0", "1.20", "0.02", 
										 "-1", "-1.2", "-0.11", "-12", "-13.9", 
										 "(0 + 0)", "(1.3 + 4)", "(4 + 2.1)", "(2.0 + 1.1)", "(-1 + -3)", "(-3.0 + -1)", 
										 "((1 + 2) + 3)", "((2 - 3) - 4)", "-3", "+3",  
										 errStr, errStr};

		/* Single out for debugging */
//		Integer [] singleOutIdx = {107};
		Integer [] singleOutIdx = {};
		/* Crash: 
		 * Error: 104: "((2 ^ 3) ^ 4)" <>  "(2 ^ 34)". Need to change Production: DIGIT_STRING --> DIGIT DIGIT STRING
		 *        91: (2 - 3) - 4 vs. 2 - (3 - 4) needs some sort of geometric biaser? */

		final String tokenSetPrefix = "C:\\Users\\scai\\Dropbox\\Plato\\data\\tokensets\\TS_";
		final String tokenSetSuffix = ".wts";
		final String prodSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\productions.txt";
		final String termSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\terminals.txt";
		
		/* Create written token set */
		CWrittenTokenSetNoStroke wts = new CWrittenTokenSetNoStroke();
		
		TokenSetParser tokenSetParser = new TokenSetParser(termSetFN, prodSetFN);
		
//		if ( singleOutIdx.length == 1 )
//			tokenSetParser.setDebug(false);
//		else
//			tokenSetParser.setDebug(false);
		
		/* Create token set parser */
		int nPass = 0;
		int nTested = 0;
		long totalParsingTime_ms = 0;
		
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
			//Node parseRoot = tokenSetParser.parse(wts, "ROOT");
			long millis_0 = System.currentTimeMillis();
						
			Node parseRoot = tokenSetParser.parse(wts);	/* Parsing action */
			
			long millis_1 = System.currentTimeMillis();
			
			long parsingTime = millis_1 - millis_0;
			totalParsingTime_ms += parsingTime; 
			
			String stringized = ParseTreeStringizer.stringize(parseRoot);
			boolean checkResult = stringized.equals(tokenSetTrueStrings[i]);
			String checkResultStr = checkResult ? "PASS" : "FAIL";
			nPass += checkResult ? 1 : 0; 
			
			String strPrint = "[" + checkResultStr + "] "
					          + "(" + parsingTime + " ms) " 
			                  + "File " + tokenSetNums[i] + ": " 
					          + "\"" + stringized + "\"";
			if ( !checkResult )
				strPrint += " <> " + " \"" + tokenSetTrueStrings[i] + "\"";
			
			if ( checkResult ) {
				System.out.println(strPrint);
				System.out.flush();
			}
			else {
				System.err.println(strPrint);
				System.err.flush();
			}
			
			nTested ++;
		}
		
		System.out.println("Tested: " + nTested + 
				           "; Passed: " + nPass + 
				           "; Failed: " + (nTested - nPass));
		System.out.println("Total parsing time = " + totalParsingTime_ms + " ms");
		
	}
}
