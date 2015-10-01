package me.scai.handwriting.utils;


import java.util.LinkedList;

public class LimitedStack<T> {
    private LinkedList<T> stack = new LinkedList<>();
    private int capacity;

    /* Constructors */
    public LimitedStack(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Negative capacity is illegal");
        }

        this.capacity = capacity;
    }

    public void push(T obj) {
        stack.push(obj);

        if (stack.size() > capacity) {
            stack.removeLast();
        }
    }

    public T pop() {
        return stack.pop();
    }

    public T peek() {
        return stack.peek();
    }

    public T get(int i) {
        return stack.get(i);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int getCapacity() {
        return capacity;
    }

    public int size() {
        return stack.size();
    }
}
