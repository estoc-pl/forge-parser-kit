package com.github.andrewkuryan.forge.parserKit.execution

import com.github.andrewkuryan.forge.parserKit.transition.InputSignal
import com.github.andrewkuryan.forge.parserKit.transition.InputSlice

sealed class InputMatch {
    data class Success(val symbols: List<Char>) : InputMatch()
    data class Failure(val expected: InputSlice) : InputMatch(), TransitionApplyResult.Failure
}

fun InputSlice.getMatch(input: String): InputMatch =
    foldIndexed<InputSignal, InputMatch>(InputMatch.Success(listOf())) { index, result, signal ->
        when (result) {
            is InputMatch.Failure -> result
            is InputMatch.Success -> input.getSymbolAt(index).let { symbol ->
                val nextResult = symbol?.let { InputMatch.Success(result.symbols + it) } ?: result

                if (signal.matches(symbol)) nextResult else InputMatch.Failure(this@getMatch)
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