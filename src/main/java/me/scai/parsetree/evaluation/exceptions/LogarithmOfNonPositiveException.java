package me.scai.parsetree.evaluation.exceptions;

public class LogarithmOfNonPositiveException extends ParseTreeMathException {
	private static final long serialVersionUID = 1L;

	public LogarithmOfNonPositiveException() {
		super("Logarithm of a non-positive number (complex number support is not available yet)");
	}
}
