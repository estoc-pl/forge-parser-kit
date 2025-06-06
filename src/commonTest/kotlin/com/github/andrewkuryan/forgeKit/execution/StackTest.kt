package com.github.andrewkuryan.forgeKit.execution

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import com.github.andrewkuryan.forgeKit.StackSignal
import com.github.andrewkuryan.forgeKit.StackSignal.Bottom
import com.github.andrewkuryan.forgeKit.StackSignal.Symbol
import com.github.andrewkuryan.forgeKit.StackSignal.NodeView
import com.github.andrewkuryan.forgeKit.StackSignal.Marker
import com.github.andrewkuryan.forgeKit.StackSlice

class StackTest {

    @Test
    fun `should return correct match for ⟨a⟩ in ⟨ab＄⟩`() {
        val stack: Stack = listOf(Symbol('a'), Symbol('b'), Bottom)
        val stackSlice: StackSlice = listOf(Symbol('a'))

        assertEquals(
            StackMatchResult.Success(listOf(Symbol('a')), 1),
            stackSlice.getMatch(stack)
        )
    }

    @Test
    fun `should return correct match for ⟨aEb⟩ in ⟨aEbFg＄⟩`() {
        val stack: Stack = listOf(Symbol('a'), Node("E"), Symbol('b'), Node("F"), Symbol('g'), Bottom)
        val stackSlice: StackSlice = listOf(Symbol('a'), NodeView("E"), Symbol('b'))

        assertEquals(
            StackMatchResult.Success(listOf(Symbol('a'), Node("E"), Symbol('b')), 3),
            stackSlice.getMatch(stack)
        )
    }

    @Test
    fun `should return correct match for ⟨kF＄⟩ in ⟨kF＄⟩`() {
        val stack: Stack = listOf(Symbol('k'), Node("F"), Bottom)
        val stackSlice: StackSlice = listOf(Symbol('k'), NodeView("F"), Bottom)

        assertEquals(
            StackMatchResult.Success(listOf(Symbol('k'), Node("F")), 3),
            stackSlice.getMatch(stack)
        )
    }

    @Test
    fun `should return null match for ⟨aE⟩ in ⟨aFb＄⟩`() {
        val stack: Stack = listOf(Symbol('a'), Node("F"), Symbol('b'), Bottom)
        val stackSlice: StackSlice = listOf(Symbol('a'), NodeView("E"))

        assertIs<StackMatchResult.Failure>(stackSlice.getMatch(stack))
    }

    @Test
    fun `should return null match for ⟨abc⟩ in ⟨ab⟩`() {
        val stack: Stack = listOf(Symbol('a'), Symbol('b'))
        val stackSlice: StackSlice = listOf(Symbol('a'), Symbol('b'), Symbol('c'))

        assertIs<StackMatchResult.Failure>(stackSlice.getMatch(stack))
    }

    @Test
    fun `should return correct match for ⟨E⁅a+⁆⟩ in ⟨Eaaa⁅a+⁆＄⟩`() {
        val stack: Stack = listOf(Node("E"), Symbol('a'), Symbol('a'), Symbol('a'), Marker("a+"), Bottom)
        val stackSlice: StackSlice = listOf(NodeView("E"), Marker("a+"))

        assertEquals(
            StackMatchResult.Success(listOf(Node("E"), Symbol('a'), Symbol('a'), Symbol('a')), 5),
            stackSlice.getMatch(stack)
        )
    }

    @Test
    fun `should return correct match for ⟨E⁅a+⁆b⟩ in ⟨Ea⁅a+⁆bc＄⟩`() {
        val stack: Stack = listOf(Node("E"), Symbol('a'), Marker("a+"), Symbol('b'), Symbol('c'), Bottom)
        val stackSlice: StackSlice = listOf(NodeView("E"), Marker("a+"), Symbol('b'))

        assertEquals(
            StackMatchResult.Success(listOf(Node("E"), Symbol('a'), Symbol('b')), 4),
            stackSlice.getMatch(stack)
        )
    }

    @Test
    fun `should return null match for ⟨E⁅a+⁆⟩ in ⟨Ea＄⟩`() {
        val stack: Stack = listOf(Node("E"), Symbol('a'), Bottom)
        val stackSlice: StackSlice = listOf(NodeView("E"), Marker("a+"))

        assertIs<StackMatchResult.Failure>(stackSlice.getMatch(stack))
    }

    @Test
    fun `should return null match for ⟨E⁅a+⁆＄⟩ in ⟨EaA⁅a+⁆＄⟩`() {
        val stack: Stack = listOf(Node("E"), Symbol('a'), Node("A"), Marker("a+"), Bottom)
        val stackSlice: StackSlice = listOf(NodeView("E"), Marker("a+"), Bottom)

        assertIs<StackMatchResult.Failure>(stackSlice.getMatch(stack))
    }

    @Suppress("TestFunctionName")
    private fun Node(name: String) = StackSignal.Read.Node(NodeView(name), null)
}