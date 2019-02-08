package dijkstra

import org.junit.Test
import kotlin.test.assertEquals

class SimpleTest {

    @Test
    fun `Dijkstra on a small graph`() {
        val a = Node()
        val b = Node()
        val c = Node()
        val d = Node()
        val e = Node()
        a.addEdge(Edge(b, 2))
        a.addEdge(Edge(d, 1))
        b.addEdge(Edge(c, 4))
        b.addEdge(Edge(e, 5))
        c.addEdge(Edge(e, 1))
        d.addEdge(Edge(c, 3))
        val nodes = listOf(a, b, c, d, e)

        assertEquals(2, shortestPathSequential(a, b))
        clearNodes(nodes)
        assertEquals(2, shortestPathParallel(a, b))
        clearNodes(nodes)

        assertEquals(4, shortestPathSequential(a, c))
        clearNodes(nodes)
        assertEquals(4, shortestPathParallel(a, c))
        clearNodes(nodes)

        assertEquals(1, shortestPathSequential(a, d))
        clearNodes(nodes)
        assertEquals(1, shortestPathParallel(a, d))
        clearNodes(nodes)

        assertEquals(5, shortestPathSequential(a, e))
        clearNodes(nodes)
        assertEquals(5, shortestPathParallel(a, e))
        clearNodes(nodes)
    }
}