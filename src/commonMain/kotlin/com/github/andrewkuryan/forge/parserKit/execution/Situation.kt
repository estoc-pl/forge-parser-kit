package com.github.andrewkuryan.forge.parserKit.execution

import com.github.andrewkuryan.forge.parserKit.transition.*

data class Situation(val state: State, val input: String, val stack: Stack)

sealed interface TransitionApplyResult {
    data class Success(val guard: Guard.Meaningful<*>, val nextSituation: Situation) : TransitionApplyResult
    sealed interface Failure : TransitionApplyResult
}

fun <N : Any> Situation.applyTransition(guard: Guard.Meaningful<N>, target: State): TransitionApplyResult =
    when (guard) {
        is Guard.Input -> applyInputTransition(guard, target)
        is Guard.Stack -> applyStackTransition(guard, target)
    }

fun <N : Any> Situation.applyInputTransition(guard: Guard.Input<N>, target: State): TransitionApplyResult =
    when (val inputMatch = guard.combinedInput.getMatch(input)) {
        is InputMatch.Failure -> inputMatch
        is InputMatch.Success -> when (val stackMatch = guard.stackPreview.getMatch(stack)) {
            is StackMatch.Failure -> stackMatch
            is StackMatch.Success -> TransitionApplyResult.Success(
                guard,
                Situation(target, input.drop(inputMatch.symbols.size), stack.applyInputGuard(guard, inputMatch))
            )
        }
    }

fun <N : Any> Situation.applyStackTransition(guard: Guard.Stack<N>, target: State): TransitionApplyResult =
    when (val stackMatch = guard.stack.getMatch(stack)) {
        is StackMatch.Failure -> stackMatch
        is StackMatch.Success -> when (val previewMatch = guard.stackPreview.getMatch(stack, stackMatch.readCount)) {
            is StackMatch.Failure -> previewMatch
            is StackMatch.Success -> when (val inputMatch = guard.inputPreview.getMatch(input)) {
                is InputMatch.Failure -> inputMatch
                is InputMatch.Success -> TransitionApplyResult.Success(
                    guard,
                    Situation(target, input, stack.applyStackGuard(guard, stackMatch))
                )
            }
        }
    }

private fun <N : Any> Stack.applyStackGuard(guard: Guard.Stack<N>, stackMatch: StackMatch.Success): Stack {
    val targetNode = StackSignal.Read.Node(
        guard.rollupTarget,
        if (guard.semanticAction is SemanticAction.Value<N, *>) guard.semanticAction.handler(
            stackMatch.frames.filterIsInstance<StackSignal.Read.Node<N>>().map { it.value }
        ) else null
    )
    return guard.stackPushAfter + targetNode + guard.stackPushBefore + this.drop(stackMatch.readCount)
}

private fun <N : Any> Stack.applyInputGuard(guard: Guard.Input<N>, inputMatch: InputMatch.Success): Stack {
    val readSignals = inputMatch.symbols.map { StackSignal.Symbol(it) }
    return guard.stackPushAfter + readSignals + guard.stackPushBefore + this
}