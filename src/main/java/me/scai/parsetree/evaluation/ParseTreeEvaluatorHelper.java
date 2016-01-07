package me.scai.parsetree.evaluation;

import me.scai.parsetree.evaluation.exceptions.ParseTreeEvaluatorException;

import java.lang.reflect.InvocationTargetException;

public class ParseTreeEvaluatorHelper {
    /**
     * Try to find the root cause of an invocation target exception
     * @param t   The invocation target exception
     * @return    If the root cause can't be found, null
     *            If found, the root cause itself
     */
    public static ParseTreeEvaluatorException findRootCause(InvocationTargetException t) {
        Throwable tt = t;

        while (tt.getCause() != null) {
            if (tt.getCause() instanceof ParseTreeEvaluatorException) {
                return (ParseTreeEvaluatorException) tt.getCause();
            } else {
                tt = tt.getCause();
            }
        }

        return null;
    }
}
