package com.github.andrewkuryan.forgeKit.execution

import com.github.andrewkuryan.forgeKit.transition.InputSignal
import com.github.andrewkuryan.forgeKit.transition.InputSlice

sealed class InputMatchResult {
    data object Success : InputMatchResult()
    data class Failure(val expected: InputSignal, val received: Char?) : InputMatchResult(),
        TransitionApplyResult.Failure

    companion object {
        fun success(): InputMatchResult = Success
        fun failure(expected: InputSignal, received: Char?): InputMatchResult = Failure(expected, received)
    }
}

fun InputSlice.getMatch(input: String): InputMatchResult = with(InputMatchResult) {
    foldIndexed(success()) { index, result, signal ->
        when (result) {
            is InputMatchResult.Failure -> result
            is InputMatchResult.Success -> input.getSymbolAt(index).let { symbol ->
                if (signal.matches(symbol)) success() else failure(signal, symbol)
            }
        }
    }
}

fun InputSignal.matches(symbol: Char?) = when (this) {
    is InputSignal.Unitary -> matches(symbol)
    is InputSignal.Not -> (listOf(first) + rest).none { it.matches(symbol) }
}

fun InputSignal.Unitary.matches(symbol: Char?) = when (this) {
    is InputSignal.EOI -> symbol == null
    is InputSignal.Symbol -> symbol == value
    is InputSignal.Range -> symbol in value
}

private fun String.getSymbolAt(index: Int) = if (index == length) null else this[index]