package linked_list_set;

import kotlinx.atomicfu.*;

public class SetImpl implements Set {
    private static class MyAtomicMarkableReference {
        private final AtomicRef<NodeWrapper> nodeAtomicRef;

        MyAtomicMarkableReference(Node node, boolean flag) {
            if (!flag) {
                nodeAtomicRef = new AtomicRef<NodeWrapper>(new Alive(node));
            } else {
                nodeAtomicRef = new AtomicRef<NodeWrapper>(new Removed(node));
            }
        }

        public Node get(boolean[] flag) {
            NodeWrapper node = nodeAtomicRef.getValue();
            if (node instanceof Alive) {
                flag[0] = false;
                return ((Alive) node).node;
            }
            if (node instanceof Removed) {
                flag[0] = true;
                return ((Removed) node).node;
            }
            throw new RuntimeException("unexpected case");
        }

        public Node getReference() {
            return get(new boolean[1]);
        }

        public boolean isMarked() {
            boolean[] res = new boolean[1];
            get(res);
            return res[0];
        }

        public boolean compareAndSet(Node expectedNode, Node newNode,
                                     boolean expectedMark, boolean newMark) {
            NodeWrapper node = nodeAtomicRef.getValue();
            if (!expectedMark) {
                if (node instanceof Alive) {
                    Node node1 = ((Alive) node).node;
                    if (node1 == expectedNode) {
                        if (!newMark) {
                            return nodeAtomicRef.compareAndSet(node, new Alive(newNode));
                        } else {
                            return nodeAtomicRef.compareAndSet(node, new Removed(newNode));
                        }
                    }
                }
            } else {
                if (node instanceof Removed) {
                    Node node1 = ((Removed) node).node;
                    if (node1 == expectedNode) {
                        if (!newMark) {
                            return nodeAtomicRef.compareAndSet(node, new Alive(newNode));
                        } else {
                            return nodeAtomicRef.compareAndSet(node, new Removed(newNode));
                        }
                    }
                }
            }
            return false;
        }
    }

    private interface NodeWrapper {
    }

    private static class Removed implements NodeWrapper {
        final Node node;

        Removed(Node node) {
            this.node = node;
        }
    }

    private static class Alive implements NodeWrapper {
        final Node node;

        Alive(Node node) {
            this.node = node;
        }
    }

    private static class Node {
        MyAtomicMarkableReference next_and_flag;
        int x;

        Node(int x, Node next) {
            this.next_and_flag = new MyAtomicMarkableReference(next, false);
            this.x = x;
        }
    }

    private static class Window {
        Node cur, next;

        Window(Node cur, Node next) {
            this.cur = cur;
            this.next = next;
        }
    }

    private final Node head = new Node(Integer.MIN_VALUE, new Node(Integer.MAX_VALUE, null));

    /**
     * Returns the {@link Window}, where cur.x < x <= next.x
     */
    private Window findWindow(int x) {
        retry:
        while (true) {
            Node w_cur = head;
            Node w_next = w_cur.next_and_flag.getReference();
            boolean[] w_next_is_removed = new boolean[1];
            while (w_next.x < x) {
                Node w_next_next = w_next.next_and_flag.get(w_next_is_removed);
                if (w_next_is_removed[0]) {
                    if (!w_cur.next_and_flag.compareAndSet(w_next, w_next_next, false, false)) {
                        continue retry;
                    }
                    w_next = w_next_next;
                } else {
                    w_cur = w_next;
                    w_next = w_cur.next_and_flag.getReference();
                }
            }
            return new Window(w_cur, w_next);
        }
    }

    @Override
    public boolean add(int x) {
        while (true) {
            Window w = findWindow(x);

            if (!w.next.next_and_flag.isMarked()) {
                if (w.next.x == x) {
                    return false;
                }
            }

            Node node = new Node(x, w.next);

            if (w.cur.next_and_flag.compareAndSet(w.next, node, false, false)) {
                return true;
            }
        }
    }

    @Override
    public boolean remove(int x) {
        while (true) {
            Window w = findWindow(x);

            if (w.next.next_and_flag.isMarked() || w.next.x != x) {
                return false;
            }

            Node node = w.next.next_and_flag.getReference();

            if (w.next.next_and_flag.compareAndSet(node, node, false, true)) {
                w.cur.next_and_flag.compareAndSet(w.next, node, false, false);
                return true;
            }
        }
    }

    @Override
    public boolean contains(int x) {
        Window w = findWindow(x);
        if (!w.next.next_and_flag.isMarked()) {
            return w.next.x == x;
        }
        return false;
    }
}