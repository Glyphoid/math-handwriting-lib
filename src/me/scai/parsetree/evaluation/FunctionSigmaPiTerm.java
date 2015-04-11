package me.scai.parsetree.evaluation;

import java.util.List;

import me.scai.parsetree.Node;

public abstract class FunctionSigmaPiTerm { /* For the lack of a better name ... */
	/* Member variables */
	protected FunctionArgumentList argList;
	protected List<String> argNames;

	Node body; /* Function body */

	/* ~Member variables */

	/* Constructor */
	public FunctionSigmaPiTerm(FunctionArgumentList tArgList) {
		argList = tArgList; /* TODO: Check to see if allSymbols() is true */

		if (argList.allSymbols()) {
			argNames = argList.getSymbolNames();
		}
	}

	/* Concrete methods */
	public void defineBody(Node tBody) {
		body = tBody;
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
}