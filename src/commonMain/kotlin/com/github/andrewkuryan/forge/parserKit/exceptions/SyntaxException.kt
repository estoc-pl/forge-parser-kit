package com.github.andrewkuryan.forge.parserKit.exceptions

import com.github.andrewkuryan.forge.parserKit.execution.InputMatchResult
import com.github.andrewkuryan.forge.parserKit.transition.InputSignal

class SyntaxException(val failures: List<InputMatchResult.Failure>, val position: Int) : Exception() {

    override val message: String
        get() = "[Syntax Error]: Unexpected symbol on position $position: " +
                "${failures.format()} expected, but received ${failures.first().received.format()}"
}

private fun Char?.format() = if (this == null) "End-Of-Input" else "'${this}'"

private fun List<InputMatchResult.Failure>.format() =
    if (size == 1) first().expected.format()
    else take(size - 1).joinToString(", ") { it.expected.format() } + " or " + last().expected.format()

private fun InputSignal.Unitary.format() = when (this) {
    is InputSignal.EOI -> "End-Of-Input"
    is InputSignal.Symbol -> "'${value}'"
    is InputSignal.Range -> "'${value.first}'..'${value.last}'"
}

private fun InputSignal.format() = when (this) {
    is InputSignal.Unitary -> format()
    is InputSignal.Not -> "NOT(${(listOf(first) + rest).joinToString(" | ") { it.format() }}"
}