package me.scai.parsetree.geometry;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import me.scai.parsetree.Node;
import me.scai.parsetree.TerminalSet;

public class NodeInternalGeometry {
	/* Member variables */
	private TerminalSet termSet;
	public final static List<String> majorTerminalTypes = new ArrayList<String>();
	/* ~Member variables */
	
	/* Constructor */
	public NodeInternalGeometry(TerminalSet tTermSet) {
		termSet = tTermSet;
		
		majorTerminalTypes.add("DIGIT");
		majorTerminalTypes.add("VARIABLE_SYMBOL");
	}
	
	/* Methods */
	
	/* Get the sizes of all major tokens (as defined in "majorTokenTypes") under the node */
	public List<float []> getMajorTokenBounds(Node node) {
		/* Uses a recursive algorithm */
		if ( node == null ) {
			return null;
		}
					
		List<float []> b = new LinkedList<float []>();
					
		for (int i = 0; i < node.ch.length; ++i) {
			Node chNode = node.ch[i]; /* Child node */			
			
			if (chNode.isTerminal()) {

                List<String> termTypes = termSet.getTypeOfToken(chNode.termName);
                boolean typeMatch = false;

                if (termTypes != null) {
                    for (String termType : termTypes) {
                        if (majorTerminalTypes.contains(termType)) {
                            typeMatch = true;
                            break;
                        }
                    }
                }

                if (typeMatch) {
                    b.add(chNode.getBounds());
                }
			}
			else {
				List<float []> tBounds = getMajorTokenBounds(chNode);
				b.addAll(tBounds);
			}
		}
		
		return b;
	}
	
	public float getMaxMajorTokenWidth(Node node) {
		float maxWidth = Float.NEGATIVE_INFINITY;
		
		List<float []> allBounds = getMajorTokenBounds(node);
		if (allBounds == null) {
			return maxWidth;
		}
		
		for (float [] bounds : allBounds) {
			float w = bounds[3] - bounds[1];
			
			if (w > maxWidth) {
				maxWidth = w;
			}
		}
		
		return maxWidth;
	}
	
	public boolean isTerminalTypeMajor(List<String> termTypes) {
        boolean match = false;

        if (termTypes != null) {
            for (String termType : termTypes) {
                if (majorTerminalTypes.contains(termType)) {
                    match = true;
                    break;
                }
            }
        }

        return match;
	}
}
