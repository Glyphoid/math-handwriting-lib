package me.scai.handwriting;

import com.google.gson.JsonObject;
import me.scai.handwriting.utils.LimitedStack;

public class StateStack {
    /* Constants */

    /* Member variables */
    private LimitedStack<HandwritingEngineState> limitedStack;

    private int capacity;
    private int stackPointer;

    /* Constructors */
    public StateStack(int capacity) {
        this.capacity = capacity;
        this.limitedStack = new LimitedStack<>(capacity);

        this.stackPointer = 0;
    }

    public void push(HandwritingEngineState state) {
        /* Pop out all the states above the stack pointer */
        int nToPop = stackPointer;

        for (int n = 0; n < nToPop; ++n) {
            limitedStack.pop();
        }

        limitedStack.push(state);

        stackPointer = 0;
    }

    public void undo() {
        if (canUndo()) {
            stackPointer++;
            assert stackPointer >= 0 && stackPointer <= limitedStack.size();
        } else {
            throw new IllegalStateException("No more state to undo");
        }
    }

    public void redo() {
        if (canRedo()) {
            stackPointer--;
            assert stackPointer >= 0 && stackPointer <= limitedStack.size();
        } else {
            throw new IllegalStateException("No more state to redo");
        }
    }

    public boolean canUndo() {
        return stackPointer < limitedStack.size();
    }

    public boolean canRedo() {
        return stackPointer > 0;
    }

    public StrokeCuratorUserAction getLastUserAction() {
        if ( stackPointer < limitedStack.size() ) {
            return limitedStack.get(stackPointer).getUserAction();
        } else {
            return null;
        }
    }

    public JsonObject getLastSerializedState() {
        if ( stackPointer < limitedStack.size() ) {
            return limitedStack.get(stackPointer).getState();
        } else {
            return null;
        }
    }

    public HandwritingEngineState getLastState() {
        if ( stackPointer < limitedStack.size() ) {
            return limitedStack.get(stackPointer);
        } else {
            return null;
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isEmpty() {
        return limitedStack.isEmpty();
    }

    public int getSize() {
        return limitedStack.size();
    }

    /**
     * Get the number of states that have been undone, but are still in the stack and can potentially be redone.
     * @return
     */
    public int getUndoneSize() {
        return stackPointer;
    }

}
