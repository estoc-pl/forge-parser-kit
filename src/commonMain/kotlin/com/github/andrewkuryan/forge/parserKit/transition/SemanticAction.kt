package com.github.andrewkuryan.forge.parserKit.transition

object EmptyNode

sealed class SemanticAction<N : Any> {
    data class Serialized<N : Any>(val callExpression: String) : SemanticAction<N>()
    data class Live<N : Any>(val value: (body: List<N?>) -> N?) : SemanticAction<N>()
}