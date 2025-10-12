package com.github.andrewkuryan.forge.parserKit.execution

import com.github.andrewkuryan.forge.parserKit.transition.StackSignal
import com.github.andrewkuryan.forge.parserKit.transition.StackSlice
import com.github.andrewkuryan.forge.parserKit.extensions.foldSubList
import com.github.andrewkuryan.forge.parserKit.extensions.indexOfFrom

typealias Stack = List<StackSignal.Frame>

sealed class StackMatchResult {
    data class Success(val frames: List<StackSignal.Read>, val readCount: Int) : StackMatchResult()
    data class Failure(val signal: StackSignal.Preview, val stack: Stack, val position: Int) : StackMatchResult(),
        TransitionApplyResult.Failure

    companion object {
        fun success(frames: List<StackSignal.Read>, readCount: Int): StackMatchResult = Success(frames, readCount)
        fun success(frame: StackSignal.Read): StackMatchResult = Success(listOf(frame), 1)
        fun failure(signal: StackSignal.Preview, stack: Stack, position: Int): StackMatchResult =
            Failure(signal, stack, position)
    }
}

fun StackSlice.getMatch(stack: Stack, position: Int = 0): StackMatchResult = with(StackMatchResult) {
    fold(success(listOf(), 0)) { result, signal ->
        when (result) {
            is StackMatchResult.Failure -> result
            is StackMatchResult.Success -> when (val nextResult = signal.getMatch(stack, position + result.readCount)) {
                is StackMatchResult.Failure -> nextResult
                is StackMatchResult.Success -> result.concat(nextResult)
            }
        }
    }
}

fun StackSignal.Preview.getMatch(stack: Stack, position: Int): StackMatchResult = with(StackMatchResult) {
    when (val signal = this@getMatch) {
        is StackSignal.Bottom -> stack.getOrNull(position)
            ?.takeIfInstance<StackSignal.Bottom>()
            ?.let { success(listOf(), 1) }

        is StackSignal.Symbol -> stack.getOrNull(position)
            ?.takeIfInstance<StackSignal.Symbol>()
            ?.takeIf { it == signal }
            ?.let { success(it) }

        is StackSignal.NodeView -> stack.getOrNull(position)
            ?.takeIfInstance<StackSignal.Read.Node<*>>()
            ?.takeIf { it.view == signal }
            ?.let { success(it) }

        is StackSignal.Marker -> stack.indexOfFrom(signal, position)
            .takeIf { it != -1 }
            ?.let { markerIndex ->
                stack.foldSubList(success(listOf(), 1), position, markerIndex) { result, frame ->
                    when (result) {
                        is StackMatchResult.Failure -> result
                        is StackMatchResult.Success -> when (frame) {
                            is StackSignal.Symbol -> result.concat(frame)
                            else -> failure(signal, stack, position)
                        }
                    }
                }
            }
    } ?: failure(this@getMatch, stack, position)
}

private fun StackMatchResult.Success.concat(other: StackMatchResult.Success) =
    StackMatchResult.Success(frames + other.frames, readCount + other.readCount)

private fun StackMatchResult.Success.concat(frame: StackSignal.Read) =
    StackMatchResult.Success(frames + frame, readCount + 1)

private inline fun <reified T> Any.takeIfInstance(): T? = if (this is T) this else null