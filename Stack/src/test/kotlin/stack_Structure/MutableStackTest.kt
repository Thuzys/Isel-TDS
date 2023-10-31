package stack_Structure

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class MutableStackTest{
    @Test
    fun `Test empty Stack`(){
        val stack = MutableStack<Int>()
        assertFailsWith<NoSuchElementException> { stack.top }
        assertFailsWith<NoSuchElementException> { stack.pop() }
    }
    @Test
    fun `Test push of an element`(){
        val stack = MutableStack<Int>()
        stack.push(10)
        assertEquals(10, stack.top)
    }
    @Suppress("UnnecessaryVariable")
    @Test
    fun `Test of mutability`(){
        val stack = MutableStack<Int>()
        val old = stack
        stack.push(10)
        assertEquals(old, stack)
    }
    @Test
    fun `Test of pops`(){
        val stack = MutableStack<Int>()
        (0..10).forEach{
            stack.push(it)
        }
        (10 downTo 0).forEach {
            assertEquals(it, stack.pop())
        }
    }
}