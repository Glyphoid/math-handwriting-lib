package me.scai.parsetree.evaluation.exceptions;

public class UndefinedFunctionException extends ParseTreeEvaluatorException {
	private static final long serialVersionUID = 1L;

	public UndefinedFunctionException(String funcName) {
		super("Undefined function \"" + funcName + "\"");
	}
}
