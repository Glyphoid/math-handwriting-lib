package me.scai.parsetree;

import org.junit.Test;

import java.util.IllegalFormatCodePointException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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

    @Test
    public void testDiff() {
        final float tol = 1e-9f;

        float[] x1 = {3f, 4f, 5f, 10f};
        float[] x1TrueDiff = {1f, 1f, 5f};

        float[] x2 = {3f, -4f};
        float[] x2TrueDiff = {-7f};

        // Edge cases
        float[] x3 = {0f};
        float[] x3TrueDiff = {};

        float[] x4 = {};

        assertArrayEquals(x1TrueDiff, MathHelper.diff(x1), tol);
        assertArrayEquals(x2TrueDiff, MathHelper.diff(x2), tol);
        assertArrayEquals(x3TrueDiff, MathHelper.diff(x3), tol);

        IllegalArgumentException caughtException = null;
        try {
            MathHelper.diff(x4);
        } catch (IllegalArgumentException exc) {
            caughtException = exc;
        }
        assertNotNull(caughtException);

    }

    @Test
    public void testCumsum() {
        final float tol = 1e-9f;

        float[] x1 = {3f, 4f, 5f, 10f};
        float[] x1TrueDiffNoInitialZero = {3f, 7f, 12f, 22f};
        float[] x1TrueDiffWithInitialZero = {0f, 3f, 7f, 12f, 22f};

        float[] x2 = {-3f};
        float[] x2TrueDiffNoInitialZero = {-3f};
        float[] x2TrueDiffWithInitialZero = {0f, -3f};

        float[] x3 = {};

        assertArrayEquals(x1TrueDiffNoInitialZero, MathHelper.cumsum(x1, false), tol);
        assertArrayEquals(x1TrueDiffWithInitialZero, MathHelper.cumsum(x1, true), tol);

        assertArrayEquals(x2TrueDiffNoInitialZero, MathHelper.cumsum(x2, false), tol);
        assertArrayEquals(x2TrueDiffWithInitialZero, MathHelper.cumsum(x2, true), tol);

        IllegalArgumentException caughtException = null;
        try {
            MathHelper.cumsum(x3, false);
        } catch (IllegalArgumentException exc) {
            caughtException = exc;
        }
        assertNotNull(caughtException);
    }

    @Test
    public void testSum() {
        final float tol = 1e-9f;

        float[] x1 = {-3f, 4f, 5f, 10f};
        float x1Sum = 16f;

        float[] x2 = {-3f};
        float x2Sum = -3f;

        // Edge cases
        float[] x3 = {};
        float x3Sum = 0f;

        assertEquals(x1Sum, MathHelper.sum(x1), tol);
        assertEquals(x2Sum, MathHelper.sum(x2), tol);
        assertEquals(x3Sum, MathHelper.sum(x3), tol);
    }

    @Test
    public void testCountOccurrence() {
        int[] X1 = {-7, 1, 2, 3, 4, 1, 0, 1};
        int[] X2 = {};

        assertEquals(3, MathHelper.countOccurrences(X1, 1));
        assertEquals(1, MathHelper.countOccurrences(X1, 4));
        assertEquals(0, MathHelper.countOccurrences(X1, 5));

        assertEquals(0, MathHelper.countOccurrences(X2, 2));

    }

    @Test
    public void testGetFullDiscreteSpace() {
        // Invalid input: negative number of values
        boolean exceptionCaught = false;
        try {
            int[][] ds_minus1_1 = MathHelper.getFullDiscreteSpace(-1, 1);
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);

        // Invalid input: negative number of dimensions
        exceptionCaught = false;
        try {
            int[][] ds_1_minus1 = MathHelper.getFullDiscreteSpace(1, -1);
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);

        // Edge case: 1 value, 0 dimension
        int[][] ds_1_0 = MathHelper.getFullDiscreteSpace(1, 0);

        assertEquals(1, ds_1_0.length);
        assertEquals(0, ds_1_0[0].length);

        // Edge case: 0 value, 1 dimension
        int[][] ds_0_1 = MathHelper.getFullDiscreteSpace(0, 1);

        assertEquals(0, ds_0_1.length);

        // Edge case: 1 value, 1 dimension
        int[][] ds_1_1 = MathHelper.getFullDiscreteSpace(1, 1);

        assertEquals(1, ds_1_1.length);
        for (int[] ds : ds_1_1) {
            assertEquals(1, ds.length);
        }

        // 2 values, 2 dimensions
        int[][] ds_2_2 = MathHelper.getFullDiscreteSpace(2, 2);

        assertEquals(4, ds_2_2.length);
        for (int[] ds : ds_2_2) {
            assertEquals(2, ds.length);
        }

        // 3 values, 2 dimensions
        int[][] ds_3_2 = MathHelper.getFullDiscreteSpace(3, 2);

        assertEquals(9, ds_3_2.length);
        for (int[] ds : ds_2_2) {
            assertEquals(2, ds.length);
        }

        // 2 values, 3 dimensions
        int[][] ds_2_3 = MathHelper.getFullDiscreteSpace(2, 3);

        assertEquals(8, ds_2_3.length);
        for (int[] ds : ds_2_3) {
            assertEquals(3, ds.length);
        }
    }





}
