package com.coalesce.bot.utilities

import java.util.*

fun <K, V> hashTableOf(): Hashtable<K, V> = Hashtable()

fun <K, V> hashTableOf(vararg elements: Pair<K, V>): Hashtable<K, V> = Hashtable<K, V>(elements.size).apply { putAll(elements) }

fun String.parseDouble(): Double? {
    try {
        return java.lang.Double.parseDouble(this)
    } catch (ex: NumberFormatException) {
        return null
    }
}

fun String.limit(limit: Int, ending: String = "..."): String {
    if (length > limit) {
        return this.substring(0, limit) + ending
    }
    return this
}

inline fun ifDo(can: Boolean, crossinline todo: () -> Unit) {
    if (can) {
        todo.invoke()
    }
}

inline fun ifDo(can: () -> Boolean, crossinline todo: () -> Unit) {
    if (can.invoke()) {
        todo.invoke()
    }
}

inline fun <T> ifwithDo(can: (T) -> Boolean, with: T, crossinline todo: () -> Unit) {
    if (can.invoke(with)) {
        todo.invoke()
    }
}