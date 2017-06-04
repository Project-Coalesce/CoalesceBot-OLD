package com.coalesce.bot.utilities

import java.util.*

fun tryLog(message: String, func: () -> Unit) =
    try {
        func()
    } catch (ex: Exception) {
        System.err.println(message)
        ex.printStackTrace()
    }

fun quietly(func: () -> Unit) = try{ func() } catch (ex: Exception) { /* Ignore */ }

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