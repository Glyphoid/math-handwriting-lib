package me.scai.parsetree.evaluation.exceptions;

public class InvalidArgumentForMatrixOperation extends ParseTreeMathException {
	private static final long serialVersionUID = 1L;

	public InvalidArgumentForMatrixOperation(String op) {
		super("Invalid argument type for matrix operation \"" + op + "\"");
    }
}
