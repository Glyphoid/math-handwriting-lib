package me.scai.parsetree.evaluation.exceptions;

public class ParseTreeEvaluatorException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public ParseTreeEvaluatorException(String msg) {
		super("Evaluation error: " + msg);
	}
}

