import kotlinx.atomicfu.*

class DynamicArrayImpl<E> : DynamicArray<E> {
    private val core = atomic(Core<E>(INITIAL_CAPACITY))

    override fun get(index: Int): E {
        TODO("Not yet implemented")
    }

    override fun put(index: Int, element: E) {
        TODO("Not yet implemented")
    }

    override fun pushBack(element: E) {
        TODO("Not yet implemented")
    }

    override val size: Int get() {
        TODO("Not yet implemented")
    }
}

private class Core<E>(
    capacity: Int,
) {
    private val array = atomicArrayOfNulls<E>(capacity)
}

private const val INITIAL_CAPACITY = 1 // DO NOT CHANGE ME