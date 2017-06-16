package com.coalesce.bot.utilities

import java.util.*
import java.util.concurrent.TimeUnit

fun tryLog(message: String, func: () -> Unit) =
    try {
        func()
    } catch (ex: Exception) {
        System.err.println(message)
        ex.printStackTrace()
    }

inline fun <T> Iterable<T>.allIndexed(predicate: (T, Int) -> Boolean): Boolean {
    forEachIndexed { index, element -> if (!predicate(element, index)) return false }
    return true
}

fun quietly(func: () -> Unit) = try{ func() } catch (ex: Exception) { /* Ignore */ }

fun <E> List<E>.subList(fromIndex: Int): List<E> = subList(fromIndex, size)

fun <K, V> hashTableOf(): Hashtable<K, V> = Hashtable()

fun <K, V> hashTableOf(vararg elements: Pair<K, V>): Hashtable<K, V> = Hashtable<K, V>(elements.size).apply { putAll(elements) }

fun Long.formatTimeDiff(): String {
    fun ensurePlural(numb: Long, str: String): String {
        return if (numb > 1) " ${str}s" else " $str"
    }

    val timeDiff = this

    val second = timeDiff / 1000 % 60
    val minute = timeDiff / (1000 * 60) % 60
    val hour = timeDiff / (1000 * 60 * 60) % 24
    val day = timeDiff / (1000 * 60 * 60 * 24)

    if (day > 0) return "$day${ensurePlural(day, "day")} and $hour${ensurePlural(hour, "hour")}"
    if (hour > 0) return "$hour${ensurePlural(hour, "hour")} and $minute${ensurePlural(minute, "minute")}"
    if (minute > 0) return "$minute${ensurePlural(minute, "minute")} and $second${ensurePlural(second, "second")}"
    if (second > 0) return "$second${ensurePlural(second, "second")}"
    return timeDiff.toString() + "ms"
}

fun String.truncate(from: Int, to: Int): String = if (this.length >= to) substring(from, to) + "..." else this

fun String.parseTimeUnit(): TimeUnit? {
    try {
        return TimeUnit.valueOf(this.toUpperCase())
    } catch (ex: IllegalArgumentException) {
        return null
    }
}

fun String.parseDouble(): Double? {
    try {
        return java.lang.Double.parseDouble(this)
    } catch (ex: NumberFormatException) {
        return null
    }
}

fun String.matching(regx: Regex): String {
    val matchList = regx.matchEntire(this)
    val str = StringBuilder()
    while (true) {
        str.append((matchList ?: break).next() ?: break)
    }
    return str.toString()
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

fun String.isInteger(): Boolean {
    if (this.isEmpty()) return false
    val length = this.length
    if (length == 0) {
        return false
    }
    var i = 0
    if (this[0] == '-') {
        if (length == 1) {
            return false
        }
        i = 1
    }
    while (i < length) {
        val c = this[i]
        if (c < '0' || c > '9') {
            return false
        }
        i++
    }
    return true
}

open class Timeout(time: Long, unit: TimeUnit): Thread() {
    private val lock = java.lang.Object()
    private val millis = TimeUnit.MILLISECONDS.convert(time, unit)

    var timeout: (() -> Unit)? = null

    override fun run() = interruptableCycle()
    fun keepAlive() = interrupt()

    private fun interruptableCycle() {
        try {
            lock.wait(millis)
        } catch (ie: InterruptedException) {
            interruptableCycle()
        }

        (timeout ?: return).invoke()
    }
}

val emptyClassList = listOf<Class<*>>()