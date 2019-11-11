import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SynchronousQueueMS<E> : SynchronousQueue<E> {
    // TODO head and tail pointers

    override suspend fun send(element: E) {
        TODO("Implement me!")
    }

    override suspend fun receive(): E {
        TODO("Implement me!")
    }
}
