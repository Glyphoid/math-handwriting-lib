package me.scai.handwriting;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Test_Rectangle {
    private static final float floatTol = 1e-9f;

    @Test
    public void testRectangle1() {
        Rectangle rect = new Rectangle(new float[] {0f, 0f, 1f, 2f});

        assertEquals(0.5f, rect.getCentralX(), floatTol);
        assertEquals(1f, rect.getCentralY(), floatTol);
    }

    @Test
    public void testRectangle2() {
        Rectangle rect = new Rectangle(0f, 0f, 1f, 2f);

        assertEquals(0.5f, rect.getCentralX(), floatTol);
        assertEquals(1f, rect.getCentralY(), floatTol);
    }
}
