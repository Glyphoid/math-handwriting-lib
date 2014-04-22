package me.scai.parsetree;

public class Node {
	private static final int prodNumTerminal = -1;
	
	boolean isTerminal = true;
	int prodNum = prodNumTerminal;	/* Production number: -1 means terminal */
	int nc = 0; 					/* Number of children */
	
	Node p = null;					/* Parent */
	Node [] ch = null;				/* Children */
	
	/* Constructors */
	/* Default constructor: terminal node */
	public Node() {
		isTerminal = true;
		prodNum = prodNumTerminal;
		nc = 0;
		ch = null;
		p = null;
	}
	
	/* Terminal node with the parent specified */
	public Node(Node t_p) {
		this();
		p = t_p;
	}
	
	/* Non-terminal (NT) node with production number, parent and children specified */
	public Node(int t_prodNum, Node t_p, Node [] t_ch) {
		assert(prodNum >= 0);
		
		isTerminal = false;
		prodNum = t_prodNum;
		p = t_p;
		nc = t_ch.length;
		ch = t_ch;		
	}
	
	/* Non-terminal (NT) node with production number and children specified */
	public Node(int t_prodNum, Node [] t_ch) {
		assert(prodNum >= 0);
		
		isTerminal = false;
		prodNum = t_prodNum;
		p = null;
		nc = t_ch.length;
		ch = t_ch;		
	}
	
}
