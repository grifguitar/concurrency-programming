package dijkstra

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class ConcurrentStressTest {

    @Test
    fun `test on trees`() {
        testOnRandomGraphs(100, 99)
    }

    @Test
    fun `test on very small graphs`() {
        testOnRandomGraphs(16, 25)
    }

    @Test
    fun `test on small graphs`() {
        testOnRandomGraphs(100, 1000)
    }

    @Test
    fun `test on big graphs`() {
        testOnRandomGraphs(10000, 100000)
    }

    private fun testOnRandomGraphs(nodes: Int, edges: Int) {
        val r = Random()
        repeat(GRAPHS) {
            val nodesList = randomConnectedGraph(nodes, edges)
            repeat(SEARCHES) {
                val from = nodesList[r.nextInt(nodes)]
                val to = nodesList[r.nextInt(nodes)]
                val seqRes = shortestPathSequential(from, to)
                clearNodes(nodesList)
                val parRes = shortestPathParallel(from, to)
                clearNodes(nodesList)
                assertEquals(seqRes, parRes)
            }
        }
    }

}

private const val GRAPHS = 10
private const val SEARCHES = 100
