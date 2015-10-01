package me.scai.handwriting;

import com.google.gson.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Test_StateStack {
    @Test
    public void testStateStack() {
        StateStack stack = new StateStack(3);

        assertEquals(3, stack.getCapacity());
        assertTrue(stack.isEmpty());

        // Push one action
        stack.push(new StrokeCuratorState(StrokeCuratorUserAction.AddStroke, new JsonObject()));
        assertEquals(StrokeCuratorUserAction.AddStroke, stack.getLastUserAction());
        assertEquals(1, stack.getSize());
        assertEquals(0, stack.getUndoneSize());

        // Undo
        stack.undo();
        assertEquals(1, stack.getSize());
        assertEquals(1, stack.getUndoneSize());
        assertNull(stack.getLastUserAction());

        // Redo
        stack.redo();
        assertEquals(1, stack.getSize());
        assertEquals(0, stack.getUndoneSize());
        assertEquals(StrokeCuratorUserAction.AddStroke, stack.getLastUserAction());

        // Push two more actions
        stack.push(new StrokeCuratorState(StrokeCuratorUserAction.ForceSetTokenName, new JsonObject()));
        stack.push(new StrokeCuratorState(StrokeCuratorUserAction.RemoveLastToken, new JsonObject()));

        assertEquals(3, stack.getSize());
        assertEquals(0, stack.getUndoneSize());
        assertEquals(StrokeCuratorUserAction.RemoveLastToken, stack.getLastUserAction());

        // Undo
        stack.undo();
        assertEquals(3, stack.getSize());
        assertEquals(1, stack.getUndoneSize());
        assertEquals(StrokeCuratorUserAction.ForceSetTokenName, stack.getLastUserAction());

        // Redo
        stack.redo();
        assertEquals(3, stack.getSize());
        assertEquals(0, stack.getUndoneSize());
        assertEquals(StrokeCuratorUserAction.RemoveLastToken, stack.getLastUserAction());

        // Redo twice
        for (int i = 0; i < 2; ++i) {
            stack.undo();
        }

        assertEquals(3, stack.getCapacity());
        assertEquals(3, stack.getSize());
        assertEquals(2, stack.getUndoneSize());

        // Push a new action, that should have reset the stack pointer to 0
        stack.push(new StrokeCuratorState(StrokeCuratorUserAction.ClearStrokes, new JsonObject()));

        assertEquals(3, stack.getCapacity());
        assertEquals(2, stack.getSize());
        assertEquals(0, stack.getUndoneSize());
        assertEquals(StrokeCuratorUserAction.ClearStrokes, stack.getLastUserAction());
    }

}
