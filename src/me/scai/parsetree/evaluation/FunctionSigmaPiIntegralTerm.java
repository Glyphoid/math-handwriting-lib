package me.scai.parsetree.evaluation;

import java.util.List;

import me.scai.parsetree.Node;

/* Parent class for functions, sigma summation, pi production and integral terms */
public abstract class FunctionSigmaPiIntegralTerm { /* For the lack of a better name ... */
	/* Member variables */
	protected FunctionArgumentList argList;
	protected List<String> argNames;

	Node body; /* Function body that preserves the original variable names */
	Node evalBody; /* Function body for evaluation purpose. Variable names are internal temporary ones */
	/* ~Member variables */

	/* Constructor */
	public FunctionSigmaPiIntegralTerm(FunctionArgumentList tArgList) {
		argList = tArgList; /* TODO: Check to see if allSymbols() is true */

		if (argList.allSymbols()) {
			argNames = argList.getSymbolNames();
		}
	}

	/* Concrete methods */
	public void defineBody(Node tBody) {
		body = tBody;
		evalBody = new Node(tBody); /* Use the copy constructor of Node */
		/* TODO: This is probably not the best option. Why would you duplicate and 
		 *       DFS-traverse a function body just to accommodate variable name changes? 
		 *       Can build a cache of the argument name nodes to speed up the  
		 *       name changes and avoid duplication. 
		 */
	}

	public boolean isDefined() {
		return (body != null);
	}

	// /* Abstract methods */
	// abstract Object evaluate(ParseTreeEvaluator evaluator,
	// FunctionArgumentList argValueList)
	// throws ParseTreeEvaluatorException;

	public FunctionArgumentList getArgumentList() {
		return argList;
	}
	
	public void setArgNames(List<String> tArgNames) {
	    this.argNames = tArgNames;
	}
	
	public List<String> getArgNames() {
	    return this.argNames;
	}
}