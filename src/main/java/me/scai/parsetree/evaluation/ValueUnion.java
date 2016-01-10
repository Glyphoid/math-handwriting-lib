package me.scai.parsetree.evaluation;

import Jama.Matrix;
import me.scai.parsetree.ParseTreeStringizer;
import me.scai.parsetree.evaluation.matrix.MatrixHelper;
import org.jscience.physics.amount.Amount;

public class ValueUnion {
	public enum ValueType {
        Undefined,      // Undefined type, used in case such as: unsatisfied logical predicament in an "if" expression
		Boolean,
		Double,
        Matrix,
        UserFunction,
        PhysicalQuantity
	}

	private ValueType valueType;
	private Object value;
    private String description = "";

	/* Constructors */
    /* Constructor for Undefined type */
    public ValueUnion(Undefined undefined) {
        assert(undefined != null);

        valueType = ValueType.Undefined;
        value = undefined;
    }

    /* Constructor for Boolean type */
    public ValueUnion(boolean bv) {
        valueType = ValueType.Boolean;
        value = bv;
    }

    /* Constructor for double type */
	public ValueUnion(double dv) {
		valueType = ValueType.Double;
		value = dv;
	}

    public ValueUnion(double dv, String description) {
        valueType = ValueType.Double;
        value = dv;
        this.description = description;
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

    public ValueUnion(final Amount physicalAmount, final String description) {
        this.valueType   = ValueType.PhysicalQuantity;
        this.value       = physicalAmount;
        this.description = description;
    }

	/* Value getters */
	public Object get() {
		return value;
	}

    // Note: The Undefined type does not require a dedicated value getter because the value always points to the same
    // singleton object

    public boolean getBoolean() {
        if (valueType == ValueType.Boolean) {
            return (Boolean) value;
        } else {
            throw new RuntimeException("Incorrect value type for getDouble(): " + ValueType.Double.toString());
        }
    }

	public double getDouble() {
		if (valueType == ValueType.Double) {
            return (Double) value;
        } else if (valueType == ValueType.PhysicalQuantity) {
            Amount valueAmount = (Amount) value;
            return valueAmount.getEstimatedValue();
		} else {
            throw new RuntimeException("Incorrect value type for getDouble(): " + ValueType.Double.toString());
        }

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

    public String getDescription() {
        return description;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public String getValueString(ParseTreeStringizer stringizer) {
        String valStr;

        if (valueType == ValueType.Boolean) {
            valStr = Boolean.toString((Boolean) value);

        } else if (valueType == ValueType.Double) {
            valStr = Double.toString((Double) value);

        } else if (valueType == ValueType.Matrix) {
            valStr = MatrixHelper.matrix2String((Matrix) value);

        } else if (valueType == ValueType.UserFunction) {
            assert(valueType != null);

            valStr = ((FunctionTerm) value).getFullDefinition(stringizer);

        } else if (valueType == ValueType.PhysicalQuantity) {
            valStr = ((Amount) value).toString();

        } else {
            throw new IllegalStateException("Encountered unexpected value type when trying to generate string representation of ValueUnion");
        }

        return valStr;
    }

}