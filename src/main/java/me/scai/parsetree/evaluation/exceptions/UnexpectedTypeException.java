package me.scai.parsetree.evaluation.exceptions;

/**
 * Created by shanqing on 1/7/16.
 */
class UnexpectedTypeException extends ParseTreeEvaluatorException {
	private static final long serialVersionUID = 1L;

	public UnexpectedTypeException(String msg) { super(msg); }
}
