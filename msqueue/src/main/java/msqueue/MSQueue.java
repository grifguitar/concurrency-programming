package msqueue;

import kotlinx.atomicfu.AtomicRef;

public class MSQueue implements Queue {
    private static class Node {
        final int x;
        final AtomicRef<Node> next;

        Node(int x, Node next) {
            this.x = x;
            this.next = new AtomicRef<>(next);
        }
    }

    private final AtomicRef<Node> head;
    private final AtomicRef<Node> tail;

    public MSQueue() {
        Node dummy = new Node(0, null);
        this.head = new AtomicRef<>(dummy);
        this.tail = new AtomicRef<>(dummy);
    }

    @Override
    public void enqueue(int x) {
        Node newTail = new Node(x, null);
        while (true) {
            Node curTail = tail.getValue();
            if (curTail.next.compareAndSet(null, newTail)) {
                tail.compareAndSet(curTail, newTail);
                return;
            } else {
                tail.compareAndSet(curTail, curTail.next.getValue());
            }
        }
    }

    @Override
    public int dequeue() {
        while (true) {
            Node curHead = head.getValue();
            Node curNextHead = curHead.next.getValue();
            if (curNextHead == null) {
                return Integer.MIN_VALUE;
            }

            Node curTail = tail.getValue();
            if (curHead == curTail) {
                tail.compareAndSet(curTail, curTail.next.getValue());
            }

            if (head.compareAndSet(curHead, curNextHead)) {
                return curNextHead.x;
            }
        }
    }

    @Override
    public int peek() {
        Node curHead = head.getValue();
        Node curNextHead = curHead.next.getValue();
        if (curNextHead == null) {
            return Integer.MIN_VALUE;
        } else {
            return curNextHead.x;
        }
    }
}