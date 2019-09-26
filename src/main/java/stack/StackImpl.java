package stack;

import kotlinx.atomicfu.AtomicRef;

public class StackImpl implements Stack {
    private static class Node {
        final AtomicRef<Node> next;
        final int x;

        Node(int x, Node next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }

    // head pointer
    private AtomicRef<Node> head = new AtomicRef<>(null);

    @Override
    public void push(int x) {
        head.setValue(new Node(x, head.getValue()));
    }

    @Override
    public int pop() {
        Node curHead = head.getValue();
        if (curHead == null) return Integer.MIN_VALUE;
        head.setValue(curHead.next.getValue());
        return curHead.x;
    }
}
