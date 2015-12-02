package me.scai.parsetree.geometry;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Test_GeometryHelper {
    private static final float tol = 1e-9f;

    @Test
    public void testMergeBounds_bEnclosesA() {
        float[] boundsA = new float[] {10f, 10f, 20f, 20f};
        float[] boundsB = new float[] {-20f, -10f, 30f, 40f};

        float[] mergedBounds = GeometryHelper.mergeBounds(boundsA, boundsB);
        assertEquals(4, mergedBounds.length);
        assertArrayEquals(new float[] {-20f, -10f, 30f, 40f}, GeometryHelper.mergeBounds(boundsA, boundsB), tol);
    }

    @Test
    public void testMergeBounds_aEnclosesB() {
        float[] boundsA = new float[] {-20f, -10f, 30f, 40f};
        float[] boundsB = new float[] {5f, 5f, 10f, 10f};

        float[] mergedBounds = GeometryHelper.mergeBounds(boundsA, boundsB);
        assertEquals(4, mergedBounds.length);
        assertArrayEquals(new float[] {-20f, -10f, 30f, 40f}, GeometryHelper.mergeBounds(boundsA, boundsB), tol);
    }

    @Test
    public void testMergeBounds_nonEnclosingNonOverlap1() {
        float[] boundsA = new float[] {-20f, -10f, 30f, 40f};
        float[] boundsB = new float[] {40f, -10f, 80f, 40f};

        float[] mergedBounds = GeometryHelper.mergeBounds(boundsA, boundsB);
        assertEquals(4, mergedBounds.length);
        assertArrayEquals(new float[] {-20f, -10f, 80f, 40f}, GeometryHelper.mergeBounds(boundsA, boundsB), tol);
    }

    @Test
    public void testMergeBounds_nonEnclosingNonOverlap2() {
        float[] boundsA = new float[] {-20f, -10f, 30f, 40f};
        float[] boundsB = new float[] {-20f, 50f, 30f, 80f};

        float[] mergedBounds = GeometryHelper.mergeBounds(boundsA, boundsB);
        assertEquals(4, mergedBounds.length);
        assertArrayEquals(new float[] {-20f, -10f, 30f, 80f}, GeometryHelper.mergeBounds(boundsA, boundsB), tol);
    }

    @Test
    public void testMergeBounds_nonEnclosingNonOverlap3() {
        float[] boundsA = new float[] {-20f, -10f, 30f, 40f};
        float[] boundsB = new float[] {30f, 40f, 80f, 90f};

        float[] mergedBounds = GeometryHelper.mergeBounds(boundsA, boundsB);
        assertEquals(4, mergedBounds.length);
        assertArrayEquals(new float[] {-20f, -10f, 80f, 90f}, GeometryHelper.mergeBounds(boundsA, boundsB), tol);
    }

    @Test
    public void testMergeBounds_nonEnclosingOverlapping1() {
        float[] boundsA = new float[] {-20f, -10f, 30f, 40f};
        float[] boundsB = new float[] {0f, 10f, 80f, 90f};

        float[] mergedBounds = GeometryHelper.mergeBounds(boundsA, boundsB);
        assertEquals(4, mergedBounds.length);
        assertArrayEquals(new float[] {-20f, -10f, 80f, 90f}, GeometryHelper.mergeBounds(boundsA, boundsB), tol);
    }


}
