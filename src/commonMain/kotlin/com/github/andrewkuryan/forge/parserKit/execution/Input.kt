package com.github.andrewkuryan.forge.parserKit.execution

import com.github.andrewkuryan.forge.parserKit.transition.InputSignal
import com.github.andrewkuryan.forge.parserKit.transition.InputSlice

sealed class InputMatchResult {
    data class Success(val symbols: List<Char>) : InputMatchResult()
    data class Failure(val expected: InputSignal, val received: Char?) : InputMatchResult(),
        TransitionApplyResult.Failure

    companion object {
        fun success(symbols: List<Char>): InputMatchResult = Success(symbols)
        fun failure(expected: InputSignal, received: Char?): InputMatchResult = Failure(expected, received)
    }
}

fun InputSlice.getMatch(input: String): InputMatchResult = with(InputMatchResult) {
    foldIndexed(success(listOf())) { index, result, signal ->
        when (result) {
            is InputMatchResult.Failure -> result
            is InputMatchResult.Success -> input.getSymbolAt(index).let { symbol ->
                if (signal.matches(symbol)) symbol?.let { result.concat(it) } ?: result
                else failure(signal, symbol)
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

private fun InputMatchResult.Success.concat(symbol: Char) = InputMatchResult.Success(symbols + symbol)