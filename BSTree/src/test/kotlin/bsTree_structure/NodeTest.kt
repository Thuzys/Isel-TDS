package bsTree_structure

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NodeTest{
    @Test fun `copy of a node`(){
        val node = Node(8).also { it.addNode(9) }
        val new = node.copy(Node(node.elem))
        assertNotEquals(node, new)
    }
    @Test fun `contains validation`(){
        val node = Node("First").also { it.addNode("Second") }
        assertTrue(node.contains("Second"))
        assertFalse(node.contains("Third"))
        node.addNode("Third")
        assertTrue(node.contains("Third"))
    }
    @Test fun `height of a node`(){
        val node = Node(9)
        (8 downTo 0).forEach { node.addNode(it) }
        assertEquals(10, deep(node))
    }
    @Test fun `for each by order`(){
        val node = Node(9)
        (8 downTo 0).forEach { node.addNode(it) }
        val list = mutableListOf<Int>()
        forEachByOrder(node){ list.add(it) }
        assertEquals((0 .. 9).toList(), list)
    }
}