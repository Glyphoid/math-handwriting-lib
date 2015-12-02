package me.scai.parsetree;

import java.util.Arrays;

public class Node {
//	private float x_min = 0.0f, y_min = 0.0f, x_max = 0.0f, y_max = 0.0f; /* Location information */	
	private boolean isTerminal = true;
	public String lhs;
	public String prodSumString;	/* Production summary string. See GraphicalProduction.sumString */
	public String termName; 		/* Terminal name: applies only to terminal nodes, e.g., EPS, 3 */
//	public String auxTermName;      /* Used by classes such as the evaluator to store auxiliary info 
//	                                   such as internal variable names during function calls */
	public String [] rhsTypes;		        /* Child types: applies only to non-terminal nodes */
	
	private float [] bounds;        /* {x_min, y_min, x_max, y_max} */
	
	private float geometricScore = 0.0f;
	
	int nc = 0; 					/* Number of children */
	
	Node p = null;					/* Parent */
	public Node [] ch = null;		/* Children */
	
	/* Constructors */
	/* Default constructor: terminal node */
	public Node() {
		isTerminal = true;
		prodSumString = null;
		
		nc = 0;
		ch = null;
		p = null;
	}
	
	/* Terminal node with the parent specified */
//	public Node(Node t_p) {
//		this();
//		p = t_p;
//	}
	
	/* Non-terminal (NT) node with production summary string, parent and children specified */
	public Node(String t_lhs, String t_prodSumString, Node t_p, Node [] t_ch) {
		assert(t_prodSumString.length() >= 0);
		
		lhs = t_lhs;
		isTerminal = false;
		prodSumString = t_prodSumString;
		p = t_p;
		nc = t_ch.length;
		ch = t_ch;		
	}
	
	/* Copy constructor */
	public Node(Node n0) {
	    this.isTerminal = n0.isTerminal;
	    this.lhs = n0.lhs;
	    this.prodSumString = n0.prodSumString;
	    this.termName = n0.termName;
	    
	    if (n0.rhsTypes != null) {
	        this.rhsTypes = Arrays.copyOf(n0.rhsTypes, n0.rhsTypes.length);
	    }
	    if (n0.bounds != null) {
	        this.bounds = Arrays.copyOf(n0.bounds, n0.bounds.length);
	    }
	    
	    this.geometricScore = n0.geometricScore;
	    this.nc = n0.nc;
	    
//	    this.p = n0.p;
	    /* TODO: This may be problematic. We want p to point to the copy, not the original. */
	    
	    if (n0.ch != null) {
    	    this.ch = new Node[n0.ch.length];
    	    
    	    for (int i = 0; i < this.ch.length; ++i) {
    	        if (n0.ch[i] != null) {
    	            this.ch[i] = new Node(n0.ch[i]);
    	            this.ch[i].p = this; /* Order of things matter. See comment above. */
    	        }
    	    }
	    }
	}
	
	/* Non-Terminal (NT) node with production summary string specified */
	public Node(String t_lhs, String t_prodSumString, String [] t_rhsTypes) {
		lhs = t_lhs;
		isTerminal = false;
		prodSumString = t_prodSumString;
		rhsTypes = t_rhsTypes;
		p = null;
//		nc = 0;
		nc = t_rhsTypes.length;
		ch = new Node[t_rhsTypes.length];
	}
	
	/* Terminal (T) node with production summary string specified */
	public Node(String t_lhs, String t_prodSumString, String t_termName, float [] t_bounds) {		
		lhs = t_lhs;
		isTerminal = true; /* Will be set to false when addChild() is called */
		prodSumString = t_prodSumString;
		
		if (TerminalSet.isTerminalNameType(t_termName)) {
			termName = TerminalSet.getTerminalNameTypeTokenName(t_termName);
		}
		else {
			termName = t_termName;
		}
		p = null;
		nc = 0;
		ch = null;
		
		bounds = t_bounds;
	}
	
	/* Non-terminal (NT) node with production summary string and children specified */
	public Node(String t_lhs, String t_prodSumString, Node [] t_ch) {
		assert(t_prodSumString.length() >= 0);
		
		lhs = t_lhs;
		isTerminal = false;
		prodSumString = t_prodSumString;
		p = null;
		nc = t_ch.length;
		ch = t_ch;		
	}
	
	/* Property getters */
	public boolean isTerminal() {
		return isTerminal;
	}
	
	public int numChildren() {
		return nc;
	}
	
	public void setChild(int ic, Node child) {
		ch[ic] = child;
	}
	
//	public void addChild(Node newChild) {
//		if ( isTerminal )
//			isTerminal = false;
//		
//		Node [] chOld = ch;
//		if ( chOld == null )
//			ch = new Node[1];
//		else
//			ch = new Node[chOld.length + 1];
//		
//		for (int i = 0; i < ch.length - 1; ++i)
//			ch[i] = chOld[i];
//		ch[ch.length - 1] = newChild;
//		
//		nc++;
//	}

	public void setRHSTypes(String [] t_rhsTypes) {
		rhsTypes = t_rhsTypes;
	}
	
	public String [] getRHSTypes() {
		return rhsTypes;
	}
	
	@Override
	public String toString() {
		String s = "Node (";
		if ( prodSumString != null ) {
			s += prodSumString;
		}
		s += ")";
		
		if ( isTerminal ) {
			s += "(T: " + termName + ")";
		}
		else {
			s += "(NT)";
		}
		
		return s;
	}
	
	public void setGeometricScore(float gs) {
		geometricScore = gs;
	}
	
	public float getGeometricScore() {
		return geometricScore;
	}

    public void setBounds(float[] bounds) {
        this.bounds = bounds;
    }

	public float [] getBounds() {
		return bounds;
	}
}
	