package com.github.andrewkuryan.forgeKit.execution

import com.github.andrewkuryan.forgeKit.transition.*

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
    when (val inputMatch = guard.combinedInput.getMatch(input)) {
        is InputMatchResult.Failure -> inputMatch
        is InputMatchResult.Success -> when (val stackMatch = guard.stackPreview.getMatch(stack)) {
            is StackMatchResult.Failure -> stackMatch
            is StackMatchResult.Success -> TransitionApplyResult.Success(
                ExecState(target, input.drop(inputMatch.symbols.size), stack.applyInputGuard(guard, inputMatch))
            )
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
                is InputMatchResult.Success -> TransitionApplyResult.Success(
                    ExecState(target, input, stack.applyStackGuard(guard, stackMatch))
                )
            }
        }
    }

private fun <N : SyntaxNode> Stack.applyStackGuard(guard: Guard.Stack<N>, stackMatch: StackMatchResult.Success): Stack {
    val targetNode = StackSignal.Read.Node(
        guard.rollupTarget,
        guard.semanticAction?.handler?.invoke(stackMatch.frames)
    )
    return guard.stackPushAfter + targetNode + guard.stackPushBefore + this.drop(stackMatch.readCount)
}

private fun <N : SyntaxNode> Stack.applyInputGuard(guard: Guard.Input<N>, inputMatch: InputMatchResult.Success): Stack {
    val readSignals = inputMatch.symbols.map { StackSignal.Symbol(it) }
    return guard.stackPushAfter + readSignals + guard.stackPushBefore + this
}