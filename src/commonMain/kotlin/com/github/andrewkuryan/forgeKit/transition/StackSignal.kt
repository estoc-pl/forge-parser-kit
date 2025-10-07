package com.github.andrewkuryan.forgeKit.transition

typealias StackSlice = List<StackSignal.Preview>
typealias StackPush = List<StackSignal.Push>

sealed interface StackSignal {
    sealed interface Frame : StackSignal
    sealed interface Preview : StackSignal

    sealed interface Read : Frame {
        data class Node<N : Any>(val view: NodeView, val value: N?) : Read
    }

    sealed interface Push : Frame, Preview

    data object Bottom : Frame, Preview
    data class Symbol(val value: Char) : Read, Push, Preview
    data class NodeView(val name: String) : Preview
    data class Marker(val name: String) : Push, Preview
}