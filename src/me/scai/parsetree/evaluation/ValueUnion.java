package me.scai.parsetree.evaluation;

import Jama.Matrix;
import org.jscience.physics.amount.Amount;

public class ValueUnion {
	public enum ValueType {
		Double,
        Matrix,
        UserFunction,
        PhysicalQuantity
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

    public ValueUnion(Amount physicalAmount) {
        valueType = ValueType.PhysicalQuantity;
        value = physicalAmount;
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

    public Amount getPhysicalQuantity() {
        if (valueType != ValueType.PhysicalQuantity) {
            throw new RuntimeException("Incorrect value type");
        }

        return (Amount) value;
    }
}