package me.scai.parsetree;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import me.scai.handwriting.CWrittenTokenSetNoStroke;

public class TokenSetParser implements ITokenSetParser {

	private TerminalSet termSet = null;
	private GraphicalProductionSet gpSet = null;
	// protected ParseTreeStringizer stringizer = null;
	// protected ParseTreeEvaluator evaluator = null;
	/* TODO: Separate the stringize and evaluator from the parser */

	/* Properties */
	private float recursionGeomScoreRatioThresh = 0.90f;

	private int drillDepthLimit = Integer.MAX_VALUE; /*
													 * No limit on levels of
													 * recursive drill
													 */
	// private int drillDepthLimit = 2; /* Limiting it to a specific number runs
	// without errors, but may cause wrong parsing */
	private int currDrillDepth = 0; /* Thread-safe? */

	private boolean bDebug = false;
	private boolean bDebug2 = false;

	/* Temporary variables for parsing */
    private boolean usingMultiThreads = false;

	private Map<String, int[]> tokenSetLHS2IdxValidProdsMap;
	// private HashMap<String, int []> tokenSetLHS2IdxValidProdsNoExcludeMap;
	private Map<String, ArrayList<int[][]>> tokenSetLHS2IdxPossibleHeadsMap;
	private Map<String, Integer> tokenSetLHS2IdxBestProdMap;

	private Map<String, Float> evalGeom2MaxScoreMap;
	private Map<String, Node[][]> evalGeom2NodesMap;
	private Map<String, float[][]> evalGeom2ScoresMap;
	private Map<String, CWrittenTokenSetNoStroke[][][]> evalGeom2RemSetsMap;

	protected ParseTreeBiaser biaser;

	/* Methods */

	/* Constructor */
	public TokenSetParser(TerminalSet t_termSet,
			GraphicalProductionSet t_gpSet,
			final float t_recursionGeomScoreRatioThresh) {
		termSet = t_termSet;
		gpSet = t_gpSet;
		setRecursionGeomScoreRatioThresh(t_recursionGeomScoreRatioThresh);

		biaser = new ParseTreeBiaser(gpSet);
	}

	public void setRecursionGeomScoreRatioThresh(
			final float t_recursionGeomScoreRatioThresh) {
		if (t_recursionGeomScoreRatioThresh < 0.0f
				|| t_recursionGeomScoreRatioThresh >= 1.0f)
			throw new IllegalArgumentException();

		recursionGeomScoreRatioThresh = t_recursionGeomScoreRatioThresh;
	}

	public float getRecursionGeomScoreRatioThresh() {
		return recursionGeomScoreRatioThresh;
	}

	public void setDebug(boolean t_bDebug) {
		bDebug = t_bDebug;
	}

	public void init() {
        setUsingMultiThreads(false);

		tokenSetLHS2IdxValidProdsMap = new ConcurrentHashMap<String, int[]>();
		tokenSetLHS2IdxPossibleHeadsMap = new ConcurrentHashMap<String, ArrayList<int[][]>>();
		tokenSetLHS2IdxBestProdMap = new ConcurrentHashMap<String, Integer>();

		evalGeom2MaxScoreMap = new ConcurrentHashMap<String, Float>();
		evalGeom2NodesMap = new ConcurrentHashMap<String, Node[][]>();
		evalGeom2ScoresMap = new ConcurrentHashMap<String, float[][]>();
		evalGeom2RemSetsMap = new ConcurrentHashMap<String, CWrittenTokenSetNoStroke[][][]>();

		// tokenSetLHS2IdxValidProdsMap.clear();
		// // tokenSetLHS2IdxValidProdsNoExcludeMap.clear();
		// tokenSetLHS2IdxPossibleHeadsMap.clear();
		// tokenSetLHS2IdxBestProdMap.clear();
		//
		// evalGeom2MaxScoreMap.clear();
		// evalGeom2NodesMap.clear();
		// evalGeom2ScoresMap.clear();
		// evalGeom2RemSetsMap.clear();
	}

	@Override
	public Node parse(CWrittenTokenSetNoStroke tokenSet) throws TokenSetParserException {
		init();

		tokenSet.getAllTokensTerminalTypes(termSet);

		Node n = parse(tokenSet, "ROOT");
		biaser.process(n);

		return n;
	}

    /* Side effect input arguments:
     *     nodes,
     *     maxGeomScores,
     *     aRemainingSets */
	private float evalGeometry(final CWrittenTokenSetNoStroke tokenSet,
                               final int[] idxValidProds, final int[] idxValidProds_wwoe,
                               final ArrayList<int[][]> idxPossibleHead,
                               final Node[][] nodes,
                               final float[][] maxGeomScores,
                               final CWrittenTokenSetNoStroke[][][] aRemainingSets) {
		final boolean bDebug = false;
		final float selectiveDrillThresh = recursionGeomScoreRatioThresh; /* To disable selective drill, set to 0.0f */

		final String[] hashKey1 = new String[1];
//		String tHashKey = tokenSet.toString() + "@"+ MathHelper.intArray2String(idxValidProds);
//        String tHashKey = tokenSet.toString() + "@"+ MathHelper.intArray2HashCode(idxValidProds);
//        String tHashKey = "" + tokenSet.hashCode() + "@"+ MathHelper.intArray2String(idxValidProds);
        String tHashKey = getHashCodeFromTokenSetAndIdxValidProds(tokenSet, idxValidProds);

        if (existsInEvalGeom2MaxScoreMap(tHashKey)) {
			if (this.bDebug2) {
                System.out.println("Hash map contains key: " + tHashKey);
            }

			Node[][] r_nodes = getFromEvalGeom2NodesMap(tHashKey);
			for (int i = 0; i < r_nodes.length; ++i) {
                nodes[i] = r_nodes[i];
            }

			float[][] r_maxGeomScores = getFromEvalGeom2ScoresMap(tHashKey);
			for (int i = 0; i < r_nodes.length; ++i) {
                maxGeomScores[i] = r_maxGeomScores[i];
            }

			CWrittenTokenSetNoStroke[][][] r_aRemainingSets = getFromEvalGeom2RemSetsMap(tHashKey);
			for (int i = 0; i < r_aRemainingSets.length; ++i) {
                aRemainingSets[i] = r_aRemainingSets[i];
            }

			return getFromEvalGeom2MaxScoreMap(tHashKey);
		}

		if (this.bDebug2) {
            System.out.println("evalGeometry: " + tHashKey);
        }

		/* First pass: Obtain the geometric scores without drill-down */
		boolean[][] bToDrill = null;
		if (selectiveDrillThresh > 0.0f) {
			final float[][] maxGeomScores_noFlag = new float[maxGeomScores.length][];

//            Thread[] jobThreads = new Thread[idxValidProds.length];
			for (int ii = 0; ii < idxValidProds.length; ++ii) {
                final int i = ii;

//                Runnable jobRunnable = new Runnable() {
//                    @Override
//                    public void run() {
                        int nrhs = gpSet.prods.get(idxValidProds[i]).rhs.length;
				        /* Number of right-hand size elements, including the head */

                        nodes[i] = new Node[idxPossibleHead.get(i).length];
                        maxGeomScores[i] = new float[idxPossibleHead.get(i).length];
                        maxGeomScores_noFlag[i] = new float[idxPossibleHead.get(i).length];
                        aRemainingSets[i] = new CWrittenTokenSetNoStroke[idxPossibleHead.get(i).length][];

                        for (int j = 0; j < idxPossibleHead.get(i).length; ++j) {
                            final int[] idxHead = idxPossibleHead.get(i)[j];

                            final CWrittenTokenSetNoStroke[] remainingSets = new CWrittenTokenSetNoStroke[nrhs];
                            final float[] maxGeomScore = new float[1];

                            if (idxHead.length == 0) {
				        		/* The head must not be an empty */
                                // throw new RuntimeException("TokenSetParser.evalGeometry encountered empty idxHead");
                                nodes[i][j] = null;
                                maxGeomScores[i][j] = 0.0f;
                                maxGeomScores_noFlag[i][j] = 0.0f;
                            } else {
                                Node n = gpSet.prods.get(idxValidProds[i]).attempt(tokenSet, idxHead, remainingSets, maxGeomScore);

						        /* If the head child is a terminal, replace tokenName with the actual name of the token */
                                if (n != null && termSet.isTypeTerminal(n.ch[0].termName)) {
                                    n.ch[0].termName = tokenSet.tokens.get(idxPossibleHead.get(i)[j][0]).getRecogWinner();
                                }

                                /* Writing data to outside variables */
                                nodes[i][j] = n;
                                maxGeomScores[i][j] = maxGeomScore[0];
                                maxGeomScores_noFlag[i][j] = maxGeomScore[0];
                                if (maxGeomScores_noFlag[i][j] == GraphicalProduction.flagNTNeedsParsing) {
                                    maxGeomScores_noFlag[i][j] = 1.0f; /* Is this correct? */
                                }

                                aRemainingSets[i][j] = remainingSets; // PerfTweak new key
                            }

                        }
//                    }
//                };

//                jobThreads[i] = new Thread(jobRunnable);
//                jobThreads[i].start();
			}

//            for (int i = 0; i < jobThreads.length; ++i) {
//                try {
//                    jobThreads[i].join();
//                } catch(InterruptedException exc) {}
//            }

			/* Determine which ones need to be drilled down */
			int[] t_idxMax2 = MathHelper.indexMax2D(maxGeomScores_noFlag);
			float t_max2 = maxGeomScores_noFlag[t_idxMax2[0]][t_idxMax2[1]];


			bToDrill = new boolean[maxGeomScores_noFlag.length][];
			for (int i = 0; i < maxGeomScores_noFlag.length; ++i) {
				bToDrill[i] = new boolean[maxGeomScores_noFlag[i].length];

				for (int j = 0; j < maxGeomScores_noFlag[i].length; ++j) {
					if (maxGeomScores_noFlag[i][j] > t_max2
							* selectiveDrillThresh) {
						bToDrill[i][j] = true;
					} else {
						bToDrill[i][j] = false;
						maxGeomScores[i][j] = 0.0f;
					}
				}
			}
		}

		/* Second pass: Selective drill-down */
//        Thread[] jobThreads2p = new Thread[idxValidProds.length]; /* Second-pass jobs */
		for (int ii = 0; ii < idxValidProds.length; ++ii) { /* Iterate through valid productions */
            final int i = ii;

            final boolean[] bToDrill2p = bToDrill[i];

//            Runnable jobRunnable2p = new Runnable() {
//                @Override
//                public void run() {
                    int nrhs = gpSet.prods.get(idxValidProds[i]).rhs.length;
			        /* Number of right-hand size elements, including the head */

                    if (selectiveDrillThresh == 0.0f) {
                        nodes[i] = new Node[idxPossibleHead.get(i).length]; // SelectiveDrill
                        maxGeomScores[i] = new float[idxPossibleHead.get(i).length]; // SelectiveDrill
                        aRemainingSets[i] = new CWrittenTokenSetNoStroke[idxPossibleHead.get(i).length][]; // SelectiveDrill
                    }

			        /* Iterate through all potential heads */
                    for (int j = 0; j < idxPossibleHead.get(i).length; ++j) {
                        int[] idxHead = idxPossibleHead.get(i)[j];

                        /* Includes the head */
                        CWrittenTokenSetNoStroke[] remainingSets = new CWrittenTokenSetNoStroke[nrhs];

                        float[] maxGeomScore = new float[1];

                        if (idxHead.length == 0) {
                            /* The head must not be an empty */
                            // throw new RuntimeException("TokenSetParser.evalGeometry encountered empty idxHead");
                            nodes[i][j] = null;
                            maxGeomScores[i][j] = 0.0f;
                        } else {
                            Node n;
                            if (selectiveDrillThresh == 0.0f) {
                                n = gpSet.prods.get(idxValidProds[i]).attempt(tokenSet, idxHead, remainingSets, maxGeomScore);

                                /* If the head child is a terminal, replace tokenName with the actual name of the token */
                                if (n != null && termSet.isTypeTerminal(n.ch[0].termName)) {
                                    n.ch[0].termName = tokenSet.tokens.get(idxPossibleHead.get(i)[j][0]).getRecogWinner();
                                }
                            } else {
                                n = nodes[i][j];
                                remainingSets = aRemainingSets[i][j];
                                maxGeomScore[0] = maxGeomScores[i][j];
                            }

                            boolean bToDrillThis = true;
                            if (selectiveDrillThresh > 0.0f) {
//                                bToDrillThis = bToDrill[i][j];
                                bToDrillThis = bToDrill2p[j];
                            }

                            if (bToDrillThis && currDrillDepth < drillDepthLimit && maxGeomScore[0] != 0.0f && nrhs > 1) {
                                /* Drill one level down: get the maximum geometric scores from its children */
                                float[] d_scores = new float[nrhs];
                                for (int k = 0; k < nrhs; ++k) {
                                    /* Iterate through all rhs items, including the head and the non-heads. */
                                    ArrayList<int[][]> d_idxPossibleHead = new ArrayList<int[][]>();

                                    String d_lhs;
                                    d_lhs = gpSet.prods.get(idxValidProds[i]).rhs[k];

                                    if (termSet.isTypeTerminal(d_lhs)) {
                                        /* TODO: Add to hashmaps */
                                        int nTokens;

                                        nTokens = remainingSets[k].nTokens(); // Assumes that remainingSets includes the head
                                        d_scores[k] = (nTokens == 1) ? 1.0f : 0.0f;

                                        continue;
                                    }

                                    CWrittenTokenSetNoStroke d_tokenSet;
                                    if (k == 0) {
                                        /* Head */
                                        d_tokenSet = new CWrittenTokenSetNoStroke(
                                                tokenSet, idxHead);
                                    } else {
                                        /* Non-head */
                                        d_tokenSet = remainingSets[k]; // Assume that remainingSets includes the head
                                    }

                                    if (bDebug) {
                                        System.out.println("Drilling down from level "
                                                + currDrillDepth + " to level "
                                                + (currDrillDepth + 1) + ": "
                                                + gpSet.prods.get(idxValidProds[i]).lhs
                                                + " --> " + d_lhs);
                                    }

                                    int[][] d_idxValidProds_wwoe = null; /* wwoe: with or without exclusion */
                                    int[] d_idxValidProds = null;
                                    int[] d_idxValidProds_noExclude = null;

                                    hashKey1[0] = d_tokenSet.toString() + "@" + d_lhs;
                                    if (!existsInTokenSetLHS2IdxValidProdsMap(hashKey1[0])) {
                                        d_idxValidProds_wwoe = gpSet.getIdxValidProds(d_tokenSet, null, termSet, d_lhs, d_idxPossibleHead, bDebug);
                                        d_idxValidProds = d_idxValidProds_wwoe[0];
                                        d_idxValidProds_noExclude = d_idxValidProds_wwoe[1];

                                        /* Store results in hash maps */
                                        putInTokenSetLHS2IdxValidProdsMap(hashKey1[0], d_idxValidProds);
                                        // tokenSetLHS2IdxValidProdsNoExcludeMap.put(hashKey1[0], d_idxValidProds_wwoe[1]);
                                        putInTokenSetLHS2IdxPossibleHeadsMap(hashKey1[0], d_idxPossibleHead);
                                    } else {
                                        if (bDebug) {
                                            System.out.println("Hash map getting: " + hashKey1[0]);
                                        }
                                        /* Retrieve results from hash maps */
                                        d_idxValidProds = getFromTokenSetLHS2IdxValidProdsMap(hashKey1[0]);
                                        // d_idxValidProds_noExclude = tokenSetLHS2IdxValidProdsNoExcludeMap.get(hashKey1[0]);
                                        d_idxPossibleHead = getFromTokenSetLHS2IdxPossibleHeadsMap(hashKey1[0]);
                                    }

                                    if (d_idxValidProds.length == 0) {
                                        d_scores[k] = 0.0f;
                                        continue;
                                    }

                                    Node[][] d_nodes = new Node[d_idxValidProds.length][];
                                    float[][] d_c_maxGeomScores = new float[d_idxValidProds.length][];
                                    CWrittenTokenSetNoStroke[][][] d_aRemainingSets = new CWrittenTokenSetNoStroke[d_idxValidProds.length][][];
                                    // int [] d_t_idxMax2 = new int[2];

                                    currDrillDepth++; /* To check: thread-safe? */

                                    /************************************/
                                    /*          Recursive call          */
                                    float d_maxGeomScore = evalGeometry(d_tokenSet, d_idxValidProds, d_idxValidProds_noExclude,
                                            d_idxPossibleHead, d_nodes, d_c_maxGeomScores, d_aRemainingSets);
                                    /************************************/

                                    currDrillDepth--;
                                    if (d_maxGeomScore == 0.0f) {
                                        break;  /* Because geometric mean will be calculated, if any of the score is zero, the result will also be zero */
                                    }

                                    d_scores[k] = d_maxGeomScore;

                                    /* Is this right? Or should maxGeomScore be multiplied by the geometric mean of the d_maxGeomScores's? TODO */
                                }

                                maxGeomScore[0] *= MathHelper.geometricMean(d_scores);
                            }

                            nodes[i][j] = n;
                            maxGeomScores[i][j] = maxGeomScore[0];

                            aRemainingSets[i][j] = remainingSets; // PerfTweak new key
                        }

                    }
//                }
//            };

//            jobThreads2p[i] = new Thread(jobRunnable2p);
		}

//        if (getUsingMultiThreads()) {
//            for (int i = 0; i < jobThreads2p.length; ++i) {
//                jobThreads2p[i].start();
//                try {
//                    jobThreads2p[i].join();
//                } catch (InterruptedException exc) {}
//            }
//        } else {
//            for (int i = 0; i < jobThreads2p.length; ++i) {
//                jobThreads2p[i].start();
//            }
//            for (int i = 0; i < jobThreads2p.length; ++i) {
//                try {
//                    jobThreads2p[i].join();
//                } catch (InterruptedException exc) {}
//            }
//
//            setUsingMultiThreads(true);
//        }

		/* Get return value */
		int[] idxMax2 = MathHelper.indexMax2D(maxGeomScores); /* TODO: Resolve ties */
		float maxScore = maxGeomScores[idxMax2[0]][idxMax2[1]];

		/* Look for flags that indicate the need for further parsing and parse them further. Loop until all of them are gotten rid of through
		 * recursive calls. */
		/* We probably don't need to store all the aRemainingSets. This can probably reduce the number of GC and speed things up. Make this an
		 * option? */
		while (maxScore == GraphicalProduction.flagNTNeedsParsing) {
			int i = idxMax2[0];
			int j = idxMax2[1];

			GraphicalProduction c_prod = gpSet.prods.get(idxValidProds[i]);
			String c_lhs = c_prod.rhs[0];
			ArrayList<int[][]> c_idxPossibleHead = new ArrayList<int[][]>();

			int[][] c_idxValidProds_wwoe = null;
			int[] c_idxValidProds = null;
			int[] c_idxValidProds_noExclude = null;

			String hashKey = tokenSet.toString() + "@" + c_lhs;
			if (!existsInTokenSetLHS2IdxValidProdsMap(tokenSet.toString())) {
				c_idxValidProds_wwoe = gpSet.getIdxValidProds(tokenSet, null,
						termSet, c_lhs, c_idxPossibleHead, this.bDebug);
				c_idxValidProds = c_idxValidProds_wwoe[0];
				c_idxValidProds_noExclude = c_idxValidProds_wwoe[1];

				/* Store results in hash maps */
				putInTokenSetLHS2IdxValidProdsMap(hashKey, c_idxValidProds);
				// tokenSetLHS2IdxValidProdsNoExcludeMap.put(hashKey,
				// c_idxValidProds_noExclude);
				putInTokenSetLHS2IdxPossibleHeadsMap(hashKey, c_idxPossibleHead);
			} else {
				if (this.bDebug)
					System.out.println("Hash map getting: " + hashKey);

				/* Retrieve results from hash maps */
				c_idxValidProds = getFromTokenSetLHS2IdxValidProdsMap(hashKey);
				// c_idxValidProds_noExclude = tokenSetLHS2IdxValidProdsNoExcludeMap.get(hashKey);
				c_idxPossibleHead = getFromTokenSetLHS2IdxPossibleHeadsMap(hashKey);
			}

			if (c_idxValidProds == null || c_idxValidProds.length == 0) {
				maxGeomScores[i][j] = 0.0f; /* Necessary? */
			} else {
				Node[][] c_nodes = new Node[c_idxValidProds.length][];
				float[][] c_maxGeomScores = new float[c_idxValidProds.length][];
				CWrittenTokenSetNoStroke[][][] c_aRemainingSets = new CWrittenTokenSetNoStroke[c_idxValidProds.length][][];

				/* Recursive call */
				float c_maxScore = evalGeometry(tokenSet, c_idxValidProds,
						c_idxValidProds_noExclude, c_idxPossibleHead, c_nodes,
						c_maxGeomScores, c_aRemainingSets);

				maxGeomScores[i][j] = c_maxScore;
			}

			/* Re-calculate the maximum */
			idxMax2 = MathHelper.indexMax2D(maxGeomScores);
			maxScore = maxGeomScores[idxMax2[0]][idxMax2[1]];

		}


		/* Optional: store result in hash map */
		int idxBestProd = idxValidProds[idxMax2[0]];
        if (hashKey1[0] != null) {
            putInTokenSetLHS2IdxBestProdMap(hashKey1[0], idxBestProd);
        }

        putEvalGeomMapsData(tHashKey, maxScore, nodes, maxGeomScores, aRemainingSets);

		return maxScore;
	}

    private synchronized boolean existsInTokenSetLHS2IdxValidProdsMap(final String key) {
        return tokenSetLHS2IdxValidProdsMap.containsKey(key);
    }

    private synchronized int[] getFromTokenSetLHS2IdxValidProdsMap(final String key) {
        return tokenSetLHS2IdxValidProdsMap.get(key);
    }

    private synchronized ArrayList<int [][]> getFromTokenSetLHS2IdxPossibleHeadsMap(final String key) {
        return tokenSetLHS2IdxPossibleHeadsMap.get(key);
    }

    private synchronized void putInTokenSetLHS2IdxValidProdsMap(final String key, final int[] idxValidProds) {
        tokenSetLHS2IdxValidProdsMap.put(key, idxValidProds);
    }

    private synchronized void putInTokenSetLHS2IdxPossibleHeadsMap(final String key, final ArrayList<int [][]> idxPossibleHeads) {
        tokenSetLHS2IdxPossibleHeadsMap.put(key, idxPossibleHeads);
    }

    private synchronized void putInTokenSetLHS2IdxBestProdMap(final String key, final int idxBestProd) {
        tokenSetLHS2IdxBestProdMap.put(key, idxBestProd);
    }

    private synchronized boolean existsInEvalGeom2MaxScoreMap(final String key) {
        return evalGeom2MaxScoreMap.containsKey(key);
    }

    private synchronized float getFromEvalGeom2MaxScoreMap(final String key) {
        return evalGeom2MaxScoreMap.get(key);
    }

    private synchronized Node[][] getFromEvalGeom2NodesMap(final String key) {
        return evalGeom2NodesMap.get(key);
    }

    private synchronized float[][] getFromEvalGeom2ScoresMap(final String key) {
        return evalGeom2ScoresMap.get(key);
    }

    private synchronized CWrittenTokenSetNoStroke[][][] getFromEvalGeom2RemSetsMap(final String key) {
        return evalGeom2RemSetsMap.get(key);
    }

    private synchronized void putEvalGeomMapsData(final String key,
                                                  final float maxScore,
                                                  final Node[][] nodes,
                                                  final float[][] maxGeomScores,
                                                  final CWrittenTokenSetNoStroke[][][] remainingSets) {
        evalGeom2MaxScoreMap.put(key, maxScore);
        evalGeom2NodesMap.put(key, nodes);
        evalGeom2ScoresMap.put(key, maxGeomScores);
        evalGeom2RemSetsMap.put(key, remainingSets);
    }

//    private Map<String, Integer> tokenSetLHS2IdxBestProdMap;


	/* This implements a recursive descend parser */
	private Node parse(CWrittenTokenSetNoStroke tokenSet, String lhs) throws TokenSetParserException {
		/* Input sanity check */
		if (tokenSet == null) {
            throw new IllegalArgumentException("Parsing null token set!");
        }

		ArrayList<int[][]> idxPossibleHead = new ArrayList<int[][]>();
		/* Determine the name of the lhs */

		int[][] idxValidProds_wwoe = null; /* wwoe: ?? */
		int[] idxValidProds = null;

		String hashKey = tokenSet.toString() + "@" + lhs;
		if (!existsInTokenSetLHS2IdxValidProdsMap(hashKey)) {
			idxValidProds_wwoe = gpSet.getIdxValidProds(tokenSet, null, termSet, lhs, idxPossibleHead, this.bDebug);
			idxValidProds = idxValidProds_wwoe[0];
			// idxValidProds_noExclude = idxValidProds_wwoe[1];

			/* Store results in hash maps */
			putInTokenSetLHS2IdxValidProdsMap(hashKey, idxValidProds);
			// tokenSetLHS2IdxValidProdsNoExcludeMap.put(hashKey, idxValidProds_noExclude);
			putInTokenSetLHS2IdxPossibleHeadsMap(hashKey, idxPossibleHead);
		} else {
			/* Retrieve results from hash maps */
			idxValidProds = getFromTokenSetLHS2IdxValidProdsMap(hashKey);
			// idxValidProds_noExclude = tokenSetLHS2IdxValidProdsNoExcludeMap.get(hashKey);
			idxPossibleHead = getFromTokenSetLHS2IdxPossibleHeadsMap(hashKey);
		}

		if (idxValidProds.length == 0) {
			return null; /* No valid production for this token set */
		}

		/* Geometric evaluation */
		Node[][] nodes = new Node[idxValidProds.length][];
		float[][] maxGeomScores = new float[idxValidProds.length][];
		CWrittenTokenSetNoStroke[][][] aRemainingSets = new CWrittenTokenSetNoStroke[idxValidProds.length][][];

		currDrillDepth = 0;
		evalGeometry(tokenSet, idxValidProds, null, idxPossibleHead, nodes, maxGeomScores, aRemainingSets);

		/* Select the maximum geometric score */
		int[] idxMax2 = MathHelper.indexMax2D(maxGeomScores);

		/* *********************************************************** */
		/*
		 * New approach: After evalGeometry has been called once, all the
		 * information should be there, utilize that information.
		 */
		LinkedList<Node> nStack = new LinkedList<Node>();
		LinkedList<Boolean> bParsedStack = new LinkedList<Boolean>();
		LinkedList<CWrittenTokenSetNoStroke[]> rsStack = new LinkedList<CWrittenTokenSetNoStroke[]>();
		LinkedList<Integer> levelStack = new LinkedList<Integer>();

		Node t_node = nodes[idxMax2[0]][idxMax2[1]];
		CWrittenTokenSetNoStroke[] t_remSets = aRemainingSets[idxMax2[0]][idxMax2[1]]; /* Includes the head */

		rsStack.push(t_remSets);
		nStack.push(t_node);
		bParsedStack.push(false);
		levelStack.push(0);

        /* Tree construction through depth-first traversal. Child nodes get build prior to parents (bottom-up). */
		while (nStack.size() != 0) { /* Two possible actions in each iteration: push or set child / pop */
			CWrittenTokenSetNoStroke[] rsStackTop = rsStack.getFirst();
			Node nStackTop = nStack.getFirst();
			boolean bParsedStackTop = bParsedStack.getFirst();
			int topLevel = levelStack.getFirst();

			boolean bHeadIsTerminal;
			if (nStackTop.rhsTypes == null) {
                bHeadIsTerminal = true;
            } else {
                bHeadIsTerminal = termSet.isTypeTerminal(nStackTop.rhsTypes[0]);
            }

			/* Is this a terminal? */
			if (bParsedStackTop) { /* Action: set child and pop */
				ListIterator<Boolean> parent = bParsedStack.listIterator();
				ListIterator<Integer> parentLevel = levelStack.listIterator();
				int n = 0;
				boolean isParsed = true;
				while (parent.hasNext()) {
					isParsed = parent.next();
					boolean levelMatch = (parentLevel.next() == topLevel - 1);
					n++;
					if ((!isParsed) && levelMatch)
						break;
				}

				if (isParsed) { /* Broke out due to stack exhaustion */
					return nStack.pop();
				}

				int idxChild = n - 2;
				Node ch = nStack.pop();

                Node tParent = nStack.get(n - 2);
                tParent.setChild(idxChild, ch);

//                /* Check the match of row element counts between rows */
//                if (idxChild == 0 && tParent.prodSumString.startsWith("COLUMN_CONTENT->COLUMN_CONTENT ROW_CONTENT") && tParent.ch[1] != null) {
//                    assert(tParent.ch[1].lhs.equals("ROW_CONTENT")); // TODO: Grammar dependency removal
//                    assert(ch.lhs.equals("COLUMN_CONTENT"));
//
//                    int rowElemCount = 0;
//                    Node rowNode = tParent.ch[1];
//                    while (rowNode.lhs.equals("ROW_CONTENT")) {
//                        rowElemCount++;
//                        rowNode = rowNode.ch[0];
//                    }
//
//                    int newRowElemCount = 0;
//                    if (ch.ch[0].prodSumString.startsWith("ROW_CONTENT")) {
//                        rowNode = ch.ch[0];
//                    } else {
//                        rowNode = ch.ch[1];
//                    }
//                    while (rowNode.lhs.equals("ROW_CONTENT")) {
//                        newRowElemCount++;
//                        rowNode = rowNode.ch[0];
//                    }
//
//                    if (rowElemCount != newRowElemCount) {
//                        throw new RuntimeException("Mismatch between row sizes " + rowElemCount + " != " + newRowElemCount);
//                    }
//
//                    int i26 = 26;
//                }

				if (idxChild == 0) {
                    bParsedStack.set(n - 1, true);
                }

				bParsedStack.pop();
				rsStack.pop();
				levelStack.pop();
			} else { /* Action: push */
				/* Determine how many nodes (including the head) still need to be parsed */
				CWrittenTokenSetNoStroke[] remSets = rsStackTop;

				for (int k = 0; k < remSets.length; ++k) {
					if (k == 0 && bHeadIsTerminal) { /* Head is terminal */
						rsStack.push(null); /* No need to parse */
						nStack.push(nStackTop.ch[0]);
						bParsedStack.push(true);
						levelStack.push(topLevel + 1);

						continue;
					}

					boolean bNodeIsTerminal = termSet.isTypeTerminal(nStackTop.rhsTypes[k]);

					CWrittenTokenSetNoStroke t_remSet = remSets[k];
					if (!bNodeIsTerminal) { /* This node is NT */						
						if (t_remSet == null) {
                            return null;
                        }

						String tHashKey1 = t_remSet.toString() + "@" + nStackTop.rhsTypes[k];

						int[] t_idxValidProds = null;
						t_idxValidProds = getFromTokenSetLHS2IdxValidProdsMap(tHashKey1);

                        if (t_idxValidProds == null) {
                            throw new TokenSetParserException();
                        }

//						String tHashKey2 = t_remSet.toString() + "@" + MathHelper.intArray2String(t_idxValidProds);
                        String tHashKey2 = getHashCodeFromTokenSetAndIdxValidProds(t_remSet, t_idxValidProds);

						float[][] t_c_scores = this.evalGeom2ScoresMap.get(tHashKey2);
						/* TODO: Why can't we store the best Node? */

						Node[][] t_c_nodes = this.evalGeom2NodesMap.get(tHashKey2);

						if (t_c_scores == null) {
							return null;
						}

						int[] t_c_idxMax2 = MathHelper.indexMax2D(t_c_scores);
						Node t_c_node = t_c_nodes[t_c_idxMax2[0]][t_c_idxMax2[1]];
						CWrittenTokenSetNoStroke[] t_c_remSets = this.evalGeom2RemSetsMap.get(tHashKey2)[t_c_idxMax2[0]][t_c_idxMax2[1]];

						/* Push onto stack */
//                        if (t_c_node == null) {
//                            int iii = 0; //DEBUG
//                        }

						nStack.push(t_c_node);
						rsStack.push(t_c_remSets);
						bParsedStack.push(t_c_node.isTerminal());
						levelStack.push(topLevel + 1);
					} else { /* This node is T */
						rsStack.push(null); /* No need to parse */

						float [] tBounds = t_remSet.getSetBounds();
						
						Node tNode = new Node(nStackTop.lhs, nStackTop.prodSumString, nStackTop.rhsTypes[k], tBounds);

						nStack.push(tNode);
						bParsedStack.push(true);
						levelStack.push(topLevel + 1);
					}
				}
			}
		}

		/* ~New approach */
		/* *********************************************************** */

		return null; /* If we are here, parsing has failed for some reason */
	}

    public void setUsingMultiThreads(boolean usingMultiThreads) {
        this.usingMultiThreads = usingMultiThreads;
    }

    public boolean getUsingMultiThreads() {
        return usingMultiThreads;
    }

    private String getHashCodeFromTokenSetAndIdxValidProds(CWrittenTokenSetNoStroke wtSet, int[] idxValidProds) {
        return wtSet.toString() + "@" + MathHelper.intArray2String(idxValidProds);
//        return Integer.toString(wtSet.hashCode()) + "@" + MathHelper.intArray2HashCode(idxValidProds);
    }
}
