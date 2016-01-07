package me.scai.parsetree.evaluation.exceptions;

public class DivisionByZeroException extends ParseTreeMathException {
	private static final long serialVersionUID = 1L;

	public DivisionByZeroException() {
		super("Division by zero");
	}
}
