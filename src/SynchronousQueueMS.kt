import kotlinx.atomicfu.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SynchronousQueueMS<E> : SynchronousQueue<E> {
    private val q = MSQueue<Core<E>>()

    override suspend fun send(element: E) {
        while (true) {
            val h = q.head.value
            val t = q.tail.value
            if (t == h || (t.core != null && t.core.isSend.value)) {
                val res = suspendCoroutine<Any?> sc@{ cont ->
                    val core = Core(cont, element, true)
                    if (!q.enqueue(t, core)) {
                        cont.resume(false)
                        return@sc
                    }
                }
                if (res == false) {
                    continue
                } else {
                    return
                }
            } else {
                val ans = q.dequeue(h)
                if (ans == null) {
                    continue
                } else {
                    ans.elem.getAndSet(element)
                    ans.cont.value!!.resume(true)
                    return
                }
            }
        }
    }

    override suspend fun receive(): E {
        while (true) {
            val h = q.head.value
            val t = q.tail.value
            if (t == h || (t.core != null && !t.core.isSend.value)) {
                val core = Core<E>(null, null, false)
                val res = suspendCoroutine<Any?> sc@{ cont ->
                    core.cont.getAndSet(cont)
                    if (!q.enqueue(t, core)) {
                        cont.resume(false)
                        return@sc
                    }
                }
                if (res == false) {
                    continue
                } else {
                    return core.elem.value ?: throw RuntimeException()
                }
            } else {
                val ans = q.dequeue(h)
                if (ans == null) {
                    continue
                } else {
                    ans.cont.value!!.resume(true)
                    return ans.elem.value ?: throw RuntimeException()
                }
            }
        }
    }
}

class Core<E>(cont: Continuation<Any?>?, elem: E?, isSend: Boolean) {
    val cont: AtomicRef<Continuation<Any?>?> = atomic(cont)
    val elem: AtomicRef<E?> = atomic(elem)
    val isSend: AtomicBoolean = atomic(isSend)
}

class Node<T>(val core: T?, next: Node<T>?) {
    val next: AtomicRef<Node<T>?> = atomic(next)
}

class MSQueue<T> {
    val head: AtomicRef<Node<T>>
    val tail: AtomicRef<Node<T>>

    init {
        val dummy = Node<T>(null, null)
        head = atomic(dummy)
        tail = atomic(dummy)
    }

    fun enqueue(curTail: Node<T>, x: T): Boolean {
        val newTail = Node(x, null)
        if (curTail.next.compareAndSet(null, newTail)) {
            tail.compareAndSet(curTail, newTail)
            return true
        } else {
            tail.compareAndSet(curTail, curTail.next.value!!)
            return false
        }
    }

    fun dequeue(curHead: Node<T>): T? {
        val curNextHead = curHead.next.value ?: return null
        if (head.compareAndSet(curHead, curNextHead)) {
            return curNextHead.core
        } else {
            return null
        }
    }
}