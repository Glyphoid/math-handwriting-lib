package me.scai.parsetree;

import java.util.HashMap;

public class ParseTreeMathTexifier {
	/* Member variables */
	/* Constants */
	public final static String parsingErrString = "[Parsing failed: Syntax error]";
    public final static String MATH_TEXIFICATION_FAILED_STRING = "[Conversion to Math Tex failed]";
	
	private HashMap<String, String []> sumString2MathTexInstrMap = new HashMap<String, String []>();/* For stringization to Math TeX noatation */
	private HashMap<String, String> terminal2TexNotationMap = new HashMap<String, String>();
	/* ~Member variables */
	
	/* Constructor */
	public ParseTreeMathTexifier(final GraphicalProductionSet gpSet, 
			                     final TerminalSet termSet) {	
		sumString2MathTexInstrMap.clear();
		
		for (int i = 0; i < gpSet.prods.size(); ++i) {
			GraphicalProduction gp = gpSet.prods.get(i);
			
			String t_sumString = gp.sumString;
			String [] t_instr = gp.mathTexInstr;
			
			sumString2MathTexInstrMap.put(t_sumString, t_instr);
		}
		
		/* Connect map for Math TeX notations */
		terminal2TexNotationMap = termSet.token2TexNotationMap;
	}
	
	/* Get the function name out of a instruction item such as:
	 *   "GET_VAR_TEX_NOTATION(n0)" */
	static String getTexFunctionName(String item) {
		if (item.indexOf("(") == -1 || item.indexOf(")") == -1) {
			return null;
		}
		else {
			String funcName = item.substring(0, item.indexOf("("));
			
			return funcName;
		}
	}	
	
	/* Get the function name out of a instruction item such as:
	 *   "GET_VAR_TEX_NOTATION(n0)" */
	static int [] getTexFunctionArgIndices(String item) {
		if (item.indexOf("(") == -1 || item.indexOf(")") == -1) {
			return null;
		}
		else {
			String funcArgsStr = item.substring(item.indexOf("(") + 1, item.indexOf(")"));
			
			String [] funcArgs = funcArgsStr.split(",");
			int [] argIndices = new int[funcArgs.length];
			
			for (int i = 0; i < argIndices.length; ++i) {
				String funcArg = funcArgs[i].trim();
				
				argIndices[i] = Integer.parseInt(funcArg.replace("n",  ""));
			}
			
			return argIndices;
		}
	}
	
	/* Input: n: root of the parse tree */
	/* Currently based on recursion. */
	public String texify(Node n) {
		if ( n == null ) {
			return MATH_TEXIFICATION_FAILED_STRING;
		}
					
		String s = "";
			
		String prodSumString = n.prodSumString;
		String [] instr = sumString2MathTexInstrMap.get(prodSumString);
		if ( instr == null ) {
			throw new RuntimeException("Cannot find the stringization instruction for: " 
		                               + n.prodSumString);
		}
		
		for (int i = 0; i < instr.length; ++i) {
			String instrItem = instr[i];
			
			String texFunctionName = getTexFunctionName(instrItem);
			
			if ( texFunctionName != null ) { /* Special string */
				int [] chIndices = getTexFunctionArgIndices(instrItem);
				int chIdx = chIndices[0];
				
				switch (texFunctionName) {	
				case "GET_TEX_VAR_NOTATION":
					s += getTexVarNotation(n.ch[chIdx].termName);
					break;
				case "GET_TEX_PLUS_OP":
					s += getTexPlusOp(n.ch[chIdx].termName);
					break;
				case "GET_TEX_MINUS_OP":
					s += getTexMinusOp(n.ch[chIdx].termName);
					break;
				case "GET_TEX_MULTIPLY_OP":
					s += getTexMultiplyOp(n.ch[chIdx].termName);
					break;
				case "GET_TEX_ASSIGN_OP":
					s += getTexAssignOp(n.ch[chIdx].termName);
					break;
                case "GET_TEX_COMPARATOR_OP":
                    s += getTexComparatorOP(n.ch[chIdx].termName);
                    break;
				default:
					throw new RuntimeException("Unrecognized function name for TeXification: \"" + texFunctionName + "\"");					
				}
//				s += invokeTexFunction(texFunctionName, texFunctionNodeIdx); /* TODO: Use reflection */
			}
			else if ( instrItem.startsWith("n") ) { /* String content from the children nodes */
				int iNode = Integer.parseInt( instrItem.substring(1, instrItem.length()) );
				if ( iNode < 0 || iNode >= n.nc ) {
					throw new RuntimeException("Node index (" + iNode 
							                   + ") exceeds number of children (" 
							                   + n.nc + ")");
				}
				
				if ( n.ch[iNode].isTerminal() ) {
					s += getTexNotation(n.ch[iNode].termName);
				}
				else {
					s += texify(n.ch[iNode]);
				}
			}
			else {	/* Hard-coded string content */
				s += instrItem;
			}
				
		}
		
		return s;
	}
	
	/* Get the Math TeX notation from terminal name */
	private String getTexNotation(String term) {
		boolean contains = terminal2TexNotationMap.containsKey(term);
		
		if (contains) {
			return terminal2TexNotationMap.get(term);
		}
		else {
			return term;
		}
	}

	private String getTexVarNotation(String term) {
		return getTexNotation(term);
	}

	private String getTexMultiplyOp(String term) {
		if (term.equals("*")) {
			return "\\ast";
		}
		else if (term.equals("X")) {
			return "\\times";
		}
		else {
			return term;
		}
	}

	private String getTexPlusOp(String term) {
		return term;
	}

	private String getTexMinusOp(String term) {
		return term;
	}

	private String getTexAssignOp(String term) {
		return term;
	}

	private String getTexComparatorOP(String term) {
        String texOp = null;

		switch (term) {
            case "lt":
                texOp = "<";
                break;
            case "gt":
                texOp = ">";
                break;
            case "lte":
                texOp = "\\leq";
                break;
            case "gte":
                texOp = "\\geq";
                break;
            default:
                throw new IllegalArgumentException("Unrecognized comparator: \"" + term + "\"");
        }

        return texOp;
	}
}
