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