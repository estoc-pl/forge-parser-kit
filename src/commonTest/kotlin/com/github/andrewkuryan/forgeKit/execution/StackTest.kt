package com.github.andrewkuryan.forgeKit.execution

import kotlin.test.Test
import kotlin.test.assertEquals
import com.github.andrewkuryan.forgeKit.StackSignal.Bottom
import com.github.andrewkuryan.forgeKit.StackSignal.Symbol
import com.github.andrewkuryan.forgeKit.StackSignal.Read.Node
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
        val stack: Stack = listOf(Symbol('a'), Node("E", null), Symbol('b'), Node("F", null), Symbol('g'), Bottom)
        val stackSlice: StackSlice = listOf(Symbol('a'), NodeView("E"), Symbol('b'))

        assertEquals(
            StackMatchResult.Success(listOf(Symbol('a'), Node("E", null), Symbol('b')), 3),
            stackSlice.getMatch(stack)
        )
    }

    @Test
    fun `should return correct match for ⟨kF＄⟩ in ⟨kF＄⟩`() {
        val stack: Stack = listOf(Symbol('k'), Node("F", null), Bottom)
        val stackSlice: StackSlice = listOf(Symbol('k'), NodeView("F"), Bottom)

        assertEquals(
            StackMatchResult.Success(listOf(Symbol('k'), Node("F", null)), 3),
            stackSlice.getMatch(stack)
        )
    }

    @Test
    fun `should return null match for ⟨aE⟩ in ⟨aFb＄⟩`() {
        val stack: Stack = listOf(Symbol('a'), Node("F", null), Symbol('b'), Bottom)
        val stackSlice: StackSlice = listOf(Symbol('a'), NodeView("E"))

        assertEquals(StackMatchResult.Failure(1), stackSlice.getMatch(stack))
    }

    @Test
    fun `should return null match for ⟨abc⟩ in ⟨ab⟩`() {
        val stack: Stack = listOf(Symbol('a'), Symbol('b'))
        val stackSlice: StackSlice = listOf(Symbol('a'), Symbol('b'), Symbol('c'))

        assertEquals(StackMatchResult.Failure(2), stackSlice.getMatch(stack))
    }

    @Test
    fun `should return correct match for ⟨E⁅a+⁆⟩ in ⟨Eaaa⁅a+⁆＄⟩`() {
        val stack: Stack = listOf(Node("E", null), Symbol('a'), Symbol('a'), Symbol('a'), Marker("a+"), Bottom)
        val stackSlice: StackSlice = listOf(NodeView("E"), Marker("a+"))

        assertEquals(
            StackMatchResult.Success(listOf(Node("E", null), Symbol('a'), Symbol('a'), Symbol('a')), 5),
            stackSlice.getMatch(stack)
        )
    }

    @Test
    fun `should return correct match for ⟨E⁅a+⁆b⟩ in ⟨Ea⁅a+⁆bc＄⟩`() {
        val stack: Stack = listOf(Node("E", null), Symbol('a'), Marker("a+"), Symbol('b'), Symbol('c'), Bottom)
        val stackSlice: StackSlice = listOf(NodeView("E"), Marker("a+"), Symbol('b'))

        assertEquals(
            StackMatchResult.Success(listOf(Node("E", null), Symbol('a'), Symbol('b')), 4),
            stackSlice.getMatch(stack)
        )
    }

    @Test
    fun `should return null match for ⟨E⁅a+⁆⟩ in ⟨Ea＄⟩`() {
        val stack: Stack = listOf(Node("E", null), Symbol('a'), Bottom)
        val stackSlice: StackSlice = listOf(NodeView("E"), Marker("a+"))

        assertEquals(StackMatchResult.Failure(1), stackSlice.getMatch(stack))
    }

    @Test
    fun `should return null match for ⟨E⁅a+⁆＄⟩ in ⟨EaA⁅a+⁆＄⟩`() {
        val stack: Stack = listOf(Node("E", null), Symbol('a'), Node("A", null), Marker("a+"), Bottom)
        val stackSlice: StackSlice = listOf(NodeView("E"), Marker("a+"), Bottom)

        assertEquals(StackMatchResult.Failure(1), stackSlice.getMatch(stack))
    }
}