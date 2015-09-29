package me.scai.parsetree;


import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

public class Test_MathHelper {

    @Test
    public void testGetMaxNIndices() {
        double[] xs0 = {100.0, 200.0, 300.0, -100.0, 1.0, 400.0};
        int[] maxIndices0 = MathHelper.getMaxNIndices(xs0, 3);
        assertArrayEquals(maxIndices0, new int[] {5, 2, 1});

        /* Edge case: n > array length */
        double[] xs1 = {3.0, 7.0, 4.0};
        int[] maxIndices1 = MathHelper.getMaxNIndices(xs1, 4);
        assertArrayEquals(maxIndices1, new int[] {1, 2, 0});

        /* Edge case: empty array */
        double[] xs2 = {};
        int[] maxIndices2 = MathHelper.getMaxNIndices(xs2, 2);
        assertNull(maxIndices2);
    }


}
