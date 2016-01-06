package me.scai.parsetree.evaluation.matrix;

import Jama.Matrix;

public class MatrixHelper {
    /* Convert a matrix to a string */
    public static String matrix2String(Matrix m) {
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
}
