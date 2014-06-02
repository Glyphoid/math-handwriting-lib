package me.scai.parsetree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.HashMap;


//import me.scai.handwriting.CAbstractWrittenTokenSet;
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
	private final boolean bUseHashMaps = true;		/* For getIdxValidProds() */
	private final boolean bUseHashMaps2 = true;		/* For evalGeometry() */
	
	/* Temporary variables for parsing */
	private HashMap<String, int []> tokenSetLHS2IdxValidProdsMap;
	private HashMap<String, ArrayList<int [][]>> tokenSetLHS2IdxPossibleHeadsMap;
	private HashMap<String, Integer> tokenSetLHS2IdxBestProdMap;
	
	private HashMap<String, Float> evalGeom2MaxScoreMap;
	private HashMap<String, Node [][]> evalGeom2NodesMap;
	private HashMap<String, float [][]> evalGeom2ScoresMap;
	private HashMap<String, CWrittenTokenSetNoStroke [][][]> evalGeom2RemSetsMap;
//	private HashMap<String, int []> evalGeom2IdxMaxMap;
	
	/* DEBUG variables */
	private int nCallEvalGeometry;
	
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
		nCallEvalGeometry = 0;
		
		tokenSetLHS2IdxValidProdsMap = new HashMap<String, int []>();
		tokenSetLHS2IdxPossibleHeadsMap = new HashMap<String, ArrayList<int [][]>>();
		tokenSetLHS2IdxBestProdMap = new HashMap<String, Integer>();
		
		evalGeom2MaxScoreMap = new HashMap<String, Float>();
		evalGeom2NodesMap = new HashMap<String, Node [][]>();
		evalGeom2ScoresMap = new HashMap<String, float [][]>();
		evalGeom2RemSetsMap = new HashMap<String, CWrittenTokenSetNoStroke [][][]>();
//		evalGeom2IdxMaxMap = new HashMap<String, int []>();
	}
	
	@Override
	public Node parse(CWrittenTokenSetNoStroke tokenSet) {
		init();
		
		return parse(tokenSet, "ROOT");
	}
	
	private float evalGeometry(CWrittenTokenSetNoStroke tokenSet,
			                   int [] idxValidProds, 
			                   ArrayList<int [][]> idxPossibleHead, 
			                   Node [][] nodes, 
			                   float [][] maxGeomScores, 
			                   CWrittenTokenSetNoStroke [][][] aRemainingSets)
	{
		final boolean bDebug = false;
		
		String hashKey1 = null;
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
				
			CWrittenTokenSetNoStroke [][][] r_aRemainingSets = evalGeom2RemSetsMap.get(tHashKey);
			for (int i = 0; i < r_aRemainingSets.length; ++i)
				aRemainingSets[i] = r_aRemainingSets[i];
			
//			int [] r_idxMax = evalGeom2IdxMaxMap.get(tHashKey);
//			for (int i = 0; i < r_idxMax.length; ++i)
//				idxMax[i] = r_idxMax[i];
			
			return evalGeom2MaxScoreMap.get(tHashKey);
		}
		
		if ( this.bDebug2 )
			System.out.println("evalGeometry: " + tHashKey);
		
		for (int i = 0; i < idxValidProds.length; ++i) {
			int nrhs =  gpSet.prods.get(idxValidProds[i]).rhs.length;
			/* Number of right-hand size elements, including the head */
					
			nodes[i] = new Node[idxPossibleHead.get(i).length];
			maxGeomScores[i] = new float[idxPossibleHead.get(i).length];
			aRemainingSets[i] = new CWrittenTokenSetNoStroke[idxPossibleHead.get(i).length][];
			
			/* Iterate through all potential heads */
			for (int j = 0; j < idxPossibleHead.get(i).length; ++j) {
				int [] idxHead = idxPossibleHead.get(i)[j];
				
				/* Does not include the head */
//				CWrittenTokenSetNoStroke [] remainingSets = new CWrittenTokenSetNoStroke[nrhs - 1];
				
				/* Includes the head */
				CWrittenTokenSetNoStroke [] remainingSets = new CWrittenTokenSetNoStroke[nrhs];
				
				float [] maxGeomScore = new float[1];
				
				if ( idxHead.length == 0 ) {
					/* The head must not be an empty */
//					throw new RuntimeException("TokenSetParser.evalGeometry encountered empty idxHead");
					nodes[i][j] = null;
					maxGeomScores[i][j] = 0.0f;
				}
				else {
					Node n = gpSet.prods.get(idxValidProds[i]).attempt(tokenSet, idxHead, remainingSets, maxGeomScore);
					
					/* If the head child is a terminal, replace tokenName with the actual name of the token */
					if ( n != null && termSet.isTypeTerminal(n.ch[0].termName) )
						n.ch[0].termName = tokenSet.tokens.get(idxPossibleHead.get(i)[j][0]).getRecogWinner();
					
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
													
							if ( termSet.isTypeTerminal(d_lhs) ) {
								int nTokens;
//								if ( k == 0 )		// Assumes the remainingSets does not include the head
//									nTokens = idxHead.length;
//								else
//									nTokens = remainingSets[k - 1].nTokens();
								nTokens = remainingSets[k].nTokens();	// Assumes that remainingSets includes the head
								
								if ( nTokens == 1 )
									d_scores[k] = 1.0f; 
								else
									d_scores[k] = 0.0f;
								
								continue;
							}
							
							//CAbstractWrittenTokenSet d_tokenSet;
							CWrittenTokenSetNoStroke d_tokenSet;
							if ( k == 0 ) {
								/* Head */
								CWrittenTokenSetNoStroke d_tokenSetNoStroke = new CWrittenTokenSetNoStroke();
								d_tokenSetNoStroke.setTokenNames(tokenSet.getTokenNames());
					    		
					    		for (int m = 0; m < idxHead.length; ++m) { 	/* TODO: Wrap in a new constructor */
					    			int irt = idxHead[m];
					    			
					    			d_tokenSetNoStroke.addToken(tokenSet.tokens.get(irt));
					    		}
					    		
								d_tokenSet = d_tokenSetNoStroke; /* Automatic upcast */
							}
							else {
								/* Non-head */
//								d_tokenSet = remainingSets[k - 1];	// Assume that remainingSets does not include the head
								d_tokenSet = remainingSets[k];		// Assume that remainingSets includes the head
							}
							
							if ( bDebug )
								System.out.println("Drilling down from level " + currDrillDepth + 
									           	   " to level " + (currDrillDepth + 1) + ": " +
									           	   gpSet.prods.get(idxValidProds[i]).lhs + 
									               " --> " + d_lhs);
						
							int [] d_idxValidProds = null;
							if ( bUseHashMaps ) {
								hashKey1 = d_tokenSet.toString() + "@" + d_lhs;
								if ( !tokenSetLHS2IdxValidProdsMap.containsKey(hashKey1) ) {								
									d_idxValidProds = gpSet.getIdxValidProds(d_tokenSet, termSet, d_lhs, d_idxPossibleHead, this.bDebug);
									
									/* Store results in hash maps */
									tokenSetLHS2IdxValidProdsMap.put(hashKey1, d_idxValidProds);
									tokenSetLHS2IdxPossibleHeadsMap.put(hashKey1, d_idxPossibleHead);
								}
								else {
									if ( this.bDebug ) 
										System.out.println("Hash map getting: " + hashKey1);
									/* Retrieve results from hash maps */
									d_idxValidProds = tokenSetLHS2IdxValidProdsMap.get(hashKey1);
									d_idxPossibleHead = tokenSetLHS2IdxPossibleHeadsMap.get(hashKey1);
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
							CWrittenTokenSetNoStroke [][][] d_aRemainingSets = new CWrittenTokenSetNoStroke[d_idxValidProds.length][][];
//							int [] d_t_idxMax2 = new int[2];

							currDrillDepth++; /* To check: thread-safe? */
							/* Recursive call */
							float d_maxGeomScore = evalGeometry(d_tokenSet, d_idxValidProds, d_idxPossibleHead, 
																d_nodes, d_c_maxGeomScores, d_aRemainingSets);
							currDrillDepth--;
							d_scores[k] = d_maxGeomScore; 
							/* Is this right? Or should maxGeomScore be multiplied by the geometric mean of the d_maxGeomScores's? TODO */
							
						}
						
						maxGeomScore[0] *= MathHelper.geometricMean(d_scores);
					}
					
					nodes[i][j] = n;
					maxGeomScores[i][j] = maxGeomScore[0];
	
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
			}
			
			if ( c_idxValidProds == null || c_idxValidProds.length == 0 ) {
				maxGeomScores[i][j] = 0.0f; /* Necessary? */
			}
			else {
				Node [][] c_nodes = new Node[c_idxValidProds.length][];
				float [][] c_maxGeomScores = new float[c_idxValidProds.length][];
				CWrittenTokenSetNoStroke [][][] c_aRemainingSets = new CWrittenTokenSetNoStroke[c_idxValidProds.length][][];
				
				/* Recursive call */
				float c_maxScore = evalGeometry(tokenSet, c_idxValidProds, c_idxPossibleHead, 
					                            c_nodes, c_maxGeomScores, c_aRemainingSets);
								
				maxGeomScores[i][j] = c_maxScore;
			}
			
			/* Re-calculate the maximum */
			idxMax2 = MathHelper.indexMax2D(maxGeomScores);
			maxScore = maxGeomScores[idxMax2[0]][idxMax2[1]];
		}
		
		/* Optional: store result in hash map */
		if ( this.bUseHashMaps ) {
			int idxBestProd = idxValidProds[idxMax2[0]];
			tokenSetLHS2IdxBestProdMap.put(hashKey1, idxBestProd);
		}
		
		if ( this.bUseHashMaps2 ) {
			evalGeom2MaxScoreMap.put(tHashKey, maxScore);
			evalGeom2NodesMap.put(tHashKey, nodes);
			evalGeom2ScoresMap.put(tHashKey, maxGeomScores);
			evalGeom2RemSetsMap.put(tHashKey, aRemainingSets);
			
		}
		
		return maxScore;
	}
	
	/* This implements a recursive descend parser */
	private Node parse(CWrittenTokenSetNoStroke tokenSet, String lhs) {
		/* Input sanity check */		
		if ( tokenSet == null )
			System.err.println("Parsing null token set!");
		
		ArrayList<int [][]> idxPossibleHead = new ArrayList<int [][]>();
		/* Determine the name of the lhs */
		
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
			idxValidProds = gpSet.getIdxValidProds(tokenSet, termSet, lhs, idxPossibleHead, this.bDebug);
		}
		
		if ( idxValidProds.length == 0 ) {
			return null; /* No valid production for this token set */
		}
		
		/* Geometric evaluation */
		Node [][] nodes = new Node[idxValidProds.length][];
		float [][] maxGeomScores = new float[idxValidProds.length][];
		CWrittenTokenSetNoStroke [][][] aRemainingSets = new CWrittenTokenSetNoStroke[idxValidProds.length][][];
		
		currDrillDepth = 0;
		nCallEvalGeometry++;
		evalGeometry(tokenSet, idxValidProds, idxPossibleHead, 
				     nodes, maxGeomScores, aRemainingSets);
//		if ( this.bUseHashMaps2 )	// DEBUG
//			System.out.println("After calling evalGeometry() " + nCallEvalGeometry + 
//					           " times, hash map size = " + this.evalGeom2RemSetsMap.size());

		/* Select the maximum geometric score */		
		int [] idxMax2 = MathHelper.indexMax2D(maxGeomScores);
		
		/* *********************************************************** */
		/* New approach: After evalGeometry has been called once, all the 
		 * information should be there, utilize that information.  
		 */
		final boolean bNewApproach = false;
		if ( bNewApproach ) {
			//String maxHashKey = tokenSet.toString() + "@" + MathHelper.intArray2String(idxValidProds);
			LinkedList<Node> nStack = new LinkedList<Node>();
			LinkedList<CWrittenTokenSetNoStroke> rsStack = new LinkedList<CWrittenTokenSetNoStroke>();
			
			Node t_node = nodes[idxMax2[0]][idxMax2[1]];
			CWrittenTokenSetNoStroke [] t_remSets = aRemainingSets[idxMax2[0]][idxMax2[1]];
			
			nStack.push(t_node);
			
			if ( !termSet.isTypeTerminal(nStack.getLast().rhsTypes[0]) ) {
				/* The head child is NT */
				
				/* Get the head indices */
				/* Need to find the parent in the stack */
				String tHashKey1 = tokenSet.toString() + "@" + nStack.getLast().prodSumString.split(" --> ")[0];
				tHashKey1 = tHashKey1;						
				tokenSetLHS2IdxPossibleHeadsMap.get(tHashKey1);
			}
			
			/* Determine how many nodes (including the head), still need to be parsed */
			int nToParse = 0;
			if ( !termSet.isTypeTerminal(nStack.getLast().rhsTypes[0]) ) { /* Head node is NT */			
				if ( nStack.getLast().ch[0].ch == null )
					nToParse++;
			}
			else {	/* Head node should already be here */
				
			}
			
			nToParse += t_remSets.length;
			for (int k = 0; k < t_remSets.length; ++k) {
				rsStack.push(t_remSets[k]);
			}
			
			for (int k = 0; k < t_remSets.length; ++k) {
				CWrittenTokenSetNoStroke t_remSet = t_remSets[k];
				
				String tHashKey1 = t_remSet.toString() + "@" + t_node.rhsTypes[k + 1];
	//			System.out.print("tHashkey1 = " + tHashKey1); //DEBUG
				
				int [] t_idxValidProds = null;
				if ( tokenSetLHS2IdxValidProdsMap.containsKey(tHashKey1) ) {
					t_idxValidProds = tokenSetLHS2IdxValidProdsMap.get(tHashKey1);				
					
					String tHashKey2 = t_remSet.toString() + "@" + MathHelper.intArray2String(t_idxValidProds);
					System.out.println("tHashkey2 = " + tHashKey2); //DEBUG
					
					if ( evalGeom2ScoresMap.containsKey(tHashKey2) ) {
						float [][] t_c_scores = this.evalGeom2ScoresMap.get(tHashKey2);	/* TODO: Why can't we store the best Node? */
						Node [][] t_c_nodes = this.evalGeom2NodesMap.get(tHashKey2);
						
						int [] t_c_idxMax2 = MathHelper.indexMax2D(t_c_scores);
						Node t_c_node = t_c_nodes[t_c_idxMax2[0]][t_c_idxMax2[1]];
					}
					else {
						System.err.println("WARNING: Missing hash key from hash map 2: " + tHashKey2);
						
	//					String [] keys = new String[evalGeom2ScoresMap.keySet().size()]; 
	//					evalGeom2ScoresMap.keySet().toArray(keys);
	//					for (int j = 0; j < keys.length; ++j) {
	//						System.err.println("keySet #" + j + ": " + keys[j]);
	//					}
					}
					
					
				}
				else {
					/* This happens only (?) on single digits */
					System.err.println("WARNING: Missing hash key from hash map 1: " + tHashKey1);
				}
				
	//			int iBestProd = tokenSetLHS2IdxBestProdMap.get(tHashKey1);	// No use. TODO: Eliminate this and related lines.
				
	//			Node t_child_node = this.evalGeom2NodesMap.get(tHashKey2);
			}
		}
		/* ~New approach */
		/* *********************************************************** */
		
		/* Search for ties */
		float maxScore = maxGeomScores[idxMax2[0]][idxMax2[1]];
		if ( maxScore == 0.0 )
			return null;
		
		int [][] idxTieMax = MathHelper.findTies2D(maxGeomScores, maxScore);
		
		/* Iterate through all members of the tie and find the best one */
		Node n;
		CWrittenTokenSetNoStroke [] remSets;
		
		if ( idxTieMax.length > 1 )
			System.err.println("WARNING: Length of idxTieMax = " + idxTieMax.length + " (> 1)");
		
		int idx0 = idxTieMax[0][0];
		int idx1 = idxTieMax[0][1];
		
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
		
		if ( !n.isTerminal() ) {
			/* In case the head child of n is a non-terminal (NT), it needs 
			 * to be parsed.
			 */
			/* Determine if the head child is an NT */
			String headChildType = n.getRHSTypes()[0];			
			boolean bHeadChildNT = !termSet.isTypeTerminal(headChildType);
			
//			if ( bHeadChildNT ) {
//				int [] headChildTokenIdx = idxPossibleHead.get(idx0)[idx1];
//				
//				CWrittenTokenSetNoStroke headChildTokenSet = new CWrittenTokenSetNoStroke(tokenSet, headChildTokenIdx);
//				
//				/* Recursive call */
//				n.ch[0] = parse(headChildTokenSet, headChildType);
//			}
			
			/* Deal with the non-head child(ren) */
//			for (int k = 0; k < nValidRemSets; ++k) { // Assume that remSets does not include the head
			for (int k = 0; k < remSets.length; ++k) { // Assume that the remSet includes the head
//				String requiredType = n.getRHSTypes()[k + 1]; 	// Assume that remSets does not include the head
				String requiredType = n.getRHSTypes()[k];		// Assume that the remSet includes the head
				
				/* Recursive call */
				if ( k > 0 || (k == 0 && bHeadChildNT) ) {
					Node cn = parse(remSets[k], requiredType);
				
					if ( cn != null ) {
						String actualType = cn.prodSumString.split(" ")[0];
						if ( requiredType.equals(actualType) ) {
	//						n.setChild(k + 1, cn);		// Assume that remSets does not include the head
							n.ch[k] = cn;			// Assume that the remSet includes the head
						}
					}
				}

			}
			
		}
		
//		/* What if there is still a tie? TODO */
		
//		if ( bTentative ) {
//			float maxChildGeometricScore = childGeometricScores[idxBreakTie];
//			if ( maxChildGeometricScore == 0.0f ) {				
//				return null;
//			}
//		}
		
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
		Integer [] singleOutIdx = {};
//		Integer [] singleOutIdx = {1};

		String tokenSetSuffix = ".wts";
		String tokenSetPrefix = null;
		String prodSetFN = null;
		String termSetFN = null;
		
		String hostName;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
			if ( hostName.toLowerCase().equals("smcg_w510") ) {
				tokenSetPrefix = "C:\\Users\\systemxp\\Documents\\My Dropbox\\Plato\\data\\tokensets\\TS_";
				prodSetFN = "C:\\Users\\systemxp\\Documents\\My Dropbox\\javaWS\\handwriting\\graph_lang\\productions.txt";
				termSetFN = "C:\\Users\\systemxp\\Documents\\My Dropbox\\javaWS\\handwriting\\graph_lang\\terminals.txt";
			}
			else {
				tokenSetPrefix = "C:\\Users\\scai\\Dropbox\\Plato\\data\\tokensets\\TS_";
				prodSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\productions.txt";
				termSetFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\terminals.txt";
			}
		}
		catch (Exception e) {
			System.err.println("Cannot determine host name");
		}
		
		/* Create written token set */
		CWrittenTokenSetNoStroke wts = new CWrittenTokenSetNoStroke();
		
		TokenSetParser tokenSetParser = new TokenSetParser(termSetFN, prodSetFN);
		
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
