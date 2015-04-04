package me.scai.parsetree;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.IllegalAccessException;

import java.io.StringWriter;
import java.io.PrintWriter;

import Jama.Matrix;

class ValueUnion {
	public enum ValueType {
		Double, 
		Matrix
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
}

public class ParseTreeEvaluator {
	/* Member variables */
	/* Constants */
	// final static String passFuncName = "PASS";

	Map<String, String> sumString2FuncNameMap = new HashMap<>();
	Map<String, int[]> sumString2NodeIdxMap = new HashMap<>();

	Map<String, ValueUnion> varMap = new HashMap<>(); 
	
	/* ~Member variables */

	/* Methods */

	/* Constructor */
	public ParseTreeEvaluator(GraphicalProductionSet gpSet) {
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
					throw new RuntimeException("In evaluation instruction: \"" + gp.evalInstr[0] + "\", a node index exceeds valid range");

				nodeIndices[m - 1] = nodeIdx;
			}

			sumString2NodeIdxMap.put(sumString, nodeIndices);
		}
	}

	public String eval2String(Node n) throws ParseTreeEvaluatorException {
		Object evalRes = eval(n);
		
		String evalResStr = "";
		if (evalRes.getClass().equals(Matrix.class)) {
			evalResStr = matrix2String((Matrix) evalRes);
		}
		else {
			evalResStr = evalRes.toString();
		}
		
		return evalResStr;
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
	
	/* Main method: eval: evaluate a parse tree */
	public Object eval(Node n) throws ParseTreeEvaluatorException {
		String sumString = n.prodSumString;
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

				if (n.ch[argIndices[j]].isTerminal()) {					
					args[j] = n.ch[chIdx].termName;
				} else {
					args[j] = eval(n.ch[chIdx]);
				}
			}

			try {
				evalRes = m.invoke(this, args);
			} 
			catch (InvocationTargetException iteExc) {
				throw new ParseTreeEvaluatorException("Evaluation failed due to InvocationTargetException");
			}
			catch (IllegalAccessException iaeExc) {
				throw new ParseTreeEvaluatorException("Evaluation failed due to IllegalAccessExcpetion");
			}
			
			
		} catch (NoSuchMethodException nsme) {
			throw new ParseTreeEvaluatorException(
					"Cannot find evaluation function named \"" + funcName
							+ "\" and with " + argTypes.length + " arguments");
		}

		return evalRes;
	}
	
	public double getDouble(Object x) {
		if (x.getClass().equals(Double.class)) {
			return ((Double) x);
		} else if (x.getClass().equals(String.class)) {
			return Double.parseDouble((String) x);
		} else {
			throw new RuntimeException("Unexpected type: " + x.getClass());
		}
	}

	public Object pass(Object x) {
		if (x.getClass().equals(ValueUnion.class)) {
			return ((ValueUnion) x).get();
		}
		if (x.getClass().equals(String.class)) {
//			return Double.parseDouble((String) x);
			return x;
		} 
		else if (x.getClass().equals(Double.class)) {
			return (Double) x;
		}
		else if (x.getClass().equals(Matrix.class)) {
			return (Matrix) x;
		}
		else {
			throw new RuntimeException("Unexpected input type: " + x.getClass());
		}

	}

	public double negative(Object x) {
		return -1.0 * getDouble(x);
	}

	public Object add(Object x, Object y) {
		boolean xIsNum = x.getClass().equals(Double.class) || x.getClass().equals(String.class);
		boolean yIsNum = y.getClass().equals(Double.class) || y.getClass().equals(String.class);
		
		if (xIsNum && yIsNum) {
			return getDouble(x) + getDouble(y);
		}
		else if (xIsNum && y.getClass().equals(Matrix.class)) {
			double dx = getDouble(x);
			Matrix my = (Matrix) y;
			
			Matrix mOut = matrixScalarAdd(my, dx);
			return mOut;
		}
		else if (x.getClass().equals(Matrix.class) && yIsNum) {
			Matrix mx = (Matrix) x;
			double dy = getDouble(y);
			
			Matrix mOut = matrixScalarAdd(mx, dy);
			return mOut;
		}
		else if (x.getClass().equals(Matrix.class) && 
				 y.getClass().equals(Matrix.class)) {
			Matrix mx = (Matrix) x;
			Matrix my = (Matrix) y;
			
			return mx.plus(my);
		}
		else {
			throw new RuntimeException("Unsupport types scenario for method \"add\"");
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
		boolean xIsNum = x.getClass().equals(Double.class) || x.getClass().equals(String.class);
		boolean yIsNum = y.getClass().equals(Double.class) || y.getClass().equals(String.class);
		
		if (xIsNum && yIsNum) {
			return getDouble(x) - getDouble(y);
		}
		else if (xIsNum && y.getClass().equals(Matrix.class)) {
			double dx = getDouble(x);
			Matrix my = (Matrix) y;
			
			Matrix mOut = matrixScalarAdd(my.times(-1.0), dx);
			return mOut;
		}
		else if (x.getClass().equals(Matrix.class) && yIsNum) {
			Matrix mx = (Matrix) x;
			double dy = getDouble(y);
			
			Matrix mOut = matrixScalarAdd(mx, -dy);
			return mOut;
		}
		else if (x.getClass().equals(Matrix.class) && 
				 y.getClass().equals(Matrix.class)) {
			Matrix mx = (Matrix) x;
			Matrix my = (Matrix) y;
			
			return mx.minus(my);
		}
		else {
			throw new RuntimeException("Unsupport types scenario for method \"subtract\"");
		}
	}

	public Object multiply(Object x, Object y) {
		boolean xIsNum = x.getClass().equals(Double.class) || x.getClass().equals(String.class);
		boolean yIsNum = y.getClass().equals(Double.class) || y.getClass().equals(String.class);
		
		if (xIsNum && yIsNum) {
			return getDouble(x) * getDouble(y);
		}
		else if (xIsNum && y.getClass().equals(Matrix.class)) {
			double dx = getDouble(x);
			Matrix my = (Matrix) y;
			
			return my.times(dx);
		}
		else if (x.getClass().equals(Matrix.class) && yIsNum) {
			Matrix mx = (Matrix) x;
			double dy = getDouble(y);
			
			return mx.times(dy);
		}
		else if (x.getClass().equals(Matrix.class) && 
				 y.getClass().equals(Matrix.class)) {
			Matrix mx = (Matrix) x;
			Matrix my = (Matrix) y;
			
			return mx.times(my);
		}
		else {
			throw new RuntimeException("Unsupport types scenario for method \"multiply\"");
		}
	}

	public double divide(Object numer, Object denom)
			throws DivisionByZeroException {
		double d_numer = getDouble(numer); /* TODO: Figure out what is wrong */
		double d_denom = getDouble(denom);

		if (d_numer == 0.0) {
			throw new DivisionByZeroException();
		}

		return d_denom / d_numer;
	}

	public double exponentiation(Object base, Object exp)
			throws ZeroToZerothPowerException {
		double d_base = getDouble(base);
		double d_exp = getDouble(exp);

		if (d_base == 0.0 && d_exp == 0.0)
			throw new ZeroToZerothPowerException();

		return Math.pow(d_base, d_exp);
	}
	
	public double sqrt(Object x) throws SquareRootOfNegativeException {
		double d_x = getDouble(x);
		
		if (d_x < 0.0) {
			throw new SquareRootOfNegativeException("Attempt to get the square root of negative number " + d_x);
		}
		
		double y = Math.sqrt(d_x);
		return y;
	}
	
	public double ln(Object x) throws LogarithmOfNonPositiveException {
		double d_x = getDouble(x);
		
		if (d_x <= 0.0) {
			throw new LogarithmOfNonPositiveException("Attempt to take the logarithm of non-positive number " + d_x);
		}
		
		double y = Math.log(d_x);
		return y;
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
	
	public Object call_function_1arg(Object methodObj, Object arg0)
		throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {		
		Method method = (Method) methodObj;
		
		return method.invoke(this, arg0);
	}
	
	public Method get_math_function_2char_1arg(Object nameObj0, Object nameObj1) 
		throws NoSuchMethodException {
		StringBuilder funcNameBuilder = new StringBuilder();
		funcNameBuilder.append((String) nameObj0);
		funcNameBuilder.append((String) nameObj1);
		String funcName = funcNameBuilder.toString();
		
		Class [] argTypes = new Class[1];
		for (int i = 0; i < argTypes.length; ++i) {
			argTypes[i] = Object.class;
		}
		
		Method method = this.getClass().getMethod(funcName, argTypes);
		return method;
	}
	
	public Method get_math_function_3char_1arg(Object nameObj0, Object nameObj1, Object nameObj2) 
			throws NoSuchMethodException {
			StringBuilder funcNameBuilder = new StringBuilder();
			funcNameBuilder.append((String) nameObj0);
			funcNameBuilder.append((String) nameObj1);
			funcNameBuilder.append((String) nameObj2);
			String funcName = funcNameBuilder.toString();
			
			Class [] argTypes = new Class[1];
			for (int i = 0; i < argTypes.length; ++i) {
				argTypes[i] = Object.class;
			}
			
			Method method = this.getClass().getMethod(funcName, argTypes);
			return method;
		}

	public String string(Object s) {
		@SuppressWarnings("rawtypes")
		Class sClass = s.getClass();
		if (!sClass.equals(String.class))
			throw new RuntimeException(
					"Input argument to string() is not a String type");

		return (String) s;
	}

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
		
		/* Get the value; Enter the variable name and value  */
		String s = ""; /* TODO: Non-double type right hand side */
		if (b.getClass().equals(Matrix.class)) {
			varMap.put(varName, new ValueUnion((Matrix) b));
			s = matrix2String((Matrix) b);
		}
		else if (b.getClass().equals(String.class)) {
			varMap.put(varName, new ValueUnion(Double.parseDouble((String) b)));
			s = (String) b;
		} 
		else if (b.getClass().equals(Double.class)) {
			varMap.put(varName, new ValueUnion((Double) b));
			s = String.format("%f", (Double) b);
		} 
		else {
			throw new RuntimeException("Unexpected value argument type: " + b.getClass());
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
			throw new RuntimeException("Unexpected value argument type: " + b.getClass());
		}
		
		/* Enter the variable name and value */
		varMap.put(varName, new ValueUnion(val));
		
		return val;
	}

	public ValueUnion eval_variable(Object v) {
		/* Get the variable name */
		if (!v.getClass().equals(String.class)) {
			throw new RuntimeException("Variable symbol name must be a String");
		}
		String varName = (String) v;
		
		if (varMap.containsKey(varName)) {
			return varMap.get(varName);
		}
		else {
			return new ValueUnion(0.0); /* TODO: Throw an error? */
		}
	}

	/* Matrix operations */
	
	/* Generate a 1x1 matrix */
	public Matrix matrix_1x1(Object v) {
		double d_x = getDouble(v);
		
		double [][] arr = new double[1][1];
		arr[0][0] = d_x;
		
		return new Matrix(arr);
	}
	
	/* Matrix row concatenation. Expect: 
	 *   1st input argument: rvec, a row vector (in Matrix type);
	 *   2nd input argument: num, a number (Double) 
	 */
	public Matrix matrix_row_concat(Object rvec, Object num) {
		Matrix rVector = (Matrix) rvec;
		
		int nr = rVector.getRowDimension();
		int nc = rVector.getColumnDimension();
		
		if (nr != 1) {
			throw new RuntimeException("Input matrix does not have a row dimension of 1 (" + 
		                               nr + ") instead");
		}
		
		double [][] arr = new double[nr][nc + 1];
		Matrix out = new Matrix(arr);
		out.setMatrix(0, nr - 1, 0, nc - 1, rVector);
		
		double d_x = getDouble(num);
		out.set(0, nc, d_x);
		
		return out;
	}
	
	/* Matrix column concatenation. Expect
	 *   1st input argument: mat0: a matrix
	 *   2nd input argument: rvec: a row vector (in Matrix type)
	 */
	public Matrix matrix_col_concat(Object mat0, Object mat1) {
		Matrix matrix0 = (Matrix) mat0;
		Matrix matrix1 = (Matrix) mat1;
		
		int nr0 = matrix0.getRowDimension();
		int nc0 = matrix0.getColumnDimension();
		int nr1 = matrix1.getRowDimension();
		int nc1 = matrix1.getColumnDimension();
		
		if (nc0 != nc1) {
			throw new RuntimeException("Input matrices do not have equal number of rows (" + 
		                               nc0 + " != " + nc1 + ")");
		}
		
		double [][] arr = new double[nr0 + nr1][nc0];
		Matrix out = new Matrix(arr);
//		out.setMatrix(0, 0, nr0 - 1, nc0 - 1, matrix0);
		out.setMatrix(0, nr0 - 1, 0, nc0 - 1, matrix0);
//		out.setMatrix(nr0, nc0, nr0 + nr1 - 1, nc0 + nc1 - 1, matrix1);
		out.setMatrix(nr0, nr0 + nr1 - 1, 0, nc0 - 1, matrix1);
		
		return out;
	}
	
	/* ~Methods */
}
