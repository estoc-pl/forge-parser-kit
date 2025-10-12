package com.github.andrewkuryan.forge.parserKit.transition

import kotlin.jvm.JvmInline

@JvmInline value class State(val index: Int)

abstract class Transition<N : Any> {

    abstract val source: State
    abstract val target: State
    abstract val guard: Guard

    val isLoop: Boolean get() = source == target
}

data class EmptyTransition<N: Any>(
    override val source: State,
    override val target: State,
) : Transition<N>() {

    override val guard = Guard.Empty
}

data class MeaningfulTransition<N : Any>(
    override val source: State,
    override val target: State,
    override val guard: Guard.Meaningful<N>,
) : Transition<N>()

sealed class Guard {
    abstract val inputSize: Int
    abstract val stackSize: Int

    data object Empty : Guard() {

        override val inputSize = 0
        override val stackSize = 0
    }

    sealed class Meaningful<N : Any> : Guard() {
        abstract val inputPreview: InputSlice
        abstract val stackPreview: StackSlice
        abstract val stackPushBefore: StackPush
        abstract val stackPushAfter: StackPush
    }

    data class Input<N : Any>(
        val input: InputSlice,
        override val inputPreview: InputSlice = emptyList(),
        override val stackPreview: StackSlice = emptyList(),
        override val stackPushBefore: StackPush = emptyList(),
        override val stackPushAfter: StackPush = emptyList(),
    ) : Meaningful<N>() {

        val combinedInput = input + inputPreview

        override val inputSize = input.size + inputPreview.size
        override val stackSize = stackPreview.size
    }

    data class Stack<N : Any>(
        val rollupTarget: StackSignal.NodeView,
        val stack: StackSlice = emptyList(),
        val semanticAction: SemanticAction<N>? = null,
        override val inputPreview: InputSlice = emptyList(),
        override val stackPreview: StackSlice = emptyList(),
        override val stackPushBefore: StackPush = emptyList(),
        override val stackPushAfter: StackPush = emptyList(),
    ) : Meaningful<N>() {

        override val inputSize = inputPreview.size
        override val stackSize = stack.size + stackPreview.size
    }
}