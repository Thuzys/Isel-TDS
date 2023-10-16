package bsTree_structure
interface BSTree<T :Comparable<T>> {
    fun add(e: T): BSTree<T>
    operator fun contains(e: T): Boolean
    val height: Int
    fun forEachByOrder(visitor: (T)->Unit )
}
private object BSTreeEmpty: BSTree<Comparable<Any>>{
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

private class Node<T>(val elem: T, var left: Node<T>? = null, var rigth: Node<T>? = null)
private fun <T: Comparable<T>>Node<T>?.addNode(e: T): Boolean{
    if(this == null)
        return false
    when(elem.compareTo(e)){
        0 -> {
            if (!left.addNode(e))
                left = Node(e)
        }
        1 -> {
            if (!left.addNode(e))
                left = Node(e)
        }
        -1 -> {
            if(!rigth.addNode(e))
                rigth = Node(e)
        }
    }
    return true
}

private fun <T: Comparable<T>>Node<T>?.contains(e: T):Boolean =
    if(this == null)
        false
    else
        when(elem.compareTo(e)){
            0 -> true
            1 -> left.contains(e)
            else -> rigth.contains(e)
        }
private fun <T: Comparable<T>> deep(node: Node<T>?, heigth: Int = 0): Int{
    if (node == null)
        return heigth
    val left = deep(node.left, heigth+1)
    val rigth = deep(node.rigth, heigth+1)
    return if (left > rigth)
        left
    else
        rigth
}
private fun <T: Comparable<T>> forEachByOrder(node: Node<T>?, visitor: (T) -> Unit){
    if (node == null)
        return
    forEachByOrder(node.left, visitor)
    visitor(node.elem)
    forEachByOrder(node.rigth, visitor)
    visitor(node.elem)
}
private class BSTreeNotEmpty<T: Comparable<T>>(private val head: Node<T>): BSTree<T>{
    override fun add(e: T): BSTree<T> {
        head.addNode(e).also { return BSTreeNotEmpty(head) }
    }
    override fun contains(e: T): Boolean = head.contains(e)
    override val height: Int
        get() = deep(head)
    override fun forEachByOrder(visitor: (T) -> Unit) = forEachByOrder(head, visitor)
}

@Suppress("UNCHECKED_CAST")
fun <T : Comparable<T>> BSTree(): BSTree<T> = BSTreeEmpty as BSTree<T>
fun <T : Comparable<T>> bsTreeOf(vararg elems: T): BSTree<T> =
    elems.fold(BSTree()){tree, elem -> tree.add(elem)}