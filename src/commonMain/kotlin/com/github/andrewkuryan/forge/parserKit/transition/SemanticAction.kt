package com.github.andrewkuryan.forge.parserKit.transition

import kotlin.reflect.KProperty

object EmptyNode

typealias SemanticHandler<N> = (body: List<N?>) -> N?

sealed class SemanticAction<N : Any, SC : SemanticContainer<N>> {
    data class Ref<N : Any, SC : SemanticContainer<N>>(private val prop: KProperty<Value<N, SC>>) :
        SemanticAction<N, SC>() {
        val path: String get() = prop.name

        operator fun plus(other: Ref<N, SC>?): Ref<N, SC> = other ?: this

        override fun toString() = "SemanticAction($path)"
    }

    @ConsistentCopyVisibility
    data class Value<N : Any, SC : SemanticContainer<N>> internal constructor(val handler: SemanticHandler<N>) :
        SemanticAction<N, SC>() {

        override fun toString() = "SemanticAction(<kotlin code>)"
    }
}

open class SemanticContainer<N : Any> {

    protected fun semanticAction(handler: SemanticHandler<N>) = SemanticActionCreator(handler)
}

class SemanticActionCreator<N : Any> internal constructor(val handler: SemanticHandler<N>) {

    operator fun <SC : SemanticContainer<N>> getValue(thisRef: SC, prop: KProperty<*>) =
        SemanticAction.Value<N, SC>(handler)
}