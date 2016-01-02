package me.scai.parsetree;

import java.util.HashMap;
import java.util.Map;

public class ParseTreeStringizer {

	/* Constants */
    public final static String STRINGIZATION_FAILED_STRING = "[Stringization failed due to syntax error]";

    /* Member variables */
	private Map<String, String []> sumString2InstrMap = new HashMap<>(); 		/* For stringization to plain computer math notation */
	private Map<String, String> specialStringMap = new HashMap<>();

	private Map<String, String> specialTerminalNameMap = new HashMap<>();

	/* ~Member variables */

	/* Methods */

	/* Constructor */
	public ParseTreeStringizer(final GraphicalProductionSet gpSet) {
		sumString2InstrMap.clear();
		specialStringMap.clear();

		/* Create map of special strings: Strinigization instructions */
		specialStringMap.put("_SPACE_", " ");
        specialStringMap.put("_UNDERSCORE_", "_");
		specialStringMap.put("_OPEN_PAREN_", "(");
		specialStringMap.put("_CLOSE_PAREN_", ")")

		/* Create map of special mapping from terminal names in grammar definition to those in the
		 * stringization results */ ;
		// TODO: Externalize
		specialTerminalNameMap.put("lt", "<");
		specialTerminalNameMap.put("gt", ">");
		specialTerminalNameMap.put("lte", "<=");
		specialTerminalNameMap.put("gte", ">=");

		for (int i = 0; i < gpSet.prods.size(); ++i) {
			GraphicalProduction gp = gpSet.prods.get(i);

			String t_sumString = gp.sumString;
			String [] t_instr = gp.stringizeInstr;

			sumString2InstrMap.put(t_sumString, t_instr);
		}
	}

	/* Input: n: root of the parse tree */
	/* Currently based on recursion. */
	public String stringize(Node n) {
		if ( n == null ) {
			return STRINGIZATION_FAILED_STRING;
		}

		String s = "";

		String prodSumString = n.prodSumString;
		String [] instr = sumString2InstrMap.get(prodSumString);
		if ( instr == null ) {
			throw new RuntimeException("Cannot find the stringization instruction for: "
		                               + n.prodSumString);
		}

		for (int i = 0; i < instr.length; ++i) {
			if ( specialStringMap.containsKey(instr[i]) ) { /* Special string */
				s += specialStringMap.get(instr[i]);
			}
			else if ( instr[i].startsWith("n") ) { /* String content from the children nodes */
				int iNode = Integer.parseInt( instr[i].substring(1, instr[i].length()) );
				if ( iNode < 0 || iNode >= n.nc ) {
					throw new RuntimeException("Node index (" + iNode
							                   + ") exceeds number of children ("
							                   + n.nc + ")");
				}

				if ( n.ch[iNode].isTerminal() ) {
					Node chNode = n.ch[iNode];
					final String termName = chNode.termName;
					final String mappedTermName = specialTerminalNameMap.containsKey(termName) ?
							                      specialTerminalNameMap.get(termName) :
							                      termName;

					s += mappedTermName;
				}
				else {
					s += stringize(n.ch[iNode]);
				}
			}
			else {	/* Hard-coded string content */
				s += instr[i];
			}

		}

		return s;
	}

	/* ~Methods */
}
