package com.coalesce.utils

import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

fun <T> Stream<T>.reverse(): Stream<T> {
    val list = this.collect(Collectors.toList<T>())
    val reversed = list.indices.reversed().mapTo(LinkedList<T>()) { list[it] }
    return reversed.stream()
}