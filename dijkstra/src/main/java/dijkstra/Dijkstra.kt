package dijkstra

import java.util.*
import java.util.concurrent.Phaser
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

private val NODE_DISTANCE_COMPARATOR = Comparator<Node> { o1, o2 -> Integer.compare(o1!!.distance, o2!!.distance) }

class Pair<S, T>(val x: S, val y: T)

class MultiQueue(private val size: Int) {
    private val multiQueue = ArrayList<Pair<PriorityQueue<Node>, Lock>>()
    private val rnd = Random()
    private val cmp = NODE_DISTANCE_COMPARATOR

    init {
        for (i in 1..size) {
            multiQueue.add(Pair(PriorityQueue(cmp), ReentrantLock()))
        }
    }

    fun add(node: Node) {
        while (true) {
            val ind = rnd.nextInt(size)

            if (multiQueue[ind].y.tryLock()) {
                try {
                    multiQueue[ind].x.add(node)
                    return
                } finally {
                    multiQueue[ind].y.unlock()
                }
            }
        }
    }

    fun poll(): Node? {
        while (true) {
            val ind1 = rnd.nextInt(size)
            val ind2 = rnd.nextInt(size)

            if (multiQueue[ind1].y.tryLock()) {
                try {
                    if (multiQueue[ind2].y.tryLock()) {
                        try {

                            val node1: Node? =
                                if (!multiQueue[ind1].x.isEmpty()) multiQueue[ind1].x.peek() else null
                            val node2: Node? =
                                if (!multiQueue[ind2].x.isEmpty()) multiQueue[ind2].x.peek() else null
                            return if (node1 != null && node2 != null) {
                                if (cmp.compare(node1, node2) < 0) {
                                    multiQueue[ind1].x.poll()
                                } else {
                                    multiQueue[ind2].x.poll()
                                }
                            } else {
                                if (node1 != null) {
                                    multiQueue[ind1].x.poll()
                                } else {
                                    if (node2 != null) {
                                        multiQueue[ind2].x.poll()
                                    } else {
                                        null
                                    }
                                }
                            }

                        } finally {
                            multiQueue[ind2].y.unlock()
                        }
                    }
                } finally {
                    multiQueue[ind1].y.unlock()
                }
            }
        }
    }
}

// Returns `Integer.MAX_VALUE` if a path has not been found.
fun shortestPathParallel(start: Node) {
    val workers = Runtime.getRuntime().availableProcessors()
    val q = MultiQueue(2 * workers)
    start.distance = 0
    q.add(start)
    val activeNodes = AtomicInteger(1)
    val onFinish = Phaser(workers + 1)
    repeat(workers) {
        thread {
            while (activeNodes.get() > 0) {
                val curNode: Node? = q.poll()

                if (curNode == null) {
                    if (activeNodes.get() == 0) break else continue
                }

                for (curEdge in curNode.outgoingEdges) {
                    val newDist = curNode.distance + curEdge.weight
                    while (true) {
                        val curDist = curEdge.to.distance
                        if (curDist > newDist) {
                            if (curEdge.to.casDistance(curDist, newDist)) {
                                q.add(curEdge.to)
                                activeNodes.incrementAndGet()
                                break
                            }
                        } else {
                            break
                        }
                    }
                }

                activeNodes.decrementAndGet()
            }
            onFinish.arrive()
        }
    }
    onFinish.arriveAndAwaitAdvance()
}