package com.github.andrewkuryan.forge.parserKit.parsers

import com.github.andrewkuryan.forge.parserKit.transition.Guard
import com.github.andrewkuryan.forge.parserKit.transition.SemanticContainer
import com.github.andrewkuryan.forge.parserKit.transition.State

data class PEGTable<N : Any, SC : SemanticContainer<N>>(
    val initialState: State,
    val finalStates: Set<State>,
    val table: (container: SC) -> Map<State, List<Pair<Guard.Meaningful<N>, State>>>,
)