package me.scai.parsetree.evaluation;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.IllegalAccessException;

import Jama.Matrix;
import me.scai.parsetree.Node;
import me.scai.parsetree.GraphicalProduction;
import me.scai.parsetree.GraphicalProductionSet;
import me.scai.parsetree.scientific.ScientificConstants;
import org.jscience.physics.amount.Amount;

public class ParseTreeEvaluator {
    /* Constants */
    public static final String EVAL_FAILED_STRING = "[Evaluation failed]";

	/* Member variables */
	/* Constants */
	// final static String passFuncName = "PASS";
	private GraphicalProductionSet prodSet;

	Map<String, String> sumString2FuncNameMap = new HashMap<>();
	Map<String, int[]> sumString2NodeIdxMap = new HashMap<>();

	PlatoVarMap varMap = new PlatoVarMap();

	private LinkedList<String> lhsStack = new LinkedList<String>();
	private LinkedList<String[]> rhsStack = new LinkedList<String[]>();

	/* Stack for function evaluation */
	LinkedList<String> funcNameStack = new LinkedList<String>();             /* Stack of function names */
	LinkedList<String []> funcArgNamesStack = new LinkedList<String []>();   /* Stack of function argument variable names */  
	
	/* ~Member variables */

	/* Methods */

	/* Constructor */
	public ParseTreeEvaluator(GraphicalProductionSet gpSet) {
		prodSet = gpSet;

		for (int n = 0; n < gpSet.prods.size(); ++n) {
			GraphicalProduction gp = gpSet.prods.get(n);
			String sumString = gp.sumString;

			if (gp.evalInstr.length <= 1) {
				throw new RuntimeException(
						"Instruction set contains too few entries ("
								+ gp.evalInstr.length + ")");
			}

			sumString2FuncNameMap.put(sumString, gp.evalInstr[0]);

			int[] nodeIndices = new int[gp.evalInstr.length - 1];
			for (int m = 1; m < gp.evalInstr.length; ++m) {
				if (!gp.evalInstr[m].startsWith("n"))
					throw new RuntimeException(
							"Instruction set contains unrecognized node name: "
									+ gp.evalInstr[m]);

				int nodeIdx = Integer.parseInt(gp.evalInstr[m].substring(1,
						gp.evalInstr[m].length()));
				if (nodeIdx < 0 || nodeIdx >= gp.rhs.length)
					throw new RuntimeException("In evaluation instruction: \""
							+ gp.evalInstr[0]
							+ "\", a node index exceeds valid range");

				nodeIndices[m - 1] = nodeIdx;
			}

			sumString2NodeIdxMap.put(sumString, nodeIndices);
		}

        /* Put default scientific constants */
        ScientificConstants.inject2VariableMap(varMap);
	}
	
	public String evalRes2String(Object evalRes) {
        if (evalRes == null) {
            return EVAL_FAILED_STRING;
        }

	    String evalResStr = "";
        if (evalRes.getClass().equals(Matrix.class)) {
            evalResStr = matrix2String((Matrix) evalRes);
        } else if (evalRes.getClass().equals(FunctionTerm.class)) {
            evalResStr = ((FunctionTerm) evalRes).toString();
        } else {
            evalResStr = evalRes.toString();
        }
        
        return evalResStr;
	}

	public String eval2String(Node n) throws ParseTreeEvaluatorException {
		Object evalRes = eval(n);

		lhsStack.clear();
		rhsStack.clear();
		
		return evalRes2String(evalRes);
	}

	/* Convert a matrix to a string */
	private String matrix2String(Matrix m) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");

		int nr = m.getRowDimension();
		int nc = m.getColumnDimension();
		for (int i = 0; i < nr; ++i) {
			for (int j = 0; j < nc; ++j) {
				sb.append("" + m.get(i, j));
				if (j < nc - 1) {
					sb.append(", ");
				}
			}

			if (i < nr - 1) {
				sb.append("; ");
			}
		}

		sb.append("]");
		return sb.toString();
	}

	/* Trace direct ancestor and see if a certain production is included in it */
	private boolean hasDirectAncestor(String targProd) {
		Iterator<String> lhsIt = lhsStack.iterator();
		Iterator<String[]> rhsIt = rhsStack.iterator();

		if (!rhsIt.hasNext()) {
			return false;
		}
		rhsIt.next();

//		String prod = lhsStack.getFirst();
		while (lhsIt.hasNext()) {
			String lhs = lhsIt.next();
			if (lhs.equals(targProd)) {
				return true;
			}

			if (!rhsIt.hasNext()) {
				return false;
			}
			String[] rhs = rhsIt.next();

			if (!Arrays.asList(rhs).contains(lhs)) {
				return false;
			}

		}

		return false;
	}

	/* Peek lhs stack top */
	private String lhsStackTop() {
		return lhsStack.peekFirst();
	}

	/* Main method: eval: evaluate a parse tree */
	public Object eval(Node n) throws ParseTreeEvaluatorException {
        if (n == null) {
            return null;
        }

		String sumString = n.prodSumString;
		lhsStack.push(n.lhs);
		rhsStack.push(n.rhsTypes);

		String funcName = sumString2FuncNameMap.get(sumString);
		int[] argIndices = sumString2NodeIdxMap.get(sumString);
		int nArgs = argIndices.length;
		Object evalRes = null;

		if (funcName == null || argIndices == null) {
			throw new RuntimeException(
					"Cannot find evaluation instruction for production: "
							+ sumString);
		}

		@SuppressWarnings("rawtypes")
		Class[] argTypes = new Class[nArgs];
		for (int i = 0; i < nArgs; ++i) {
			argTypes[i] = Object.class;
		}

		try {
			String tFuncName = funcName.toLowerCase();
			Method m = this.getClass().getMethod(tFuncName, argTypes);

			Object[] args = new Object[nArgs];
			for (int j = 0; j < nArgs; ++j) {
				int chIdx = argIndices[j];

                // TODO: Get rid of grammar dependency
				if (lhsStackTop().equals("USER_FUNCTION_DEF") && j == 1 || 
				        lhsStackTop().equals("SIGMA_TERM") && j == 0 ||
				        lhsStackTop().equals("SIGMA_TERM") && j == 2 || // Body of the Sigma term. Pass as is. Do not evaluate.
                        lhsStackTop().equals("PI_TERM") && j == 0 ||
                        lhsStackTop().equals("PI_TERM") && j == 2 ||
                        lhsStackTop().equals("DEF_INTEG_TERM") && j == 2 ||
                        lhsStackTop().equals("DEF_INTEG_TERM") && j == 3  // Body of the Pi term. Pass as is. Do not evaluate.
                        ) {
					/* Special case for function body definition */
					/* TODO: Do not hard code this */
					args[j] = n.ch[chIdx];
				} else if (lhsStackTop().equals("VARIABLE_EXPONENTIATION") && j == 1) {
                    // TODO: This is too hacky and ad hoc.
                    String firstTermName = EvaluatorHelper.getFirstTermName(n.ch[chIdx]);

                    if (firstTermName != null && firstTermName.equals("T")) {
                        args[j] = firstTermName;
                    } else {
                        args[j] = eval(n.ch[chIdx]);
                    }
                } else {
					if (n.ch[argIndices[j]].isTerminal()) {
						args[j] = n.ch[chIdx].termName;
					} 
					else {
						args[j] = eval(n.ch[chIdx]);
					}
				}
			}

			try {
				evalRes = m.invoke(this, args);
			}  catch (InvocationTargetException iteExc) {
				throw new ParseTreeEvaluatorException("Evaluation failed due to InvocationTargetException");
			} 
			catch (IllegalAccessException iaeExc) {
				throw new ParseTreeEvaluatorException("Evaluation failed due to IllegalAccessExcpetion");
			}

		} 
		catch (NoSuchMethodException nsme) {
			throw new ParseTreeEvaluatorException(
					"Cannot find evaluation function named \"" + funcName
							+ "\" and with " + argTypes.length + " arguments");
		}

		lhsStack.pop();
		rhsStack.pop();
		return evalRes;
	}

	public double getDouble(Object x) {
		if (x.getClass().equals(Double.class)) {
			return ((Double) x);
		} else if (x.getClass().equals(String.class)) {
			return Double.parseDouble((String) x);
		} else if (x.getClass().equals(ValueUnion.class)) {
            return ((ValueUnion) x).getDouble();
        } else {
			throw new RuntimeException("Unexpected type: " + x.getClass());
		}
	}

	public Object pass(Object x) {
		if (x.getClass().equals(ValueUnion.class)) {
			return ((ValueUnion) x).get();
		} else if (x.getClass().equals(String.class)) {
			// return Double.parseDouble((String) x);
			return x;
		} else if (x.getClass().equals(Double.class)) {
			return (Double) x;
		} else if (x.getClass().equals(Matrix.class)) {
			return (Matrix) x;
		} else if (x.getClass().equals(FunctionArgumentList.class)) {
			return (FunctionArgumentList) x;
		} else if (x.getClass().equals(FunctionTerm.class)) {
            return (FunctionTerm) x;
        } else if (x.getClass().equals(Amount.class)) {
            Amount amountX = (Amount) x;
            return Double.valueOf((double) amountX.getEstimatedValue());

		} else {
			throw new RuntimeException("Unexpected input type: " + x.getClass());
		}

	}

	public double negative(Object x) {
		return -1.0 * getDouble(x);
	}

	public Object add(Object x, Object y) {
		boolean xIsNum = x.getClass().equals(Double.class)
				|| x.getClass().equals(String.class);
		boolean yIsNum = y.getClass().equals(Double.class)
				|| y.getClass().equals(String.class);

		if (xIsNum && yIsNum) {
			return getDouble(x) + getDouble(y);
		} else if (xIsNum && y.getClass().equals(Matrix.class)) {
			double dx = getDouble(x);
			Matrix my = (Matrix) y;

			Matrix mOut = matrixScalarAdd(my, dx);
			return mOut;
		} else if (x.getClass().equals(Matrix.class) && yIsNum) {
			Matrix mx = (Matrix) x;
			double dy = getDouble(y);

			Matrix mOut = matrixScalarAdd(mx, dy);
			return mOut;
		} else if (x.getClass().equals(Matrix.class)
				&& y.getClass().equals(Matrix.class)) {
			Matrix mx = (Matrix) x;
			Matrix my = (Matrix) y;

			return mx.plus(my);
		} else if (x.getClass().equals(Amount.class)
                && y.getClass().equals(Double.class)) {
            Amount ax = (Amount) x;
            double dy = getDouble(y);

            return Double.valueOf(dy + (double) ax.getExactValue());
        } else if (x.getClass().equals(Double.class)
                && y.getClass().equals(Amount.class)) {
            double dx = getDouble(x);
            Amount ay = (Amount) y;

            return Double.valueOf(dx + (double) ay.getExactValue());
        } else {
			throw new RuntimeException(
					"Unsupport types scenario for method \"add\"");
		}

	}

	private Matrix matrixScalarAdd(Matrix mx, double dy) {
		int nr = mx.getRowDimension();
		int nc = mx.getColumnDimension();

		Matrix my = mx.copy();
		for (int i = 0; i < nr; ++i) {
			for (int j = 0; j < nc; ++j) {
				my.set(i, j, my.get(i, j) + dy);
			}
		}

		return my;
	}

	public Object subtract(Object x, Object y) {
		boolean xIsNum = x.getClass().equals(Double.class)
				|| x.getClass().equals(String.class);
		boolean yIsNum = y.getClass().equals(Double.class)
				|| y.getClass().equals(String.class);

		if (xIsNum && yIsNum) {
			return getDouble(x) - getDouble(y);
		} else if (xIsNum && y.getClass().equals(Matrix.class)) {
			double dx = getDouble(x);
			Matrix my = (Matrix) y;

			Matrix mOut = matrixScalarAdd(my.times(-1.0), dx);
			return mOut;
		} else if (x.getClass().equals(Matrix.class) && yIsNum) {
			Matrix mx = (Matrix) x;
			double dy = getDouble(y);

			Matrix mOut = matrixScalarAdd(mx, -dy);
			return mOut;
		} else if (x.getClass().equals(Matrix.class)
				&& y.getClass().equals(Matrix.class)) {
			Matrix mx = (Matrix) x;
			Matrix my = (Matrix) y;

			return mx.minus(my);
		} else {
			throw new RuntimeException(
					"Unsupport types scenario for method \"subtract\"");
		}
	}

	public Object compare(Object op, Object x, Object y) throws ParseTreeEvaluatorException {
		boolean xIsNum = x.getClass().equals(Double.class)
				|| x.getClass().equals(String.class);
		boolean yIsNum = y.getClass().equals(Double.class)
				|| y.getClass().equals(String.class);

		if (xIsNum && yIsNum) {
            final String opStr = op.toString();

            final boolean compRes;
            final double xVal = getDouble(x);
            final double yVal = getDouble(y);

            switch (opStr) {
                case "lt":
                    compRes = xVal < yVal;
                    break;
                case "gt":
                    compRes = xVal > yVal;
                    break;
                case "lte":
                    compRes = xVal <= yVal;
                    break;
                case "gte":
                    compRes = xVal >= yVal;
                    break;
                default:
                    throw new ParseTreeEvaluatorException("Unsupported comparison operator: \"" + opStr +"\"");
            }

            ValueUnion result = new ValueUnion(compRes);

			return result;
		} else if (xIsNum && y.getClass().equals(Matrix.class)) {
            throw new ParseTreeEvaluatorException("Comparison operation does not apply to matrices");
		} else if (x.getClass().equals(Matrix.class) && yIsNum) {
            throw new ParseTreeEvaluatorException("Comparison operation does not apply to matrices");
		} else if (x.getClass().equals(Matrix.class)
				&& y.getClass().equals(Matrix.class)) {
			throw new ParseTreeEvaluatorException("Comparison operation does not apply to matrices");
		} else {
			throw new RuntimeException(
					"Unsupport types scenario for method \"subtract\"");
		}
	}

	public Object multiply(Object x, Object y) {
		boolean xIsNum = x.getClass().equals(Double.class)
				|| x.getClass().equals(String.class);
		boolean yIsNum = y.getClass().equals(Double.class)
				|| y.getClass().equals(String.class);

		if (xIsNum && yIsNum) {
			return getDouble(x) * getDouble(y);
		} else if (xIsNum && y.getClass().equals(Matrix.class)) {
			double dx = getDouble(x);
			Matrix my = (Matrix) y;

			return my.times(dx);
		} else if (x.getClass().equals(Matrix.class) && yIsNum) {
			Matrix mx = (Matrix) x;
			double dy = getDouble(y);

			return mx.times(dy);
		} else if (x.getClass().equals(Matrix.class)
				&& y.getClass().equals(Matrix.class)) {
			Matrix mx = (Matrix) x;
			Matrix my = (Matrix) y;

			return mx.times(my);
		} else if (x.getClass().equals(Amount.class)
                && y.getClass().equals(Double.class)) {
            Amount ax = (Amount) x;
            double dy = getDouble(y);

            return Double.valueOf(dy * ax.getExactValue());
        } else if (x.getClass().equals(Double.class)
                && y.getClass().equals(Amount.class)) {
            double dx = getDouble(x);
            Amount ay = (Amount) y;

            return Double.valueOf(dx * ay.getEstimatedValue());
        } else {
			throw new IllegalArgumentException (
                    "Unsupport argument types scenario for method \"multiply\": " +
                    x.getClass() + " & " + y.getClass());
		}
	}

	public double divide(Object numer, Object denom)
			throws DivisionByZeroException {
		double d_numer = getDouble(numer); /* TODO: Figure out what is wrong */
		double d_denom = getDouble(denom);

        return d_numer / d_denom;
	}

    public Object exponentiation(Object base, Object exp)
			throws ZeroToZerothPowerException, UnsupportedMatrixExponentException {
        if (base.getClass() == ValueUnion.class &&
                ((ValueUnion) base).getValueType() == ValueUnion.ValueType.Matrix ) {
            Matrix baseMat = ((ValueUnion) base).getMatrix();

            if (exp.getClass() == Double.class && getDouble(exp) == -1.0) {
                // "exponent" is "-1": Matrix inverse
                return baseMat.inverse();
            } else if (exp.getClass() == String.class && ((String) exp).equals("T")) {
                // "exponent" is "-1": Matrix inverse
                return baseMat.transpose();
            } else {
                throw new UnsupportedMatrixExponentException();
            }

        } else {
            double d_base = getDouble(base);
            double d_exp = getDouble(exp);

            if (d_base == 0.0 && d_exp == 0.0) {
                throw new ZeroToZerothPowerException();
            }

            return Math.pow(d_base, d_exp);
        }
	}

	public double sqrt(Object x) throws SquareRootOfNegativeException {
		double d_x = getDouble(x);

		if (d_x < 0.0) {
			throw new SquareRootOfNegativeException(
					"Attempt to get the square root of negative number " + d_x);
		}

		double y = Math.sqrt(d_x);
		return y;
	}

	public double ln(Object x) throws LogarithmOfNonPositiveException {
		double d_x = getDouble(x);

		if (d_x <= 0.0) {
			throw new LogarithmOfNonPositiveException(
					"Attempt to take the logarithm of non-positive number "
							+ d_x);
		}

		double y = Math.log(d_x);
		return y;
	}

    public double log(Object x) throws LogarithmOfNonPositiveException {
        return ln(x);
    }

	public double sin(Object x) {
		double d_x = getDouble(x);
		double y = Math.sin(d_x);
		return y;
	}

	public double cos(Object x) {
		double d_x = getDouble(x);
		double y = Math.cos(d_x);
		return y;
	}

	public double det(Object x) throws InvalidArgumentForMatrixOperation {
		if (!x.getClass().equals(Matrix.class)) {
			throw new InvalidArgumentForMatrixOperation(
					"Invalid argument type for matrix operation");
		}

		return ((Matrix) x).det();
	}

	public double rank(Object x) throws InvalidArgumentForMatrixOperation {
		if (!x.getClass().equals(Matrix.class)) {
			throw new InvalidArgumentForMatrixOperation(
					"Invalid argument type for matrix operation");
		}

		return ((Matrix) x).rank();
	}

	public Object call_function_1arg(Object methodObj, Object arg0)
			throws NoSuchMethodException, InvocationTargetException,
			IllegalAccessException {
		Method method = (Method) methodObj;

		return method.invoke(this, arg0);
	}

	public Method get_math_function_2char_1arg(Object nameObj0, Object nameObj1)
			throws NoSuchMethodException {
		StringBuilder funcNameBuilder = new StringBuilder();
		funcNameBuilder.append((String) nameObj0);
		funcNameBuilder.append((String) nameObj1);
		String funcName = funcNameBuilder.toString();

		Class[] argTypes = new Class[1];
		for (int i = 0; i < argTypes.length; ++i) {
			argTypes[i] = Object.class;
		}

		Method method = this.getClass().getMethod(funcName, argTypes);
		return method;
	}

	public Method get_math_function_4char_1arg(Object nameObj0,
			Object nameObj1, Object nameObj2, Object nameObj3)
			throws NoSuchMethodException {
		StringBuilder funcNameBuilder = new StringBuilder();
		funcNameBuilder.append((String) nameObj0);
		funcNameBuilder.append((String) nameObj1);
		funcNameBuilder.append((String) nameObj2);
		funcNameBuilder.append((String) nameObj3);
		String funcName = funcNameBuilder.toString();

		Class[] argTypes = new Class[1];
		for (int i = 0; i < argTypes.length; ++i) {
			argTypes[i] = Object.class;
		}

		Method method = this.getClass().getMethod(funcName, argTypes);
		return method;
	}

	public Method get_math_function_3char_1arg(Object nameObj0,
			Object nameObj1, Object nameObj2) throws NoSuchMethodException {
		StringBuilder funcNameBuilder = new StringBuilder();
		funcNameBuilder.append((String) nameObj0);
		funcNameBuilder.append((String) nameObj1);
		funcNameBuilder.append((String) nameObj2);
		String funcName = funcNameBuilder.toString();

		Class[] argTypes = new Class[1];
		for (int i = 0; i < argTypes.length; ++i) {
			argTypes[i] = Object.class;
		}

		Method method = this.getClass().getMethod(funcName, argTypes);
		return method;
	}

	public String string(Object s) {
		@SuppressWarnings("rawtypes")
		Class sClass = s.getClass();
		if (!sClass.equals(String.class)) {
			throw new RuntimeException(
					"Input argument to string() is not a String type");
		}

		return (String) s;
	}

    public String form_subscripted_var_name(Object v, Object sub) {
        @SuppressWarnings("rawtypes")
        Class vClass = v.getClass();
        Class subClass = sub.getClass();

        if (!vClass.equals(String.class) || !subClass.equals(String.class)) {
            throw new RuntimeException(
                    "Input argument to form_subscripted_var_name() are not of String type");
        }

        return ((String) v) + "_" + ((String) sub);
    }
	
//	public VariableInfo variable_info(Object s) {
//	    
//	    
//	    return new VariableInfo(); /* Factory method is more appropriate here? */
//	}

	public String digit_concat(Object c, Object d) {
		if (!c.getClass().equals(String.class))
			throw new RuntimeException(
					"First input argument to digit_concat() is not a String type");
		if (!d.getClass().equals(String.class))
			throw new RuntimeException(
					"Second input argument to digit_concat() is not a String type");

		return (String) c + (String) d;
	}

	public double decimal_by_parts(Object a, Object b) {
		if (!a.getClass().equals(String.class))
			throw new RuntimeException(
					"First input argument to string() is not a String type");
		if (!b.getClass().equals(String.class))
			throw new RuntimeException(
					"Second input argument to string() is not a String type");

		/* (a).(b) */
		return (Double.parseDouble((String) a) + Double.parseDouble("0."
				+ (String) b));
	}

	/* Assign double value to variable */
	public String variable_assign_value(Object a, Object b) {
		/* Get the variable name */
		if (!a.getClass().equals(String.class)) {
			throw new RuntimeException("Variable symbol name must be a String");
		}
		String varName = (String) a;

		/* Get the value; Enter the variable name and value */
		String s = ""; /* TODO: Non-double type right hand side */

		if (b.getClass().equals(Matrix.class)) {
			varMap.addVar(varName, new ValueUnion((Matrix) b));
			s = matrix2String((Matrix) b);
		} else if (b.getClass().equals(String.class)) {
            varMap.addVar(varName, new ValueUnion(Double.parseDouble((String) b)));
			s = (String) b;
		} else if (b.getClass().equals(Double.class)) {
            varMap.addVar(varName, new ValueUnion((Double) b));
            s = String.format("%f", (Double) b);
		} else if (b.getClass().equals(String.class)) {
		    // TODO: class == String: Better make sure that this doesn't happen.
            s = (String) b;

            try {
                varMap.addVar(varName, new ValueUnion(Double.parseDouble(s)));
            } catch (Throwable throwable) {}
        } else {
			throw new RuntimeException("Unexpected value argument type: "
					+ b.getClass());
		}

		return s;
	}

	/* Assign double value to variable */
	public Matrix variable_assign_matrix(Object a, Object b) {
		/* Get the variable name */
		if (!a.getClass().equals(String.class)) {
			throw new RuntimeException("Variable symbol name must be a String");
		}
		String varName = (String) a;

		/* Get the value */
		Matrix val = null;
		if (b.getClass().equals(Matrix.class)) {
			val = (Matrix) b;
		} else {
			throw new RuntimeException("Unexpected value argument type: "
					+ b.getClass());
		}

		/* Enter the variable name and value */
		varMap.addVar(varName, new ValueUnion(val));

		return val;
	}

	public Object eval_variable(Object v) {
		/* Get the variable name */
		if (!v.getClass().equals(String.class)) {
			throw new RuntimeException("Variable symbol name must be a String");
		}
		String varName = (String) v;

		if (hasDirectAncestor("USER_FUNCTION_ARGS") && /* Special case for customer function definition */
		    hasDirectAncestor("USER_FUNCTION_DEF")) {
			return varName;
		}

        if (varMap.containsVarName(varName)) {
			return varMap.getVarValue(varName);
		} else {
			return new ValueUnion(0.0); /* TODO: Throw an error? */
		}
	}

	/* Matrix operations */

	/* Generate a 1x1 matrix */
	public Matrix matrix_1x1(Object v) {
		double d_x = getDouble(v);

		double[][] arr = new double[1][1];
		arr[0][0] = d_x;

		return new Matrix(arr);
	}

	/*
	 * Matrix row concatenation. Expect: 1st input argument: rvec, a row vector
	 * (in Matrix type); 2nd input argument: num, a number (Double)
	 */
	public Matrix matrix_row_concat(Object rvec, Object num) {
		Matrix rVector = (Matrix) rvec;

		int nr = rVector.getRowDimension();
		int nc = rVector.getColumnDimension();

		if (nr != 1) {
			throw new RuntimeException(
					"Input matrix does not have a row dimension of 1 (" + nr
							+ ") instead");
		}

		double[][] arr = new double[nr][nc + 1];
		Matrix out = new Matrix(arr);
		out.setMatrix(0, nr - 1, 0, nc - 1, rVector);

		double d_x = getDouble(num);
		out.set(0, nc, d_x);

		return out;
	}

	/*
	 * Matrix column concatenation. Expect 1st input argument: mat0: a matrix
	 * 2nd input argument: rvec: a row vector (in Matrix type)
	 */
	public Matrix matrix_col_concat(Object mat0, Object mat1) {
		Matrix matrix0 = (Matrix) mat0;
		Matrix matrix1 = (Matrix) mat1;

		int nr0 = matrix0.getRowDimension();
		int nc0 = matrix0.getColumnDimension();
		int nr1 = matrix1.getRowDimension();
		int nc1 = matrix1.getColumnDimension();

		if (nc0 != nc1) {
			throw new RuntimeException(
					"Input matrices do not have equal number of rows (" + nc0
							+ " != " + nc1 + ")");
		}

		double[][] arr = new double[nr0 + nr1][nc0];
		Matrix out = new Matrix(arr);
		// out.setMatrix(0, 0, nr0 - 1, nc0 - 1, matrix0);
		out.setMatrix(0, nr0 - 1, 0, nc0 - 1, matrix0);
		// out.setMatrix(nr0, nc0, nr0 + nr1 - 1, nc0 + nc1 - 1, matrix1);
		out.setMatrix(nr0, nr0 + nr1 - 1, 0, nc0 - 1, matrix1);

		return out;
	}

	/* Methods for user-defined functions */
	public FunctionArgumentList function_arg_list(Object obj) {
		if (obj.getClass().equals(String.class)) {
			String argStr = (String) obj;

			String tokenType = prodSet.terminalSet.getTypeOfToken(argStr);
			if (tokenType != null && tokenType.equals("VARIABLE_SYMBOL")) {
				return new FunctionArgumentList(argStr);
			} else {
				return new FunctionArgumentList(getDouble(argStr)); /* TODO: Matrix types? */
			}
		}

		return new FunctionArgumentList(obj);
	}

	public FunctionArgumentList append_function_arg_list(Object argListObj,
			Object obj) {
		FunctionArgumentList argList = (FunctionArgumentList) argListObj;

        Object appended = null;
        if (obj.getClass().equals(String.class)) { //TODO: The producer of "obj" should probably never return a string
            try {
                appended = Double.parseDouble((String) obj);
            } catch (Exception e) {
                appended = obj;
            }
        } else {
            appended = obj;
        }

		argList.append(appended);
		return argList;
	}

	public FunctionTerm user_function_term(Object functionNameObj,
			Object functionArgumentListObj) {
		String functionName = (String) functionNameObj;
		FunctionArgumentList functionArgumentList = (FunctionArgumentList) functionArgumentListObj;

		FunctionTerm funcTerm = new FunctionTerm(functionName,
				functionArgumentList);
		return funcTerm;
	}

	public FunctionTerm define_user_function(Object funcTermObj, Object nodeObj) {
		FunctionTerm funcTerm = (FunctionTerm) funcTermObj;

		Node node = (Node) nodeObj;
        
		funcTerm.defineBody(node);

		/* Register the function */
		varMap.addVar(funcTerm.getFunctionName(), new ValueUnion(funcTerm));

		return funcTerm;
	}

	public Object evaluate_user_function(Object funcTermObj)
			throws ParseTreeEvaluatorException {
		FunctionTerm funcTermInput = (FunctionTerm) funcTermObj;
		String functionName = funcTermInput.getFunctionName();
		ValueUnion funcTermStoredVal = varMap.getVarValue(functionName);
		if (funcTermStoredVal == null) {
			throw new UndefinedFunctionException("Undefined function: \"" + functionName + "\"");
		}

		FunctionTerm funcTermStored = funcTermStoredVal.getUserFunction(); /* TODO: check if function exists */
		/* TODO: Function overloading by arguments */
		
		/* Determine the temporary argument names based on function call stack status */
		int funcStackPos = funcNameStack.size(); 
		List<String> funcArgNames = Arrays.asList(EvaluatorHelper.genFuncArgNames(funcStackPos, funcTermInput.argList));
		
		/* Push function name to call stack */
		funcNameStack.add(funcTermInput.functionName);
		
		Object retVal = funcTermStored.evaluate(this, funcArgNames, funcTermInput.getArgumentList());
		
		/* Pop function name from call stack */
		funcNameStack.pop();
		
		return retVal;
	}

	public Object def_sigma_term(Object assignStatementObj, Object upperLimitObj, Object bodyObj) 
	    throws ParseTreeEvaluatorException {
	    Node assignStatement = (Node) assignStatementObj;
	    Node argNameNode = assignStatement.ch[1].ch[0]; /* TODO: Get rid of grammar dependency */
	    FunctionArgumentList argList = new FunctionArgumentList(argNameNode.termName);
	    
	    Node lowerLimitNode = assignStatement.ch[2];	/* TODO: Get rid of grammar dependency */	    
	    double lowerLimit = getDouble(this.eval(lowerLimitNode));
	    
	    double upperLimit = getDouble(upperLimitObj);
	    
	    ArgumentRange argRange = new UniformArgumentRange(lowerLimit, 1.0, upperLimit);
	    List<ArgumentRange> argRanges = new ArrayList<>();
	    argRanges.add(argRange);
	    
	    SigmaTerm sigmaTerm = new SigmaTerm("_sigma_term_", argList, argRanges); /* TODO: name that makes more sense */
	    	    
	    /* Define the body */
	    sigmaTerm.defineBody((Node) bodyObj);
	    
	    return sigmaTerm;
	}

    public Object def_pi_term(Object assignStatementObj, Object upperLimitObj, Object bodyObj)
            throws ParseTreeEvaluatorException {
        Node assignStatement = (Node) assignStatementObj;
        Node argNameNode = assignStatement.ch[1].ch[0]; /* TODO: Get rid of grammar dependency */
        FunctionArgumentList argList = new FunctionArgumentList(argNameNode.termName);

        Node lowerLimitNode = assignStatement.ch[2];	/* TODO: Get rid of grammar dependency */
        double lowerLimit = getDouble(this.eval(lowerLimitNode));

        double upperLimit = getDouble(upperLimitObj);

        ArgumentRange argRange = new UniformArgumentRange(lowerLimit, 1.0, upperLimit);
        List<ArgumentRange> argRanges = new ArrayList<>();
        argRanges.add(argRange);

        PiTerm piTerm = new PiTerm("_pi_term_", argList, argRanges); /* TODO: name that makes more sense */

        /* Define the body */
        piTerm.defineBody((Node) bodyObj);

        return piTerm;
    }

    public Object def_def_integ_term(Object lowerLimitObj, Object upperLimitObj, Object bodyObj, Object varObject) {
        final int DEF_INTEG_NUM_INTERVALS = 100;        // TODO: Remove magic number

        double lowerLimit = getDouble(lowerLimitObj);
        double upperLimit = getDouble(upperLimitObj);

        Node varNode = (Node) varObject;
        Node argNameNode = varNode.ch[0]; /* TODO: Get rid of grammar dependency */
        FunctionArgumentList argList = new FunctionArgumentList(argNameNode.termName);

        ArgumentRange argRange = new UniformIntegralArgumentRange(lowerLimit, upperLimit, DEF_INTEG_NUM_INTERVALS);

        List<ArgumentRange> argRanges = new ArrayList<>();  //TODO: Multiple integrals
        argRanges.add(argRange);

        DefiniteIntegralTerm defIntegTerm = new DefiniteIntegralTerm("_def_integ_term_", argList, argRanges);

        /* Define the body */
        defIntegTerm.defineBody((Node) bodyObj);

        return defIntegTerm;
    }
	
	public Object evaluate_sigma_term(Object sigmaTermObj)
            throws ParseTreeEvaluatorException {
        SigmaTerm sigmaTerm = (SigmaTerm) sigmaTermObj;
        
        /* TODO: Function overloading by arguments */
        
//        ParseTreeEvaluator evaluator = new ParseTreeEvaluator(prodSet);
    
        int funcStackPos = funcNameStack.size(); 
        List<String> funcArgNames = Arrays.asList(EvaluatorHelper.genFuncArgNames(funcStackPos, sigmaTerm.argList));
        
        /* Push function name to call stack */
        funcNameStack.add("SigmaTerm_" + sigmaTerm.hashCode()); /* TODO: Better naming */
        
        Object retVal = sigmaTerm.evaluate(this, funcArgNames);
        
        /* Pop from call stack */
        funcNameStack.pop();
                
        return retVal;
    }

    public Object evaluate_pi_term(Object piTermObj)
            throws ParseTreeEvaluatorException {
        PiTerm piTerm = (PiTerm) piTermObj;

        /* TODO: Function overloading by arguments */

//        ParseTreeEvaluator evaluator = new ParseTreeEvaluator(prodSet);

        int funcStackPos = funcNameStack.size();
        List<String> funcArgNames = Arrays.asList(EvaluatorHelper.genFuncArgNames(funcStackPos, piTerm.argList));

        /* Push function name to call stack */
        funcNameStack.add("PiTerm_" + piTerm.hashCode()); /* TODO: Better naming */

        Object retVal = piTerm.evaluate(this, funcArgNames);

        /* Pop from call stack */
        funcNameStack.pop();

        return retVal;
    }

    public Object evaluate_def_integ_term(Object defIntegTermObj)
            throws ParseTreeEvaluatorException {
        DefiniteIntegralTerm defIntegTerm = (DefiniteIntegralTerm) defIntegTermObj;

        int funcStackPos = funcNameStack.size();
        List<String> funcArgNames = Arrays.asList(EvaluatorHelper.genFuncArgNames(funcStackPos, defIntegTerm.argList));

        /* Push function name to call stack */
        funcNameStack.add("DefIntegTerm_" + defIntegTerm.hashCode()); /* TODO: Better naming */

        Object retVal = defIntegTerm.evaluate(this, funcArgNames);

        /* Pop from call stack */
        funcNameStack.pop();

        return retVal;
    }

	/* Clear variable map: Including user-defined variables and functions */
	public void clearUserData() {
	    varMap.clear();
	}

    public PlatoVarMap getVarMap() {
        return varMap;
    }

    public ValueUnion getFromVarMap(final String varName) {
        return varMap.getVarValue(varName);
    }

    public int getFuncStackHeight() {
        return funcNameStack.size();
    }

	/* ~Methods */
}
