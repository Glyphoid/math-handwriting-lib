package me.scai.parsetree.evaluation.exceptions;

public class SquareRootOfNegativeException extends ParseTreeMathException {
	private static final long serialVersionUID = 1L;

	public SquareRootOfNegativeException() {
		super("Square root of a negative number (complex number support is not available yet)");
	}
}
