package stack;

public class StackImpl implements Stack {
    private static class Node {
        final Node next;
        final int x;

        Node(int x, Node next) {
            this.next = next;
            this.x = x;
        }
    }

    // head pointer
    private Node head = null;

    @Override
    public void push(int x) {
        head = new Node(x, head);
    }

    @Override
    public int pop() {
        Node curHead = head;
        if (curHead == null) return -1;
        head = curHead.next;
        return curHead.x;
    }
}
