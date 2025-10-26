package com.github.andrewkuryan.forge.parserKit.execution

import com.github.andrewkuryan.forge.parserKit.transition.StackSignal
import com.github.andrewkuryan.forge.parserKit.transition.StackSlice
import com.github.andrewkuryan.forge.parserKit.extensions.foldSubList
import com.github.andrewkuryan.forge.parserKit.extensions.indexOfFrom

typealias Stack = List<StackSignal.Frame>

sealed class StackMatch {
    data class Success(val frames: List<StackSignal.Read>, val readCount: Int) : StackMatch()
    data class Failure(val expected: StackSlice) : StackMatch(), TransitionApplyResult.Failure
}

fun StackSlice.getMatch(stack: Stack, position: Int = 0): StackMatch =
    fold<StackSignal.Preview, StackMatch>(StackMatch.Success(listOf(), 0)) { result, signal ->
        when (result) {
            is StackMatch.Failure -> result
            is StackMatch.Success -> when (val nextResult = signal.getMatch(stack, position + result.readCount)) {
                null -> StackMatch.Failure(this)
                else -> StackMatch.Success(result.frames + nextResult.frames, result.readCount + nextResult.readCount)
            }
        }
    }

fun StackSignal.Preview.getMatch(stack: Stack, position: Int): StackMatch.Success? = when (this) {
    is StackSignal.Bottom -> stack.getOrNull(position)
        ?.takeIfInstance<StackSignal.Bottom>()
        ?.let { StackMatch.Success(listOf(), 1) }

    is StackSignal.Symbol -> stack.getOrNull(position)
        ?.takeIfInstance<StackSignal.Symbol>()
        ?.takeIf { it == this }
        ?.let { StackMatch.Success(listOf(it), 1) }

    is StackSignal.NodeView -> stack.getOrNull(position)
        ?.takeIfInstance<StackSignal.Read.Node<*>>()
        ?.takeIf { it.view == this }
        ?.let { StackMatch.Success(listOf(it), 1) }

    is StackSignal.Marker -> stack.indexOfFrom(this, position)
        .takeIf { it != -1 }
        ?.let { markerIndex ->
            stack.foldSubList<StackSignal.Frame, List<StackSignal.Read>?>(
                listOf(),
                position,
                markerIndex
            ) { result, frame -> if (result == null || frame !is StackSignal.Symbol) null else result + frame }
        }
        ?.let { StackMatch.Success(it, it.size + 1) }
}

private inline fun <reified T> Any.takeIfInstance(): T? = this as? T