package me.scai.parsetree.evaluation;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;

public class FunctionArgumentList {
	/* enums */
	public enum ArgumentType {
		Symbol, Value
	}

	/* ~enums */

	/* Member variables */
	List<Object> args = new ArrayList<Object>();
	List<ArgumentType> argTypes = new ArrayList<ArgumentType>();

	/* ~Member variables */

	/* Constructors */
	public FunctionArgumentList(Object arg) {
		append(arg);
	}

	public void append(Object arg) {
		if (arg.getClass().equals(String.class)) {
			args.add(arg);
			argTypes.add(ArgumentType.Symbol);
		} else if (arg.getClass().equals(Double.class)
				|| arg.getClass().equals(Matrix.class)) { /*
														 * TODO: equals function
														 * class
														 */
			if (arg.getClass().equals(Double.class)) {
				args.add((Double) arg);
			} else {
				args.add((Matrix) arg);
			}
			argTypes.add(ArgumentType.Value);
		} else {
			throw new RuntimeException("Unsupport argument type: "
					+ arg.getClass());
		}
	}

	public int numArgs() {
		return args.size();
	}

	public Object get(int i) {
		return args.get(i);
	}

	public ArgumentType getType(int i) {
		return argTypes.get(i);
	}

	public boolean allSymbols() {
		for (ArgumentType argType : argTypes) {
			if (argType != ArgumentType.Symbol) {
				return false;
			}
		}

		return true;
	}

	public boolean allValues() {
		for (ArgumentType argType : argTypes) {
			if (argType != ArgumentType.Value) {
				return false;
			}
		}

		return true;
	}

	public List<String> getSymbolNames() {
		if (!allSymbols()) {
			throw new RuntimeException(
					"Not all items in the argument list are symbols"); /*
																		 * Throw
																		 * an
																		 * error
																		 * ?
																		 */
		} else {
			List<String> argNames = new ArrayList<String>();

			for (Object arg : args) {
				argNames.add((String) arg);
			}

			return argNames;
		}
	}
}
