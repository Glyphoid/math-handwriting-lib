package me.scai.parsetree;

/* Exception classes */
public class ParseTreeEvaluatorException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public ParseTreeEvaluatorException(String msg) { super(msg); } 
}

class UndefinedFunctionException extends ParseTreeEvaluatorException {
	private static final long serialVersionUID = 1L;
	
	public UndefinedFunctionException(String msg) { super(msg); }
}

class UnexpectedTypeException extends ParseTreeEvaluatorException {
	private static final long serialVersionUID = 1L;
	
	public UnexpectedTypeException(String msg) { super(msg); }
};

class ParseTreeMathException extends ParseTreeEvaluatorException {
	private static final long serialVersionUID = 1L;
	
	public ParseTreeMathException(String msg) { super(msg); }
};

class DivisionByZeroException extends ParseTreeMathException {
	private static final long serialVersionUID = 1L;
	
	public DivisionByZeroException() { super("Division by zero"); }
};

class ZeroToZerothPowerException extends ParseTreeMathException {
	private static final long serialVersionUID = 1L;
	
	public ZeroToZerothPowerException() { super("Attempt to calculate zero to the zeroth power"); }
}

class SquareRootOfNegativeException extends ParseTreeMathException {
	private static final long serialVersionUID = 1L;
	
	public SquareRootOfNegativeException(String msg) { super(msg); }
}

class LogarithmOfNonPositiveException extends ParseTreeMathException {
	private static final long serialVersionUID = 1L;
	
	public LogarithmOfNonPositiveException(String msg) { super(msg); }
}

class InvalidArgumentForMatrixOperation extends ParseTreeMathException {
	private static final long serialVersionUID = 1L;
	
	public InvalidArgumentForMatrixOperation(String msg) { super(msg); }
}

/* ~Exception classes */
