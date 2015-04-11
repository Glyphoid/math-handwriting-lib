package me.scai.parsetree.evaluation;

import java.util.List;

abstract class SigmaPiTerm extends FunctionSigmaPiTerm {
	/* Member variables */
	List<ArgumentRange> argumentRanges;

	/* ~Member variables */

	/* Constructor */
	public SigmaPiTerm(String tFunctionName, FunctionArgumentList tArgList,
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
}
