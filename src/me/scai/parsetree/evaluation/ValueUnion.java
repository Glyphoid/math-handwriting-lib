package me.scai.parsetree.evaluation;

import Jama.Matrix;

class ValueUnion {
	public enum ValueType {
		Double, Matrix, UserFunction
	}

	private ValueType valueType;
	private Object value;

	/* Constructors */
	public ValueUnion(double dv) {
		valueType = ValueType.Double;
		value = dv;
	}

	public ValueUnion(Matrix mv) {
		valueType = ValueType.Matrix;
		value = mv;
	}

	public ValueUnion(FunctionTerm funcTerm) {
		valueType = ValueType.UserFunction;
		value = funcTerm;
	}

	/* Value getters */
	public Object get() {
		return value;
	}

	public double getDouble() {
		if (valueType != ValueType.Double) {
			throw new RuntimeException("Incorrect value type");
		}

		return (Double) value;
	}

	public Matrix getMatrix() {
		if (valueType != ValueType.Matrix) {
			throw new RuntimeException("Incorrect value type");
		}

		return (Matrix) value;
	}

	public FunctionTerm getUserFunction() {
		if (valueType != ValueType.UserFunction) {
			throw new RuntimeException("Incorrect value type");
		}

		return (FunctionTerm) value;
	}
}