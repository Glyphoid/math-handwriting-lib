package me.scai.parsetree.evaluation;

import java.util.ArrayList;
import java.util.List;

class SigmaTerm extends SigmaPiTerm {
	public SigmaTerm(String tFunctionName, FunctionArgumentList tArgList,
			List<ArgumentRange> tArgRanges) {
		super(tFunctionName, tArgList, tArgRanges);
	}

	/* Implementation of abstract methods */
	public Object evaluate(ParseTreeEvaluator evaluator, 
	                       List<String> tempArgNames)
			throws ParseTreeEvaluatorException {
	    if (tempArgNames.size() != argList.numArgs()) {
	        throw new RuntimeException("Incorrect number of temporary argument names");
	    }
	    
		if (!isDefined()) {
			throw new RuntimeException(
					"The body of this function is not defined"); /* TODO: More specific exception type */
		}

		int numArgs = argList.numArgs();
		List<String> argSymbols = argList.getSymbolNames(); /* TODO: Use escape arg names such as _arg_1 */
		double sum = 0.0;

		/* Obtain the value lists for all arguments */
		ArrayList<List<Double>> allArgVals = new ArrayList<List<Double>>();
		allArgVals.ensureCapacity(numArgs);

		for (int i = 0; i < numArgs; ++i) {
			allArgVals.add(argumentRanges.get(i).getValues());
		}
		
		/* "Functionize" the body, i.e., replace the argument symbols with 
		 * special ones like "__stack0_funcArg1__"
		 */
		EvaluatorHelper.functionizeBody(this.evalBody, argSymbols);

		int numVals = allArgVals.get(0).size();
		for (int i = 0; i < numVals; ++i) {
			for (int j = 0; j < numArgs; ++j) {
//				String argSymbol = argSymbols.get(j);
			    String argSymbol = tempArgNames.get(j);
				double argVal = allArgVals.get(j).get(i);

				evaluator.variable_assign_value(argSymbol, argVal);
			}

			Object out = evaluator.eval(this.evalBody);
			double outVal = (double) out;
			/* TODO: Handle situations in which this isn't satisfied */
			sum += outVal;
		}

		return sum;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("SigmaTerm: ");
		sb.append("(");

		int nArgs = argNames.size();
		for (int i = 0; i < nArgs; ++i) {
			sb.append(argNames.get(i));
			if (i < nArgs - 1) {
				sb.append(", ");
			}
		}
		sb.append(")");

		return sb.toString();
	}
}