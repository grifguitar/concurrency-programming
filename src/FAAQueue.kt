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
        while (true) {
            val curTail = tail.value
            val enqIdx = curTail.enqIdx.getAndIncrement()
            if (enqIdx >= SEGMENT_SIZE) {
                val newTail = Segment(x)
                if (curTail.next.compareAndSet(null, newTail)) {
                    tail.compareAndSet(curTail, newTail)
                    return
                } else {
                    val nextTail = curTail.next.value
                    if (nextTail != null) {
                        tail.compareAndSet(curTail, nextTail)
                    }
                }
            } else {
                if (curTail.elements[enqIdx].compareAndSet(null, x)) {
                    return
                }
            }
        }
    }

    /**
     * Retrieves the first element from the queue
     * and returns it; returns `null` if the queue
     * is empty.
     */
    fun dequeue(): T? {
        while (true) {
            val curHead = head.value
            val deqIdx = curHead.deqIdx.getAndIncrement()
            if (deqIdx >= SEGMENT_SIZE) {
                val nextHead = curHead.next.value ?: return null
                head.compareAndSet(curHead, nextHead)
                continue
            }
            val res = curHead.elements[deqIdx].getAndSet(DONE) ?: continue
            return res as T?
        }
    }

    /**
     * Returns `true` if this queue is empty;
     * `false` otherwise.
     */
    val isEmpty: Boolean
        get() {
            while (true) {
                val curHead = head.value
                val deqIdx = curHead.deqIdx.value
                if (deqIdx >= SEGMENT_SIZE) {
                    val nextHead = curHead.next.value ?: return true
                    head.compareAndSet(curHead, nextHead)
                    continue
                } else {
                    return false
                }
            }
        }
}

private class Segment {
    val next: AtomicRef<Segment?> = atomic(null)
    val enqIdx: AtomicInt // index for the next enqueue operation
    val deqIdx: AtomicInt // index for the next dequeue operation
    val elements: AtomicArray<Any?> = atomicArrayOfNulls(SEGMENT_SIZE)

    constructor() { // for the first segment creation
        enqIdx = atomic(0)
        deqIdx = atomic(0)
    }

    constructor(x: Any?) { // each next new segment should be constructed with an element
        enqIdx = atomic(1)
        deqIdx = atomic(0)
        elements[0].getAndSet(x)
    }

}

private val DONE = Any() // Marker for the "DONE" slot state; to avoid memory leaks
const val SEGMENT_SIZE = 2 // DO NOT CHANGE, IMPORTANT FOR TESTS

