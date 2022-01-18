package stack;

import kotlinx.atomicfu.AtomicRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private final AtomicRef<Node> head = new AtomicRef<>(null);
    private final List<AtomicRef<Node>> eliminationArray;
    private final Random rnd = new Random();

    private final static int ARR_SIZE = 20;
    private final static int PASS_COUNT = 100;

    public StackImpl() {
        eliminationArray = new ArrayList<>();
        for (int i = 0; i < ARR_SIZE; i++) {
            eliminationArray.add(new AtomicRef<Node>(null));
        }
    }

    @Override
    public void push(int x) {
        int c = rnd.nextInt(ARR_SIZE);
        int a = Math.max(c - 1, 0);
        int b = Math.min(c + 1, ARR_SIZE - 1);

        Node newNode = new Node(x, null);
        for (int num = a; num <= b; num++) {
            if (eliminationArray.get(num).compareAndSet(null, newNode)) {

                //spin wait
                for (int pass = 0; pass < PASS_COUNT; pass++) {
                    Node curNode = eliminationArray.get(num).getValue();
                    if (curNode.x == Integer.MAX_VALUE) {

                        eliminationArray.get(num).setValue(null);
                        return;

                    }
                }

                if (eliminationArray.get(num).compareAndSet(newNode, null)) {
                    break;
                } else {
                    eliminationArray.get(num).setValue(null);
                    return;
                }
            }
        }

        while (true) {
            Node curHead = head.getValue();
            Node newHead = new Node(x, curHead);
            if (head.compareAndSet(curHead, newHead)) {
                return;
            }
        }
    }

    @Override
    public int pop() {
        int c = rnd.nextInt(ARR_SIZE);
        int a = Math.max(c - 1, 0);
        int b = Math.min(c + 1, ARR_SIZE - 1);

        Node newElem = new Node(Integer.MAX_VALUE, null);
        for (int num = a; num <= b; num++) {
            Node curElem = eliminationArray.get(num).getValue();
            if (curElem == null || curElem.x == Integer.MAX_VALUE) {
                continue;
            }
            if (eliminationArray.get(num).compareAndSet(curElem, newElem)) {
                return curElem.x;
            }
        }

        while (true) {
            Node curHead = head.getValue();
            if (curHead == null) {
                return Integer.MIN_VALUE;
            }
            if (head.compareAndSet(curHead, curHead.next.getValue())) {
                return curHead.x;
            }
        }
    }
}