package stack_Structure

class MutableStack<T> {
    private class Node<T>(val elem: T, val next: Node<T>?)
    private  var head: Node<T>? = null
    fun push(elem: T) {
        head = Node(elem, head)
    }
    val top get() = head?.elem ?: throw NoSuchElementException("Stack is empty.")
    fun pop():T = top.also { head = head?.next }
    override fun equals(other: Any?): Boolean {
        if (other !is MutableStack<*>) return false
        var myCurr = head
        var otherCurr = other.head
        while (myCurr != null && otherCurr != null){
            if (myCurr.elem != otherCurr.elem)
                return false
            myCurr = myCurr.next
            otherCurr = otherCurr.next
        }
        return myCurr == null && otherCurr == null
    }
    override fun hashCode(): Int {
        var finalHash = 0
        var myCurr = head
        while (myCurr != null)
            finalHash += myCurr.elem.hashCode().also { myCurr = myCurr?.next }
        return finalHash
    }
}