package me.scai.parsetree;

public class Node {
//	private static final int prodNumTerminal = -1;
	private float x_min = 0.0f, y_min = 0.0f, x_max = 0.0f, y_max = 0.0f; /* Location information */
	
	boolean isTerminal = true;
	String prodSumString = null;	/* Production summary string. See GraphicalProduction.sumString */
	String termName = null; 		/* Terminal name: applies only to terminal nodes, e.g., EPS, 3 */
	int nc = 0; 					/* Number of children */
	
	Node p = null;					/* Parent */
	Node [] ch = null;				/* Children */
	
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
	public Node(String t_prodSumString, Node t_p, Node [] t_ch) {
		assert(t_prodSumString.length() >= 0);
		
		isTerminal = false;
		prodSumString = t_prodSumString;
		p = t_p;
		nc = t_ch.length;
		ch = t_ch;		
	}
	
	/* Non-Terminal (NT) node with production summary string specified */
	public Node(String t_prodSumString) {
		isTerminal = false;
		prodSumString = t_prodSumString;
		p = null;
		nc = 0;
		ch = null;		
	}
	
	/* Terminal (T) node with production summary string specified */
	public Node(String t_prodSumString, String t_termName) {
		isTerminal = true; /* Will be set to false when addChild() is called */
		prodSumString = t_prodSumString;
		termName = t_termName;
		p = null;
		nc = 0;
		ch = null;		
	}
	
	/* Non-terminal (NT) node with production summary string and children specified */
	public Node(String t_prodSumString, Node [] t_ch) {
		assert(t_prodSumString.length() >= 0);
		
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
	
	public void addChild(Node newChild) {
		if ( isTerminal )
			isTerminal = false;
		
		Node [] chOld = ch;
		if ( chOld == null )
			ch = new Node[1];
		else			
			ch = new Node[chOld.length + 1];
		for (int i = 0; i < ch.length - 1; ++i)
			ch[i] = chOld[i];
		ch[ch.length - 1] = newChild;
		
		nc++;
	}

	
}
