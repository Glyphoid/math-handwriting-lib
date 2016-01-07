package me.scai.parsetree.evaluation.exceptions;

/**
 * Created by shanqing on 1/7/16.
 */
public class UnsupportedMatrixExponentException extends ParseTreeMathException {
    private static final long serialVersionUID = 1L;

    public UnsupportedMatrixExponentException() {
        super("Unsupported exponent on matrix base");
    }
}
