package bsTree_structure

internal class Node<T: Comparable<T>>(val elem: T, var left: Node<T>? = null, var rigth: Node<T>? = null) {
    fun addNode(e: T) {
        val dif = e.compareTo(elem)
        when {
            dif >= 1 -> {
                if (rigth != null)
                    rigth?.addNode(e)
                else
                    rigth = Node(e)
            }

            else -> {
                if (left != null)
                    left?.addNode(e)
                else
                    left = Node(e)
            }
        }
    }
}
internal fun <T: Comparable<T>> Node<T>?.contains(e: T):Boolean =
    if (this == null)
        false
    else {
        val dif = e.compareTo(elem)
        when {
            dif == 0 -> true
            dif <= 0 -> left.contains(e)
            else -> rigth.contains(e)
        }
    }
internal fun <T: Comparable<T>> deep(node: Node<T>?, heigth: Int = 0): Int{
    if (node == null)
        return heigth
    val left = deep(node.left, heigth+1)
    val rigth = deep(node.rigth, heigth+1)
    return if (left > rigth)
        left
    else
        rigth
}
internal fun <T: Comparable<T>> forEachByOrder(node: Node<T>?, visitor: (T) -> Unit){
    if (node == null)
        return
    forEachByOrder(node.left, visitor)
    visitor(node.elem)
    forEachByOrder(node.rigth, visitor)
}
internal fun <T: Comparable<T>> Node<T>?.copy(newNode:Node<T>?){
    if (this == null)
        return
    newNode?.rigth = rigth?.let { Node(it.elem) }
    newNode?.left = left?.let { Node(it.elem) }
    left.copy(newNode?.left)
    rigth.copy(newNode?.rigth)
}