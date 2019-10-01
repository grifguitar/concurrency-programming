package linked_list_set;

public class SetImpl implements Set {
    private class Node {
        Node next;
        int x;

        Node(int x, Node next) {
            this.next = next;
            this.x = x;
        }
    }

    private class Window {
        Node cur, next;
    }

    private final Node head = new Node(Integer.MIN_VALUE, new Node(Integer.MAX_VALUE, null));

    /**
     * Returns the {@link Window}, where cur.x < x <= next.x
     */
    private Window findWindow(int x) {
        Window w = new Window();
        w.cur = head;
        w.next = w.cur.next;
        while (w.next.x < x) {
            w.cur = w.next;
            w.next = w.cur.next;
        }
        return w;
    }

    @Override
    public boolean add(int x) {
        Window w = findWindow(x);
        boolean res;
        if (w.next.x == x) {
            res = false;
        } else {
            w.cur.next = new Node(x, w.next);
            res = true;
        }
        return res;
    }

    @Override
    public boolean remove(int x) {
        Window w = findWindow(x);
        boolean res;
        if (w.next.x != x) {
            res = false;
        } else {
            w.cur.next = w.next.next;
            res = true;
        }
        return res;
    }

    @Override
    public boolean contains(int x) {
        Window w = findWindow(x);
        boolean res = w.next.x == x;
        return res;
    }
}