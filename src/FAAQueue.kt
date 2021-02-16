import kotlinx.atomicfu.*

class FAAQueue<T> {
    private val head: AtomicRef<Segment> // Head pointer, similarly to the Michael-Scott queue (but the first node is _not_ sentinel)
    private val tail: AtomicRef<Segment> // Tail pointer, similarly to the Michael-Scott queue

    init {
        val firstNode = Segment()
        head = atomic(firstNode)
        tail = atomic(firstNode)
    }

    /**
     * Adds the specified element [x] to the queue.
     */
    fun enqueue(x: T) {
        val enqIdx = tail.value.enqIdx++
        if (enqIdx >= SEGMENT_SIZE) {
            val newTail = Segment(x)
            tail.value.next = newTail
            tail.value = newTail
            return
        }
        tail.value.elements[enqIdx] = x
    }

    /**
     * Retrieves the first element from the queue
     * and returns it; returns `null` if the queue
     * is empty.
     */
    fun dequeue(): T? {
        while (true) {
            if (head.value.isEmpty) {
                if (head.value.next == null) return null
                head.value = head.value.next!!
                continue
            }
            val deqIdx = head.value.deqIdx++
            val res = head.value.elements[deqIdx]
            head.value.elements[deqIdx] = DONE
            return res as T?
        }
    }

    /**
     * Returns `true` if this queue is empty;
     * `false` otherwise.
     */
    val isEmpty: Boolean get() {
        while (true) {
            if (head.value.isEmpty) {
                if (head.value.next == null) return true
                head.value = head.value.next!!
                continue
            } else {
                return false
            }
        }
    }
}

private class Segment {
    var next: Segment? = null
    var enqIdx = 0 // index for the next enqueue operation
    var deqIdx = 0 // index for the next dequeue operation
    val elements = arrayOfNulls<Any>(SEGMENT_SIZE)

    constructor() // for the first segment creation

    constructor(x: Any?) { // each next new segment should be constructed with an element
        enqIdx = 1
        elements[0] = x
    }

    val isEmpty: Boolean get() = deqIdx >= enqIdx || deqIdx >= SEGMENT_SIZE

}

private val DONE = Any() // Marker for the "DONE" slot state; to avoid memory leaks
const val SEGMENT_SIZE = 2 // DO NOT CHANGE, IMPORTANT FOR TESTS

