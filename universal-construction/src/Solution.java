/**
 * @author Khlytin Grigoriy
 */
public class Solution implements AtomicCounter {
    final Node head = new Node(0);

    final ThreadLocal<Node> tail = ThreadLocal.withInitial(() -> head);

    public int getAndAdd(int x) {
        while (true) {
            int cur_x = tail.get().x;

            Node node = new Node(cur_x + x);

            Node new_node = tail.get().consensus.decide(node);

            tail.set(new_node);

            if (new_node == node) {
                return cur_x;
            }
        }
    }

    private static class Node {
        final int x;
        final Consensus<Node> consensus = new Consensus<>();

        Node(int x) {
            this.x = x;
        }
    }
}