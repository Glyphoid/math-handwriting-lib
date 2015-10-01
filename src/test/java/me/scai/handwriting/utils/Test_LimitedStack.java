package me.scai.handwriting.utils;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class Test_LimitedStack {
    @Test
    public void testLimitedStack() {
        LimitedStack<Integer> stack = new LimitedStack<>(3);

        assertEquals(3, stack.getCapacity());
        assertEquals(0, stack.size());
        assertTrue(stack.isEmpty());

        stack.push(10);
        assertTrue(stack.peek() == 10);
        assertFalse(stack.isEmpty());

        stack.push(20);
        assertTrue(stack.peek() == 20);
        assertTrue(stack.get(0) == 20);
        assertTrue(stack.get(1) == 10);

        assertTrue(stack.pop() == 20);
        assertTrue(stack.peek() == 10);

        stack.push(20);
        stack.push(30);
        assertEquals(3, stack.size());

        stack.push(40);
        assertEquals(3, stack.size());
        assertTrue(stack.pop() == 40);
        assertTrue(stack.pop() == 30);
        assertTrue(stack.pop() == 20);

        assertTrue(stack.isEmpty());

        NoSuchElementException caughtException = null;
        try {
            stack.pop();
        } catch (NoSuchElementException e) {
            caughtException = e;
        }
        assertNotNull(caughtException);
    }
}
