package me.scai.parsetree.evaluation;

import me.scai.parsetree.evaluation.exceptions.ParseTreeEvaluatorException;

import java.util.List;

/* Parent class for sigma summation, pi product and integral terms */
abstract class SigmaPiIntegralTerm extends FunctionSigmaPiIntegralTerm {
	/* Member variables */
	List<ArgumentRange> argumentRanges;

	/* ~Member variables */

	/* Constructor */
	public SigmaPiIntegralTerm(String tFunctionName, FunctionArgumentList tArgList,
                               List<ArgumentRange> tArgRanges) {
		super(tArgList);

		if (tArgList.numArgs() != tArgRanges.size()) {
			throw new RuntimeException("Mismatch between number of arguments ("
					+ tArgList.numArgs() + ") and number of ranges ("
					+ tArgRanges.size() + ")");
		}

		/* Check to make sure that the lengths all match */
		if (tArgRanges.size() > 0) {
			int rangeLen = tArgRanges.get(0).getValues().size();

			for (int i = 1; i < tArgRanges.size(); ++i) {
				int rangeLen1 = tArgRanges.get(i).getValues().size();

				if (rangeLen1 != rangeLen) {
					throw new RuntimeException(
							"Mismatch between the length of value ranges ("
									+ rangeLen + " != " + rangeLen1 + ")");
				}
			}
		}

		this.argumentRanges = tArgRanges;
	}

    public abstract Object evaluate(ParseTreeEvaluator evaluator,
                                    List<String> tempArgNames)
            throws ParseTreeEvaluatorException;
}
