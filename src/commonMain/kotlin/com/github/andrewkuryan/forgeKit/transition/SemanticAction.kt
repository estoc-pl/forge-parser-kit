package com.github.andrewkuryan.forgeKit.transition

object EmptyNode

typealias SemanticHandler<N> = (body: List<N?>) -> N?

data class SemanticAction<N : Any>(val name: String, val handler: SemanticHandler<N>)