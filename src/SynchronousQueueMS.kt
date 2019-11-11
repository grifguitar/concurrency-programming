import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SynchronousQueueMS<E> : SynchronousQueue<E> {
    private val senders = ArrayList<Pair<Continuation<Unit>, E>>() // pair = continuation + element
    private val receivers = ArrayList<Continuation<E>>()
    private val lock = ReentrantLock()

    override suspend fun send(element: E) {
        lock.lock()
        if (receivers.isNotEmpty()) {
            val r = receivers.removeAt(0)
            r.resume(element)
            lock.unlock()
        } else {
            suspendCoroutine<Unit> { cont ->
                senders.add(cont to element)
                lock.unlock()
            }
        }
    }

    override suspend fun receive(): E {
        lock.lock()
        if (senders.isNotEmpty()) {
            val (s, elem) = senders.removeAt(0)
            lock.unlock()
            s.resume(Unit)
            return elem
        } else {
            return suspendCoroutine sc@ { cont ->
                receivers.add(cont)
                lock.unlock()
            }
        }
    }
}