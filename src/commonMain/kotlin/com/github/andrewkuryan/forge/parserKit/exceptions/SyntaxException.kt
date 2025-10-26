package com.github.andrewkuryan.forge.parserKit.exceptions

import com.github.andrewkuryan.forge.parserKit.transition.InputSignal
import com.github.andrewkuryan.forge.parserKit.transition.InputSlice
import kotlin.math.min

class SyntaxException(val failures: Set<InputSlice>, val position: Int, val input: String) : Exception() {

    override val message: String
        get() = "[Syntax Error]: Unexpected symbol on position $position: " +
                "${failures.format()} expected, but received " + input.format(failures, position)
}

private fun String.format(failures: Set<InputSlice>, position: Int) =
    substring(position, min(position + failures.maxOf { it.size }, length))
        .takeIf { it.isNotEmpty() }
        ?: "End-Of-Input"

private fun Set<InputSlice>.format() =
    if (size == 1) first().last().format()
    else take(size - 1).joinToString(", ") { it.last().format() } + " or " + last().last().format()

private fun InputSignal.Unitary.format() = when (this) {
    is InputSignal.EOI -> "End-Of-Input"
    is InputSignal.Symbol -> "'${value}'"
    is InputSignal.Range -> "'${value.first}'..'${value.last}'"
}

private fun InputSignal.format() = when (this) {
    is InputSignal.Unitary -> format()
    is InputSignal.Not -> "NOT(${(listOf(first) + rest).joinToString(" | ") { it.format() }}"
}