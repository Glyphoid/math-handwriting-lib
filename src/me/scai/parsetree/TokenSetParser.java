package me.scai.parsetree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Arrays;
import java.util.HashMap;

import me.scai.handwriting.CWrittenTokenSetNoStroke;

public class TokenSetParser implements ITokenSetParser {
	protected static final String errStr = ParseTreeStringizer.parsingErrString;
	
	protected TerminalSet termSet = null;
	public GraphicalProductionSet gpSet = null;
	//protected ParseTreeStringizer stringizer = null;
	//protected ParseTreeEvaluator evaluator = null;
	/* TODO: Separate the stringize and evaluator from the parser */
	
	/* Properties */
	private int drillDepthLimit = Integer.MAX_VALUE; 	/* No limit on levels of recursive drill */
//	private int drillDepthLimit = 3;	/* Limiting it to a specific number runs without errors, but may cause wrong parsing */
	private int currDrillDepth = 0;	/* Thread-safe? */
	
	private boolean bDebug = false;
	private boolean bDebug2 = false;
	
	/* Temporary variables for parsing */
	private HashMap<String, int []> tokenSetLHS2IdxValidProdsMap;
//	private HashMap<String, int []> tokenSetLHS2IdxValidProdsNoExcludeMap;
	private HashMap<String, ArrayList<int [][]>> tokenSetLHS2IdxPossibleHeadsMap;
	private HashMap<String, Integer> tokenSetLHS2IdxBestProdMap;
	
	private HashMap<String, Float> evalGeom2MaxScoreMap;
	private HashMap<String, Node [][]> evalGeom2NodesMap;
	private HashMap<String, float [][]> evalGeom2ScoresMap;
	private HashMap<String, CWrittenTokenSetNoStroke [][][]> evalGeom2RemSetsMap;
	
	protected ParseTreeBiaser biaser;
	
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
		
		biaser = new ParseTreeBiaser(gpSet);
	}
	
	public void setDebug(boolean t_bDebug) {
		bDebug = t_bDebug;
	}
	
	public void init() {
		tokenSetLHS2IdxValidProdsMap = new HashMap<String, int []>();
//		tokenSetLHS2IdxValidProdsNoExcludeMap = new HashMap<String, int []>();
		tokenSetLHS2IdxPossibleHeadsMap = new HashMap<String, ArrayList<int [][]>>();
		tokenSetLHS2IdxBestProdMap = new HashMap<String, Integer>();
		
		evalGeom2MaxScoreMap = new HashMap<String, Float>();
		evalGeom2NodesMap = new HashMap<String, Node [][]>();
		evalGeom2ScoresMap = new HashMap<String, float [][]>();
		evalGeom2RemSetsMap = new HashMap<String, CWrittenTokenSetNoStroke [][][]>();
		
//		tokenSetLHS2IdxValidProdsMap.clear();
////		tokenSetLHS2IdxValidProdsNoExcludeMap.clear();
//		tokenSetLHS2IdxPossibleHeadsMap.clear();
//		tokenSetLHS2IdxBestProdMap.clear();
//		
//		evalGeom2MaxScoreMap.clear();
//		evalGeom2NodesMap.clear();
//		evalGeom2ScoresMap.clear();
//		evalGeom2RemSetsMap.clear();
	}
	
	@Override
	public Node parse(CWrittenTokenSetNoStroke tokenSet) {
		init();
		
		Node n = parse(tokenSet, "ROOT");
		biaser.process(n);
		
		return n;
	}
	
	private float evalGeometry(CWrittenTokenSetNoStroke tokenSet,
			                   int [] idxValidProds, 
			                   int [] idxValidProds_wwoe, 
			                   ArrayList<int [][]> idxPossibleHead, 
			                   Node [][] nodes, 
			                   float [][] maxGeomScores, 
			                   CWrittenTokenSetNoStroke [][][] aRemainingSets)
	{
		final boolean bDebug = false;
		
		String hashKey1 = null;
		String tHashKey = tokenSet.toString() + "@" + MathHelper.intArray2String(idxValidProds);
		
		if ( evalGeom2MaxScoreMap.containsKey(tHashKey) ) {
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
								d_tokenSet = new CWrittenTokenSetNoStroke(tokenSet, idxHead);
							}
							else {
								/* Non-head */
								d_tokenSet = remainingSets[k];		// Assume that remainingSets includes the head
							}
							
							if ( bDebug )
								System.out.println("Drilling down from level " + currDrillDepth + 
									           	   " to level " + (currDrillDepth + 1) + ": " +
									           	   gpSet.prods.get(idxValidProds[i]).lhs + 
									               " --> " + d_lhs);
						
							int [][] d_idxValidProds_wwoe = null;	/* wwoe: with or without exclusion */
							int [] d_idxValidProds = null;
							int [] d_idxValidProds_noExclude = null;

							hashKey1 = d_tokenSet.toString() + "@" + d_lhs;
							if ( !tokenSetLHS2IdxValidProdsMap.containsKey(hashKey1) ) {							
								d_idxValidProds_wwoe = gpSet.getIdxValidProds(d_tokenSet, null, termSet, d_lhs, 
																		      d_idxPossibleHead, this.bDebug);
								d_idxValidProds = d_idxValidProds_wwoe[0];
								d_idxValidProds_noExclude = d_idxValidProds_wwoe[1];
								
								/* Store results in hash maps */
								tokenSetLHS2IdxValidProdsMap.put(hashKey1, d_idxValidProds);
//								tokenSetLHS2IdxValidProdsNoExcludeMap.put(hashKey1, d_idxValidProds_wwoe[1]);
								tokenSetLHS2IdxPossibleHeadsMap.put(hashKey1, d_idxPossibleHead);
							}
							else {
								if ( this.bDebug ) 
									System.out.println("Hash map getting: " + hashKey1);
								/* Retrieve results from hash maps */
								d_idxValidProds = tokenSetLHS2IdxValidProdsMap.get(hashKey1);
//								d_idxValidProds_noExclude = tokenSetLHS2IdxValidProdsNoExcludeMap.get(hashKey1);
								d_idxPossibleHead = tokenSetLHS2IdxPossibleHeadsMap.get(hashKey1);
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
							float d_maxGeomScore = evalGeometry(d_tokenSet, d_idxValidProds, d_idxValidProds_noExclude, 
									                            d_idxPossibleHead, 
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
			
			int [][] c_idxValidProds_wwoe = null;
			int [] c_idxValidProds = null;
			int [] c_idxValidProds_noExclude = null;
			
			String hashKey = tokenSet.toString() + "@" + c_lhs;
			if ( !tokenSetLHS2IdxValidProdsMap.containsKey(tokenSet.toString()) ) {
				c_idxValidProds_wwoe = gpSet.getIdxValidProds(tokenSet, null, termSet, c_lhs, 
						                                      c_idxPossibleHead, this.bDebug);
				c_idxValidProds = c_idxValidProds_wwoe[0];
				c_idxValidProds_noExclude = c_idxValidProds_wwoe[1];
				                  
				/* Store results in hash maps */
				tokenSetLHS2IdxValidProdsMap.put(hashKey, c_idxValidProds);
//				tokenSetLHS2IdxValidProdsNoExcludeMap.put(hashKey, c_idxValidProds_noExclude);
				tokenSetLHS2IdxPossibleHeadsMap.put(hashKey, c_idxPossibleHead);
			}
			else {
				if ( this.bDebug )
					System.out.println("Hash map getting: " + hashKey);
				
				/* Retrieve results from hash maps */
				c_idxValidProds = tokenSetLHS2IdxValidProdsMap.get(hashKey);
//				c_idxValidProds_noExclude = tokenSetLHS2IdxValidProdsNoExcludeMap.get(hashKey);
				c_idxPossibleHead = tokenSetLHS2IdxPossibleHeadsMap.get(hashKey);
			}
			
			
			if ( c_idxValidProds == null || c_idxValidProds.length == 0 ) {
				maxGeomScores[i][j] = 0.0f; /* Necessary? */
			}
			else {
				Node [][] c_nodes = new Node[c_idxValidProds.length][];
				float [][] c_maxGeomScores = new float[c_idxValidProds.length][];
				CWrittenTokenSetNoStroke [][][] c_aRemainingSets = new CWrittenTokenSetNoStroke[c_idxValidProds.length][][];
				
				/* Recursive call */
				float c_maxScore = evalGeometry(tokenSet, c_idxValidProds, c_idxValidProds_noExclude,  
						                        c_idxPossibleHead, 
					                            c_nodes, c_maxGeomScores, c_aRemainingSets);
								
				maxGeomScores[i][j] = c_maxScore;
			}
			
			/* Re-calculate the maximum */
			idxMax2 = MathHelper.indexMax2D(maxGeomScores);
			maxScore = maxGeomScores[idxMax2[0]][idxMax2[1]];
		}
		
		/* Optional: store result in hash map */		
		int idxBestProd = idxValidProds[idxMax2[0]];
		tokenSetLHS2IdxBestProdMap.put(hashKey1, idxBestProd);
		
		evalGeom2MaxScoreMap.put(tHashKey, maxScore);
		evalGeom2NodesMap.put(tHashKey, nodes);
		evalGeom2ScoresMap.put(tHashKey, maxGeomScores);
		evalGeom2RemSetsMap.put(tHashKey, aRemainingSets);
		
		return maxScore;
	}
	
	/* This implements a recursive descend parser */
	private Node parse(CWrittenTokenSetNoStroke tokenSet, String lhs) {
		/* Input sanity check */		
		if ( tokenSet == null )
			System.err.println("Parsing null token set!");		
		
		ArrayList<int [][]> idxPossibleHead = new ArrayList<int [][]>();
		/* Determine the name of the lhs */
		
		int [][] idxValidProds_wwoe = null;
		int [] idxValidProds = null;
//		int [] idxValidProds_noExclude = null;

		String hashKey = tokenSet.toString() + "@" + lhs;
		if ( !tokenSetLHS2IdxValidProdsMap.containsKey(hashKey) ) {
			idxValidProds_wwoe = gpSet.getIdxValidProds(tokenSet, null, termSet, lhs, 
					                                    idxPossibleHead, this.bDebug);
			idxValidProds = idxValidProds_wwoe[0];
//			idxValidProds_noExclude = idxValidProds_wwoe[1];
			
			/* Store results in hash maps */
			tokenSetLHS2IdxValidProdsMap.put(hashKey, idxValidProds);
//			tokenSetLHS2IdxValidProdsNoExcludeMap.put(hashKey, idxValidProds_noExclude);
			tokenSetLHS2IdxPossibleHeadsMap.put(hashKey, idxPossibleHead);
		}
		else {
			if ( this.bDebug )
				System.out.println("Hash map getting: " + hashKey);
			
			/* Retrieve results from hash maps */
			idxValidProds = tokenSetLHS2IdxValidProdsMap.get(hashKey);
//			idxValidProds_noExclude = tokenSetLHS2IdxValidProdsNoExcludeMap.get(hashKey);
			idxPossibleHead = tokenSetLHS2IdxPossibleHeadsMap.get(hashKey);
		}
		
		if ( idxValidProds.length == 0 ) {
			return null; /* No valid production for this token set */
		}
		
		/* Geometric evaluation */
		Node [][] nodes = new Node[idxValidProds.length][];
		float [][] maxGeomScores = new float[idxValidProds.length][];
		CWrittenTokenSetNoStroke [][][] aRemainingSets = new CWrittenTokenSetNoStroke[idxValidProds.length][][];
		
		currDrillDepth = 0;
		evalGeometry(tokenSet, idxValidProds, null, 
				     idxPossibleHead, 
				     nodes, maxGeomScores, aRemainingSets);

		/* Select the maximum geometric score */		
		int [] idxMax2 = MathHelper.indexMax2D(maxGeomScores);
		
		/* *********************************************************** */
		/* New approach: After evalGeometry has been called once, all the 
		 * information should be there, utilize that information.  
		 */		
		LinkedList<Node> nStack = new LinkedList<Node>();
		LinkedList<Boolean> bParsedStack = new LinkedList<Boolean>();
		LinkedList<CWrittenTokenSetNoStroke []> rsStack = new LinkedList<CWrittenTokenSetNoStroke []>();
		LinkedList<Integer> levelStack = new LinkedList<Integer>();
		
		Node t_node = nodes[idxMax2[0]][idxMax2[1]];			
		CWrittenTokenSetNoStroke [] t_remSets = aRemainingSets[idxMax2[0]][idxMax2[1]]; 	/* Includes the head */

		rsStack.push(t_remSets);
		nStack.push(t_node);
		bParsedStack.push(false);
		levelStack.push(0);
		
		while ( nStack.size() != 0 ) {	/* To possible actions in each iteration: push or set child / pop */
			CWrittenTokenSetNoStroke [] rsStackTop = rsStack.getFirst();
			Node nStackTop = nStack.getFirst();
			boolean bParsedStackTop = bParsedStack.getFirst();
			int topLevel = levelStack.getFirst();
							
			boolean bHeadIsTerminal;
			if ( nStackTop.rhsTypes == null )
				bHeadIsTerminal = true;
			else
				bHeadIsTerminal = termSet.isTypeTerminal(nStackTop.rhsTypes[0]);
			
			/* Is this a terminal? */
			if ( bParsedStackTop ) { /* Action: set child and pop */
				ListIterator<Boolean> parent = bParsedStack.listIterator();
				ListIterator<Integer> parentLevel = levelStack.listIterator();
				int n = 0;
				boolean isParsed = true;
				while (parent.hasNext()) {
					isParsed = parent.next();
					boolean levelMatch = (parentLevel.next() == topLevel - 1);
					n++;
					if ( (!isParsed) && levelMatch )
						break;
				}
				
				if ( isParsed ) {	/* Broke out due to stack exhaustion */
					return nStack.pop();
				}
				
				int idxChild = n - 2;
				Node ch = nStack.pop();
				nStack.get(n - 2).setChild(idxChild, ch);
				if ( idxChild == 0 )
					bParsedStack.set(n - 1, true);
				
				bParsedStack.pop();
				rsStack.pop();
				levelStack.pop();
			}
			else {	/* Action: push */
				/* Determine how many nodes (including the head) still need to be parsed */
				CWrittenTokenSetNoStroke [] remSets = rsStackTop;
				
				for (int k = 0; k < remSets.length; ++k) {
					if ( k == 0 && bHeadIsTerminal ) { /* Head is terminal */
						rsStack.push(null);	/* No need to parse */
						nStack.push(nStackTop.ch[0]);
						bParsedStack.push(true);
						levelStack.push(topLevel + 1);
						
						continue;
					}
					
					CWrittenTokenSetNoStroke t_remSet = remSets[k];
					
					if ( t_remSet == null )
						return null;
					
					String tHashKey1 = t_remSet.toString() + "@" + nStackTop.rhsTypes[k];
					
					int [] t_idxValidProds = null;
					t_idxValidProds = tokenSetLHS2IdxValidProdsMap.get(tHashKey1);
						
					String tHashKey2 = t_remSet.toString() + "@" + MathHelper.intArray2String(t_idxValidProds);
					
					float [][] t_c_scores = this.evalGeom2ScoresMap.get(tHashKey2);	/* TODO: Why can't we store the best Node? */
					Node [][] t_c_nodes = this.evalGeom2NodesMap.get(tHashKey2);
					
					int [] t_c_idxMax2 = MathHelper.indexMax2D(t_c_scores);
					Node t_c_node = t_c_nodes[t_c_idxMax2[0]][t_c_idxMax2[1]];
					CWrittenTokenSetNoStroke [] t_c_remSets = this.evalGeom2RemSetsMap.get(tHashKey2)[t_c_idxMax2[0]][t_c_idxMax2[1]];
							
					/* Push onto stack */
					nStack.push(t_c_node);
					rsStack.push(t_c_remSets);
					bParsedStack.push(t_c_node.isTerminal());
					levelStack.push(topLevel + 1);

				}
			}
		}
		
		/* ~New approach */
		/* *********************************************************** */
			
		return null; /* If we are here, parsing has failed for some reason */
	}
	
	/* Testing routine */
	public static void main(String [] args) {
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
//		Integer [] singleOutIdx = {91};
		
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
		ParseTreeStringizer stringizer = tokenSetParser.gpSet.genStringizer();
		ParseTreeEvaluator evaluator = tokenSetParser.gpSet.genEvaluator();
		
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
			
			String stringized = stringizer.stringize(parseRoot);
			Object evalRes = null;
			if ( !stringized.contains(errStr) ) {
				evalRes = evaluator.eval(parseRoot);
				if ( !evalRes.getClass().equals(Double.class) )
					throw new RuntimeException("Unexpected return type from evaluator");
			}
			
			boolean checkResult = stringized.equals(tokenSetTrueStrings[i]);
			String checkResultStr = checkResult ? "PASS" : "FAIL";
			nPass += checkResult ? 1 : 0; 
			
			String strPrint = "[" + checkResultStr + "] "
					          + "(" + parsingTime + " ms) " 
			                  + "File " + tokenSetNums[i] + ": " 
					          + "\"" + stringized + "\"";
			if ( !checkResult )
				strPrint += " <> " + " \"" + tokenSetTrueStrings[i] + "\"";
						
			strPrint += " {Value = " + evalRes + "}";
			
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
