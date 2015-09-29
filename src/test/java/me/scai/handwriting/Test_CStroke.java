package me.scai.handwriting;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Test_CStroke {
    private static final float floatTol = 1E-9f;

    @Test
    public void testMinMax() {
        final float x0 = -10.0f;
        final float y0 = -20.0f;

        CStroke stroke = new CStroke(x0, y0);

        assertEquals(stroke.min_x, x0, floatTol);
        assertEquals(stroke.min_y, y0, floatTol);
        assertEquals(stroke.max_x, x0, floatTol);
        assertEquals(stroke.max_y, y0, floatTol);

        final float x1 = -5.0f;
        final float y1 = -10.0f;

        stroke.addPoint(x1, y1);

        assertEquals(stroke.min_x, x0, floatTol);
        assertEquals(stroke.min_y, y0, floatTol);
        assertEquals(stroke.max_x, x1, floatTol);
        assertEquals(stroke.max_y, y1, floatTol);

        final float x2 = 10.0f;
        final float y2 = 20.0f;

        stroke.addPoint(x2, y2);

        assertEquals(stroke.min_x, x0, floatTol);
        assertEquals(stroke.min_y, y0, floatTol);
        assertEquals(stroke.max_x, x2, floatTol);
        assertEquals(stroke.max_y, y2, floatTol);

        final float x3 = -20.0f;
        final float y3 = -40.0f;

        stroke.addPoint(x3, y3);

        assertEquals(stroke.min_x, x3, floatTol);
        assertEquals(stroke.min_y, y3, floatTol);
        assertEquals(stroke.max_x, x2, floatTol);
        assertEquals(stroke.max_y, y2, floatTol);

    }
}
