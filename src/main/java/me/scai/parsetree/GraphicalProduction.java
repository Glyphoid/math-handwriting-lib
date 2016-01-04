package me.scai.parsetree;

import java.util.ArrayList;

import me.scai.handwriting.CAbstractWrittenTokenSet;
import me.scai.handwriting.CWrittenTokenSetNoStroke;

import me.scai.parsetree.geometry.GeometricRelation;
import me.scai.parsetree.geometry.PositionRelation;
import me.scai.parsetree.geometry.AlignRelation;
import me.scai.parsetree.geometry.HeightRelation;
import me.scai.parsetree.geometry.WidthRelation;
import me.scai.parsetree.geometry.SpacingRelation;

/* GraphicalProduction: Key class in the parser infrastructure.
 *     This specify the rules by which multiple 
 * tokens (CWrittenToken) or nodes (Node) combine into a meaningful 
 * collection. 
 */
public class GraphicalProduction {
	/* Tree association for dealing with structures such as additions and subtractions in a row 
	 * This could probably be taken care of by grammar. But currently we deal with it by 
	 * using this association.
	 */
	public enum AssocType {	
		NoAssoc,	
		AssocLeft2A,		/* For productions with two RHS items. Bias toward the first item. */
		AssocRight2B, 		/* For productions with two RHS items. Bias toward the second item. */
		AssocLeft3B,			/* For productions with three RHS items, the first one being the T head node. Bias toward the second item. */		
//		BiasRight
	}
	
	/* Member variables */
	/* Constants */
	final static String strinigzationTag = "_STRINGIZE_";
	final static String evalTag = "_EVAL_";
	final static String mathTexTag = "_MATH_TEX_";
	/* ~Constants */
	
	private final static String tokenRelSeparator = ":";
	private final static String relSeparator = ",";
	public final static String sumStringArrow = "->";
	
	public final static float flagNTNeedsParsing = 111f;
	
	private transient TerminalSet terminalSet;

	String lhs; 	/* Left-hand side, i.e., name of the production, e.g., DIGIT_STRING */

	private int nrhs; 	   	/* Number of right-hand side tokens, e.g., 2 */

	public String [] rhs;


	boolean [] rhsIsTerminal;
	/* Right-hand side items: can be a list of terminal (T) and non-terminal (NT) items.
	 * E.g., {DIGIT, DIGIT_STRING} */
	
	boolean [] bt; 	/* Boolean flags for terminals (T), e.g., {true, false} */

	public String sumString;
	/* Production summary string that does not contain geometric information, e.g.,
	 * "DIGIT_STRING --> DIGIT DIGIT_STRING" 
	 */

	GeometricRelation [][] geomRels;
	GeometricShortcut geomShortcut;
	
	transient AssocType assocType = AssocType.NoAssoc;
	transient String assocName = "";
	/* In addition to association type, the association name needs to be specified. 
	 * This is because different sets of productions may share different associations, 
	 * e.g., addition-subtraction, multiplication-division (non-fraction)
	 */
	
	transient String [] stringizeInstr;		 /* Instruction for stringization */
    transient String [] mathTexInstr;        /* Instruction for generating Math TeX */
    transient String [] mathMlInstr;         /* Instruction for generating MathML */
	public transient  String [] evalInstr;   /* Instruction for evaluation */
	
	/* ~Member variables */

	/* Methods */
	/* Constructors */
	public GraphicalProduction(String t_lhs, 
			                   String [] t_rhs, 
			                   boolean [] t_bt, 
			                   TerminalSet termSet, 		/* For determine if the rhs are terminals */
			                   GeometricRelation [][] t_geomRels, 
			                   AssocType t_assocType,
			                   String t_assocName, 
			                   String [] t_stringizeInstr,
			                   String [] t_mathTexInstr,
			                   String [] t_evalInstr) {
		lhs = t_lhs;
		rhs = t_rhs;
		bt = t_bt;
		geomRels = t_geomRels;
		
		nrhs = t_rhs.length;
		
		terminalSet = termSet;
		
		/* Determine if the rhs items are terminals */
		if ( rhs != null ) {
			rhsIsTerminal = new boolean[nrhs];
			for (int i = 0; i < nrhs; ++i) {
				rhsIsTerminal[i] = terminalSet.isTypeTerminal(rhs[i]);
			}
		}
		else {
			System.err.println("WARNING: no rhs");
		}
		
		/* Generate summary string */
		genSumString();
		
		/* Generate geometric shortcut, if any. 
		 * If there is no shortcut, shortcutType will be noShortcut. */
		geomShortcut = new GeometricShortcut(this, terminalSet);
		
		assocType = t_assocType;
		assocName = t_assocName;
		stringizeInstr = t_stringizeInstr;
		mathTexInstr = t_mathTexInstr;
		evalInstr = t_evalInstr;
	}
	
	private void genSumString() {
		sumString = lhs + sumStringArrow;
		for (int i = 0; i < rhs.length; ++i) {
			sumString += rhs[i];
			if ( i < rhs.length - 1 ) {
				sumString += " ";
			}
		}
	}
	
	private void createHeadChild(Node n, float [] bounds) {
		/* Create head child node */
		if ( n != null && 
	         rhs.length > 0 ) {
			/* TODO: hc should contain information about the 
			 * tokens that make up of the head child for further 
			 * parsing.
			 * hc also needs to be expanded if it is an NT. 
			 */
			
			Node hc = new Node(lhs, rhs[0], rhs[0], bounds); /* TODO: Second input argument is erroneous? */
			n.setChild(0, hc);
//			n.addChild(hc);
		}
	}
	
	/* Attempt to parse the input token set with this production, 
	 * given that the j-th token is used as the head.
	 * 
	 * Return: Node: node with the production, geometric, and other 
	 *               information. 
	 *               
	 * Side effect input:
	 *         remainingSets: token sets after the head node is
	 *         parsed out. null if parsing is unsuccessful. 
	 */
	public Node attempt(final CWrittenTokenSetNoStroke tokenSet,
			            final int [] iHead,
			            final CAbstractWrittenTokenSet [] remainingSets, 			//PerfTweak new
			            final float [] maxGeomScore) {

		if ( iHead.length == 0 ) {
			throw new RuntimeException("GraphicalProductionSet.attempt encountered empty idxHead.");
		}

		/* Configuration constants */
		final boolean bUseShortcut = true; /* TODO: Get rid of this constant when the method proves to be reliable */
		
		int nnht = tokenSet.nTokens() - iHead.length; /* Number of non-head tokens */
		int nrn = nrhs - 1; /* Number of remaining nodes to match */
						
		if ( (nrn > nnht) || 
		     (nrn == 0 && nnht > 0) ) {
			maxGeomScore[0] = 0.0f;
			Node n = new Node(lhs, sumString, rhs);
			
			createHeadChild(n, null);
			
			return n;
		}		

		int [][] labels = null;

		if ( geomShortcut.existsTripartiteTerminal() && bUseShortcut ) {
			/* Use this smarter approach when a geometric shortcut exists */
			labels = geomShortcut.getPartitionTripartiteTerminal(tokenSet, iHead);
		} else if ( geomShortcut.existsDefIntegStyle() && bUseShortcut) {
            labels = geomShortcut.getDefIntegStyle(tokenSet, iHead);
        } else if ( geomShortcut.existsSigmaPiStyle() && bUseShortcut) {
            labels = geomShortcut.getSigmaPiStyle(tokenSet, iHead);
        } else {
			/* Get all possible partitions: in "labels" */
			/* This is the brute-force approach. */
			labels = MathHelper.getFullDiscreteSpace(nrn, nnht);
            /* TODO: Discard the partitions that don't make sense to speed things up. */
		}

        if (labels == null || labels.length == 0) {
            return null;
        }


	    /* Get index to all non-head token */
        int[] inht = new int[nnht];
        int cnt = 0;
        int match = 0;
        for (int i = 0; i < tokenSet.nTokens(); ++i) {
            boolean bContains = false;
	    	for (int j = match; j < iHead.length; ++j) { // Assume: iHead is always in ascending order
	    		if ( iHead[j] == i ) {
	    			bContains = true;
                    match = j + 1;
	    			break;
	    		}
	    	}

	    	if ( !bContains ) {
                inht[cnt++] = i;
            }
        }
	    
	    /* Construct the remaining sets and evaluate their geometric relations */
	    CWrittenTokenSetNoStroke [][] a_rems = new CWrittenTokenSetNoStroke[labels.length][];
	    float [] geomScores = new float[labels.length];
	    
	    for (int i = 0; i < labels.length; ++i) {		/* Iterate through all partitions */
	    	a_rems[i] = new CWrittenTokenSetNoStroke[nrn];
	    	boolean [] remsFilled = new boolean[nrn];
	    	
	    	for (int j = 0; j < nrn; ++j)
	    		 /* TODO: Type safety check */
	    		a_rems[i][j] = new CWrittenTokenSetNoStroke();
    		
    		for (int k = 0; k < labels[i].length; ++k) {
    			int inode = labels[i][k];
                int irt = inht[k];
    			
    			/* The last input argument sets bCheck to false for speed */
    			/* Is this a dangerous action? */
                // Omit token UUIDs for performance
    			a_rems[i][inode].addTokenWithoutUuids(tokenSet.tokens.get(irt), tokenSet.tokenIDs.get(irt));
    			
    			remsFilled[inode] = true;
    		}
    		
    		for (int j = 0; j < nrn; ++j) {
    			a_rems[i][j].calcBounds();
    		}
    		
    		/* If there is any unfilled remaining token set, skip */
    		boolean bAllFilled = true;
    		for (int j = 0; j < nrn; j++) {
    			if ( !remsFilled[j] ) {
    				bAllFilled = false;
    				break;
    			}
    		}
    		
    		if ( !bAllFilled ) {
    			continue;
    		}
    		
//    		for (int j = 0; j < nrn; ++j)
//    			a_rems[i][j].calcBounds();
    		
    		/* Verify geometric relations */
//    		boolean bAllGeomRelVerified = true;
    		if ( nrn > 0 ) {    		
	    		float [] t_geomScores = new float[nrn];
		    	for (int j = 0; j < nrn; ++j) {
		    		
		    		/* Assume: there is only one head 
		    		 * TODO: Make more general */
		    		/* TODO: Deal with the case in which the remaining node is a Terminal (T) */

                    float terminalMultiplier = 1.0f;
		    		if ( this.rhsIsTerminal[j + 1] ) {
		    			/* TODO: Get the type of string, e.g, 1 -> DIGIT, ( -> BRACKET_L. 
		    			 *       May need to add terminal set as an input argument. */
		    			CWrittenTokenSetNoStroke tTokenSet = a_rems[i][j];
		    			int t_nTokens = tTokenSet.nTokens();
		    			if ( t_nTokens != 1 ) {
//		    				t_geomScores[j] = 0.0f; //TODO: Think about whether throwing an exception makes more sense. MATH_FUNCTION_NAME has trouble with the exception paradigm
                            terminalMultiplier = 0.0f;
		    			}

		    			/* TODO: Accommodate terminal name types (e.g., "TERMINAL(s)") */
                        // TODO: NodeToken
		    			if ( terminalSet.match(tTokenSet.tokens.get(0).getRecogResult(), this.rhs[j + 1]) ) {
//		    				t_geomScores[j] = 1.0f;
		    			}
	    				else {
//	    					t_geomScores[j] = 0.0f;
                            terminalMultiplier = 0.0f;
	    				}
		    		}
//		    		else {

                    if ( geomRels[j + 1] == null ) {
                        t_geomScores[j] = 1.0f;
                        continue;
                    }

                    float [] t_t_geomScores = new float[geomRels[j + 1].length];
                    for (int k = 0; k < geomRels[j + 1].length; ++k) {
                        int idxInRel = geomRels[j + 1][k].idxInRel[0];
                        float [] bndsInRel;
                        if ( idxInRel == 0 ) {
                            bndsInRel = tokenSet.getTokenBounds(iHead);
                        }
                        else {
                            bndsInRel = a_rems[i][idxInRel - 1].getSetBounds();
                        }

                        float v = geomRels[j + 1][k].verify(a_rems[i][j], bndsInRel);

                        t_t_geomScores[k] = v;

                    }

                    t_geomScores[j] = MathHelper.mean(t_t_geomScores) * terminalMultiplier;


//		    		}
		    	}

		    	geomScores[i] = MathHelper.mean(t_geomScores);
    		}
    		else {
    			/* This is the case in which the entire token is the head, 
    			 * and the head is an NT. 
    			 */
    			if ( !rhsIsTerminal[0] ) {
                    geomScores[i] = flagNTNeedsParsing;	/* 2.0f is a flag that indicates further geometric parsing is necessary */
                } else {
                    geomScores[i] = 1.0f;
                }
    		}
	    }

	    /* Find the partition that leads to the maximum geometric score */
	    int idxMax = MathHelper.indexMax(geomScores);
	    maxGeomScore[0] = geomScores[idxMax];
	    
	    /* For head */
	    /* TODO: Replace with constructor */
	    CWrittenTokenSetNoStroke headTokenSet = new CWrittenTokenSetNoStroke(tokenSet, iHead);
	    
	    remainingSets[0] = headTokenSet;
	    
	    /* For non-head */
	    for (int i = 0; i < a_rems[idxMax].length; ++i) {
	    	remainingSets[i + 1] = a_rems[idxMax][i];		//PerfTweak new
	    }
	    
	    Node n = new Node(lhs, sumString, rhs);
	    
	    createHeadChild(n, headTokenSet.getSetBounds()); /* TODO */
	    
	    return n;
	}
	
	/* Factory method */
	public static GraphicalProduction genFromStrings(ArrayList<String> strs, TerminalSet termSet)
		throws Exception 
	{		
		final int expectedNumNonRhsLines = 4;
				
		if ( strs.size() <= expectedNumNonRhsLines ) {
			throw new RuntimeException("Incorrect number of lines (" + strs.size() 
					                   + ") for creating new graphical production");
		}
		
		String t_lhs;
		AssocType t_assocType = AssocType.NoAssoc;
		String t_assocName = "";
		String headLine = strs.get(0).trim();
		
//		if ( headLine.contains(tokenRelSeparator) ) 
//			throw new Exception("Head node unexpectedly contains geometric relation(s)");
		
		int nLBs = TextHelper.numInstances(headLine, "(");
		int nRBs = TextHelper.numInstances(headLine, ")");
		
		if ( nLBs == 1 && nRBs == 1 ) {	/* Bias is included */
            int iLB = headLine.indexOf("(");
            int iRB = headLine.indexOf(")");
            if (iLB > iRB)
                throw new Exception("Syntax error in line: \"" + headLine + "\": Wrong order of the left and right brackets");

            String assocStr = headLine.substring(iLB + 1, iRB).trim();
            int[] idxColon = TextHelper.findAll(assocStr, ":");
            if (idxColon.length != 2)
                throw new Exception("Syntax error in line: \"" + headLine + "\": Number of colons is not equal to two");

            String assocHeaderStr = assocStr.substring(0, idxColon[0]).trim();
            if (!assocHeaderStr.equals("ASSOC"))
                throw new Exception("Syntax error in line: \"" + headLine + "\": The string preceding the first colon is not as expected");

            String assocTypeStr = assocStr.substring(idxColon[0] + 1, idxColon[1]).trim();
            if (assocTypeStr.equals("ASSOC_LEFT_2A")) {
                t_assocType = AssocType.AssocLeft2A;
            } else if (assocTypeStr.equals("ASSOC_RIGHT_2B")) {
                t_assocType = AssocType.AssocRight2B;
            } else if (assocTypeStr.equals("ASSOC_LEFT_3B")) {
                t_assocType = AssocType.AssocLeft3B;
            } else {
                throw new Exception("Unrecognized association type: " + assocTypeStr);
            }
			
			t_assocName = assocStr.substring(idxColon[1] + 1, assocStr.length()).trim();
			if ( t_assocName.length() == 0 )
				throw new Exception("Syntax error in line: \"" + headLine + "\": The association name is empty");
			
			t_lhs = headLine.substring(0, iLB).trim();
		}
		else {
			if ( !( nLBs == 0 && nRBs == 0 ) )
				throw new Exception("Syntax error in line: \"" + headLine + "\"");
			
			t_lhs = headLine;
		}		
		
		int t_nrhs = strs.size() - expectedNumNonRhsLines;
		String [] t_rhs = new String[t_nrhs];
		boolean [] t_bt = new boolean[t_nrhs];
		GeometricRelation [][] t_geomRels = new GeometricRelation[t_nrhs][];
		
		String [] t_stringizeInstr = null;
		String [] t_mathTexInstr = null;
		String [] t_evalInstr = null;
		
		for (int k = 0; k < t_nrhs; ++k) {
			String line = strs.get(k + 1);
			
			if ( k == 0 ) {
				/* This is the head node, no geometrical relation is expected */	
				/* We also need to extract the BiasType, if it exists */ 
				
				t_rhs[k] = line.trim();
			}
			else {
				if ( line.contains(tokenRelSeparator) ) {
					t_rhs[k] = line.split(tokenRelSeparator)[0].trim();
					
					String relString = line.split(tokenRelSeparator)[1].trim();
					if ( !relString.startsWith("{") || !relString.endsWith("}") )
						throw new Exception("Syntax error in input productions file"); 
					relString = relString.substring(1, relString.length() - 1);
					
					String [] relItems = relString.split(relSeparator);
					
					t_geomRels[k] = new GeometricRelation[relItems.length];
					for (int j = 0; j < relItems.length; ++j) {
						String relStr = relItems[j].trim();
						if ( relStr.startsWith("Align") ) {
							t_geomRels[k][j] = AlignRelation.createFromString(relStr, k);
						}
						else if ( relStr.startsWith("Position") ) {
							t_geomRels[k][j] = PositionRelation.createFromString(relStr, k);
						}
						else if ( relStr.startsWith("Height") ) {
							t_geomRels[k][j] = HeightRelation.createFromString(relStr, k);
						}
						else if ( relStr.startsWith("Width") ) {
							t_geomRels[k][j] = WidthRelation.createFromString(relStr, k);
						}
						else if ( relStr.startsWith("Spacing") ) {
							t_geomRels[k][j] = SpacingRelation.createFromString(relStr, k, termSet);
						}						
						else { 
							throw new Exception("Unrecognized geometric relation string: " + relStr);
						}
					}
				}
				else {
//					if ( line.trim().equals(TerminalSet.epsString) )
//						t_rhs[k] = TerminalSet.epsString;
//					else
					throw new RuntimeException("Encountered a non-head node with no geometric relations specified");
				}
			}
			
			t_bt[k] = termSet.isTypeTerminal(t_rhs[k]);
		}
		
		/* Parse stringizeInst: stringization instructions */
		String tline = strs.get(strs.size() - 3).trim();
		if ( !tline.startsWith(strinigzationTag) ) {
			throw new RuntimeException("Cannot find stringization tag in line: \"" + tline + "\"");
		}
		tline = tline.replace("\t", " ");
		ArrayList<String> listItems = new ArrayList<String>();
		String [] t_items = tline.split(" ");
		
		for (int n = 1; n < t_items.length; ++n) {
			if ( t_items[n].length() > 0 ) {
				listItems.add(t_items[n]);
			}
		}
		
		t_stringizeInstr = new String[listItems.size()];
		listItems.toArray(t_stringizeInstr);
		
		/* Parse mathTeX instructions */
		tline = strs.get(strs.size() - 2).trim();
		if ( !tline.startsWith(mathTexTag) ) {
			throw new RuntimeException("Cannot find math TeX tag in line: \"" + tline + "\"");
		}
		tline = tline.replace("\t", " ");
		
		ArrayList<String> mtListItems = new ArrayList<String>();
		String [] t_mtItems = tline.split(" ");
		
		for (int n = 1; n < t_mtItems.length; ++n) {
			if ( t_mtItems[n].length() > 0 ) {
				mtListItems.add(t_mtItems[n]);
			}
		}
		
		t_mathTexInstr = new String[mtListItems.size()];
		mtListItems.toArray(t_mathTexInstr);	
		
		/* Parse evalInstr: evaluation instructions */
		tline = strs.get(strs.size() - 1).trim();
		if ( !tline.startsWith(evalTag) ) {
			throw new RuntimeException("Cannot find evaluation tag in line: \"" + tline + "\"");
		}
		
		String evalStr = tline.replace(evalTag, "").trim();
		nLBs = TextHelper.numInstances(evalStr, "(");
		nRBs = TextHelper.numInstances(evalStr, ")");
		
		ArrayList<String> evalItems = new ArrayList<String>();
		if ( nLBs == 0 && nRBs == 0 ) {
			evalItems.add("PASS");
			if ( evalStr.contains(" ")  || evalStr.contains("\t") )
				throw new Exception("Syntax error in evaluation instruction: \"" + evalStr + "\"");
			evalItems.add(evalStr);
		}
		else if ( nLBs == 1 && nRBs == 1 ) {
			String funcName = evalStr.substring(0, evalStr.indexOf("("));
			evalItems.add(funcName);
			
			String argsStr = evalStr.substring(evalStr.indexOf("(") + 1, evalStr.indexOf(")")).trim();
			argsStr = argsStr.replace("\t", " ").replace(",", " ");
			String [] argsItems = argsStr.split(" ");
			for (int m = 0; m < argsItems.length; ++m)
				if ( argsItems[m].length() > 0 )
					evalItems.add(argsItems[m]);
		}
		
		t_evalInstr = new String[evalItems.size()];
		evalItems.toArray(t_evalInstr);
			
		
		/* Sanity check for association types */
		/* TODO */
		if ( t_assocType == AssocType.AssocLeft2A || 
			 t_assocType == AssocType.AssocRight2B) {
			if ( t_rhs.length != 2 )
				throw new RuntimeException("Under the current association type, the number of rhs (" + t_rhs.length + ") is incorrect.");
//			if ( !t_rhs[0].equals(t_rhs[1]) )
//				throw new RuntimeException("Under the current association type, it is unacceptable that the 1st and 2nd RHS items are different");
		}
		else if ( t_assocType == AssocType.AssocLeft3B ) {
			if ( t_rhs.length != 3 )
				throw new RuntimeException("Under the current association type, the number of rhs (" + t_rhs.length + ") is incorrect.");
			if ( !t_rhs[1].equals(t_rhs[2]) )
				throw new RuntimeException("Under the current association type, it is unacceptable that the 2nd and 3rd RHS items are different");
		}
				
		return new GraphicalProduction(t_lhs, t_rhs, t_bt, termSet, t_geomRels, 
									   t_assocType, t_assocName, 
									   t_stringizeInstr, t_mathTexInstr, t_evalInstr);
	}
	
	public int getNumNonHeadTokens() {
		return nrhs - 1;
	}
	
	@Override
	public String toString() {
		String s = "GP: ";
		if ( sumString != null )
			s += sumString;
		return s;
	}
	
}

