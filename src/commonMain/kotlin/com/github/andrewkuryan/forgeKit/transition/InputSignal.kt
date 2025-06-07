package com.github.andrewkuryan.forgeKit.transition

typealias InputSlice = List<InputSignal>

sealed class InputSignal {
    sealed class Unitary : InputSignal()

    data object EOI : Unitary()
    data class Symbol(val value: Char) : Unitary()
    data class Range(val value: CharRange) : Unitary()
    data class Not(val first: Unitary, val rest: List<Unitary> = listOf()) : InputSignal()
}