package me.scai.parsetree;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.IllegalAccessException;

public class ParseTreeEvaluator {
	/* Member variables */
	/* Constants */
	// final static String passFuncName = "PASS";

	Map<String, String> sumString2FuncNameMap = new HashMap<>();
	Map<String, int[]> sumString2NodeIdxMap = new HashMap<>();

	Map<String, Double> varMap = new HashMap<>(); 
	
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

	/* Main method: eval: evaluate a parse tree */
	public Object eval(Node n) throws ParseTreeEvaluatorException {
		String sumString = n.prodSumString;
		String funcName = sumString2FuncNameMap.get(sumString);
		int[] argIndices = sumString2NodeIdxMap.get(sumString);
		int nArgs = argIndices.length;
		Object evalRes = null;

		if (funcName == null || argIndices == null)
			throw new RuntimeException(
					"Cannot find evaluation instruction for production: "
							+ sumString);

		@SuppressWarnings("rawtypes")
		Class[] argTypes = new Class[nArgs];
		for (int i = 0; i < nArgs; ++i)
			argTypes[i] = Object.class;

		try {
			Method m = this.getClass().getMethod(funcName.toLowerCase(),
					argTypes);

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
				// System.out.println("Invoking " + m.toString()); //DEBUG
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

	public double pass(Object x) {
		if (x.getClass().equals(String.class)) {
			return Double.parseDouble((String) x);
		} else if (x.getClass().equals(Double.class)) {
			return (Double) x;
		} else {
			throw new RuntimeException("Unexpected input type: " + x.getClass());
		}

	}

	public double negative(Object x) {
		return -1.0 * getDouble(x);
	}

	public double add(Object x, Object y) {
		return getDouble(x) + getDouble(y);
	}

	public double subtract(Object x, Object y) {
		return getDouble(x) - getDouble(y);
	}

	public double multiply(Object x, Object y) {
		return getDouble(x) * getDouble(y);
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

	public double variable_assign_value(Object a, Object b) {
		/* Get the variable name */
		if (!a.getClass().equals(String.class)) {
			throw new RuntimeException("Variable symbol name must be a String");
		}
		String varName = (String) a;
		
		/* Get the value */
		double val = 0.0; /* TODO: Non-double type right hand side */
		if (b.getClass().equals(String.class)) {
			val = Double.parseDouble((String) b);
		} else if (b.getClass().equals(Double.class)) {
			val = (Double) b;
		} else {
			throw new RuntimeException("Unexpected value argument type: " + b.getClass());
		}
		
		/* Enter the variable name and value */
		varMap.put(varName, val);
		
		return val;
	}

	public double eval_variable(Object v) {
		/* Get the variable name */
		if (!v.getClass().equals(String.class)) {
			throw new RuntimeException("Variable symbol name must be a String");
		}
		String varName = (String) v;
		
		if (varMap.containsKey(varName)) {
			return varMap.get(varName);
		}
		else {
			return 0.0; /* TODO: Throw an error? */
		}
	}

	/* ~Methods */
}
