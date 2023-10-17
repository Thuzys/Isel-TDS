package bsTree_structure
interface BSTree<T :Comparable<T>> {
    fun add(e: T): BSTree<T>
    operator fun contains(e: T): Boolean
    val height: Int
    fun forEachByOrder(visitor: (T)->Unit )
}
private object BSTreeEmpty: BSTree<Comparable<Any>> {
    private fun throwEmpty() : Nothing = throw NoSuchElementException("BSTree is empty.")
    fun iterator() = object : Iterator<Nothing>{
        override fun hasNext(): Boolean = false
        override fun next(): Nothing = throwEmpty()
    }
    override fun add(e: Comparable<Any>): BSTree<Comparable<Any>> = BSTreeNotEmpty(Node(e))
    override fun contains(e: Comparable<Any>): Boolean = false
    override val height: Int get() = 0
    override fun forEachByOrder(visitor: (Comparable<Any>) -> Unit) {
        iterator().forEach(visitor)
    }
}
private class BSTreeNotEmpty<T: Comparable<T>>(private val head: Node<T>): BSTree<T> {
    override fun add(e: T): BSTree<T> {
        val new = Node(head.elem).also { head.copy(it) }
        new.addNode(e)
        return BSTreeNotEmpty(new)
    }
    override fun contains(e: T): Boolean =
        head.contains(e)
    override val height: Int
        get() = deep(head)
    override fun forEachByOrder(visitor: (T) -> Unit) = forEachByOrder(head, visitor)
}
@Suppress("UNCHECKED_CAST")
fun <T : Comparable<T>> BSTree(): BSTree<T> = BSTreeEmpty as BSTree<T>
fun <T : Comparable<T>> bsTreeOf(vararg elems: T): BSTree<T> =
    if (elems.isEmpty()) BSTree()
    else
        elems.fold(BSTree()){tree, elem -> tree.add(elem)}