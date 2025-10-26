package com.github.andrewkuryan.forge.parserKit.parsers

import com.github.andrewkuryan.forge.parserKit.transition.Guard
import com.github.andrewkuryan.forge.parserKit.transition.SemanticContainer
import com.github.andrewkuryan.forge.parserKit.transition.State

typealias PEGTableRow<N> = List<Pair<Guard.Meaningful<N>, State>>
typealias PEGTable<N> = Map<State, PEGTableRow<N>>

data class PEGConfig<N : Any, SC : SemanticContainer<N>>(
    val initialState: State,
    val finalStates: Set<State>,
    val getTable: (container: SC) -> PEGTable<N>,
)

