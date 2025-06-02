package com.github.andrewkuryan.forgeKit.extensions

fun <T> List<T>.indexOfFrom(element: T, fromIndex: Int): Int {
    for (i in maxOf(fromIndex, 0) until size) {
        if (this[i] == element) {
            return i
        }
    }
    return -1
}

fun <T, R> List<T>.foldSubList(initial: R, fromIndex: Int, toIndex: Int, operation: (acc: R, T) -> R): R {
    var accumulator = initial
    for (i in maxOf(fromIndex, 0) until minOf(toIndex, size)) {
        accumulator = operation(accumulator, this[i])
    }
    return accumulator
}