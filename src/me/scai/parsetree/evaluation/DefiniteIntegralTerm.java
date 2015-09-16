package me.scai.parsetree.evaluation;

import java.util.ArrayList;
import java.util.List;

public class DefiniteIntegralTerm extends SigmaPiIntegralTerm {
    public DefiniteIntegralTerm(String tFunctionName,
                                FunctionArgumentList tArgList,
                                List<ArgumentRange> tArgRanges) {
        super(tFunctionName, tArgList, tArgRanges);
    }

    @Override
    public Object evaluate(ParseTreeEvaluator evaluator,
                           List<String> tempArgNames)
            throws ParseTreeEvaluatorException {
        if (tempArgNames.size() != argList.numArgs()) {
            throw new RuntimeException("Incorrect number of temporary argument names");
        }

        if (!isDefined()) {
            throw new RuntimeException(
                    "The body of this definite integral is not defined"); /* TODO: More specific exception type */
        }

        int numArgs = argList.numArgs();
        List<String> argSymbols = argList.getSymbolNames(); /* TODO: Use escape arg names such as _arg_1 */

		/* Obtain the value lists for all arguments */
        ArrayList<List<Double>> allArgVals = new ArrayList<List<Double>>();
        allArgVals.ensureCapacity(numArgs);

        double integUnitSize = 1.0;     /* Integral unit size: General enough to support multiple integrals */
        ArrayList<Double> intervals = new ArrayList<>();
        intervals.ensureCapacity(numArgs);
        for (int i = 0; i < numArgs; ++i) {
            allArgVals.add(argumentRanges.get(i).getValues());

            intervals.add(((UniformIntegralArgumentRange) argumentRanges.get(0)).getInterval());
            integUnitSize *= intervals.get(intervals.size() - 1);
        }

		/* "Functionize" the body, i.e., replace the argument symbols with
		 * special ones like "__stack0_funcArg1__"
		 */
        EvaluatorHelper.functionizeBody(this.evalBody, argSymbols);

        double sum = 0.0;
        int numVals = allArgVals.get(0).size();

        if (numArgs > 1) {
            throw new IllegalStateException("Multiple definite integral is not implemented yet");
        }

        // Trapezoid method for 1D cases
        // TODO: Multi-dimensional integration
        // Handle the first point
        for (int j = 0; j < numArgs; ++j) {
            String argSymbol = tempArgNames.get(j);
            double argVal = allArgVals.get(j).get(0);

            evaluator.variable_assign_value(argSymbol, argVal);
        }
        Object out = evaluator.eval(this.evalBody);
        double leftY = (double) out;

        // Handle all remaining points
        for (int i = 0; i < numVals; ++i) {
            for (int j = 0; j < numArgs; ++j) {
                String argSymbol = tempArgNames.get(j);
                double argVal = allArgVals.get(j).get(i) + intervals.get(j);

                evaluator.variable_assign_value(argSymbol, argVal);
            }

            out = evaluator.eval(this.evalBody);
            double rightY = (double) out;

            sum += ((leftY + rightY) * integUnitSize / 2.0);

            leftY = rightY;
        }

        return sum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DefiniteIntegralTerm: ");
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
