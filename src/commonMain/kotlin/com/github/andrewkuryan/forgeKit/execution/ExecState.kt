package com.github.andrewkuryan.forgeKit.execution

import com.github.andrewkuryan.forgeKit.transition.Guard
import com.github.andrewkuryan.forgeKit.transition.StackSignal
import com.github.andrewkuryan.forgeKit.transition.State
import com.github.andrewkuryan.forgeKit.transition.SyntaxNode

data class ExecState(val state: State, val input: String, val stack: Stack)

sealed interface TransitionApplyResult {
    data class Success(val nextState: ExecState) : TransitionApplyResult
    sealed interface Failure : TransitionApplyResult
}

fun <N : SyntaxNode> ExecState.applyTransition(guard: Guard.Meaningful<N>, target: State): TransitionApplyResult =
    when (guard) {
        is Guard.Input -> applyInputTransition(guard, target)
        is Guard.Stack -> applyStackTransition(guard, target)
    }

fun <N : SyntaxNode> ExecState.applyInputTransition(guard: Guard.Input<N>, target: State): TransitionApplyResult =
    when (val inputMatch = (guard.input + guard.inputPreview).getMatch(input)) {
        is InputMatchResult.Failure -> inputMatch
        is InputMatchResult.Success -> when (val stackMatch = guard.stackPreview.getMatch(stack)) {
            is StackMatchResult.Failure -> stackMatch
            is StackMatchResult.Success -> {
                val readSignals = input.take(guard.input.size).map { StackSignal.Symbol(it) }
                TransitionApplyResult.Success(
                    ExecState(
                        target,
                        input.drop(guard.input.size),
                        guard.stackPushAfter + readSignals + guard.stackPushBefore + stack
                    )
                )
            }
        }
    }

fun <N : SyntaxNode> ExecState.applyStackTransition(guard: Guard.Stack<N>, target: State): TransitionApplyResult =
    when (val stackMatch = guard.stack.getMatch(stack)) {
        is StackMatchResult.Failure -> stackMatch
        is StackMatchResult.Success -> when (
            val previewMatch = guard.stackPreview.getMatch(stack, stackMatch.readCount)
        ) {
            is StackMatchResult.Failure -> previewMatch
            is StackMatchResult.Success -> when (val inputMatch = guard.inputPreview.getMatch(input)) {
                is InputMatchResult.Failure -> inputMatch
                is InputMatchResult.Success -> {
                    val targetNode = StackSignal.Read.Node(
                        guard.rollupTarget,
                        guard.semanticAction?.handler?.invoke(stackMatch.frames)
                    )
                    TransitionApplyResult.Success(
                        ExecState(
                            target,
                            input,
                            guard.stackPushAfter + targetNode + guard.stackPushBefore + stack.drop(stackMatch.readCount)
                        )
                    )
                }
            }
        }
    }