package com.coalesce.bot.utilities

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.IMentionable
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.Color
import java.io.InputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

var EmbedBuilder.embColor: Color?
    set(color) { setColor(color) }
    get() = null

var EmbedBuilder.embTitle: String?
    set(title) { setTitle(title, null) }
    get() = null

var EmbedBuilder.embDescription: String?
    set(description) { setDescription(description) }
    get() = null

fun EmbedBuilder.description(builder: StringBuilder.() -> Unit) {
    embDescription = StringBuilder().apply(builder).toString()
}

fun <T> Iterable<T>.count(check: (T) -> Boolean): Int {
    var amount = 0
    forEach { if (check(it)) amount ++ }
    return amount
}

fun <T> List<T>.startsWith(obj: T) = isNotEmpty() && first() == obj

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

fun String.smallTimeUnit(): TimeUnit? {
    TimeUnit.values().forEach {
        if (it.toString().startsWith(this.toUpperCase())) return@smallTimeUnit it
    }

    return null
}

fun quietly(func: () -> Unit) = try{ func() } catch (ex: Exception) { /* Ignore */ }

fun <E> List<E>.subList(fromIndex: Int): List<E> = subList(fromIndex, size)

fun <K, V> hashTableOf(): Hashtable<K, V> = Hashtable()

fun <K, V> hashTableOf(vararg elements: Pair<K, V>): Hashtable<K, V> = Hashtable<K, V>(elements.size).apply { putAll(elements) }

fun Long.formatTime(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(calendar.time)
}

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

fun <T> List<T>.order(func: (T, T) -> Int): MutableList<T> {
    val list = ArrayList(this)
    Collections.sort(list, func)
    return list
}

fun InputStream.readText(charset: Charset = Charsets.UTF_8): String = readBytes().toString(charset)

fun <T> List<T>.orderSelf(func: (T, T) -> Int) = Collections.sort(this, func)

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

interface Embeddables {
    fun embed(): EmbedBuilder {
        return EmbedBuilder()
    }

    fun makeField(title: String?, text: String, inline: Boolean = false): MessageEmbed.Field {
        return MessageEmbed.Field(title, text, inline)
    }

    fun makeField(title: String?, user: IMentionable, inline: Boolean = false): MessageEmbed.Field {
        return makeField(title, user.asMention, inline)
    }

    fun EmbedBuilder.field(title: String?, text: String, inline: Boolean = false): EmbedBuilder {
        return this.addField(makeField(title, text, inline))
    }

    fun EmbedBuilder.field(title: String?, user: IMentionable, inline: Boolean = false): EmbedBuilder {
        return field(title, user.asMention, inline)
    }

    fun EmbedBuilder.data(title: String?, colour: Color? = null, author: String? = null, avatar: String? = null, url: String? = null): EmbedBuilder {
        return apply {
            setTitle(title, url)
            setAuthor(author, null, avatar)
            setColor(colour)
        }
    }
}

class SingleThreadedTimeout: Thread() {
    private val lock = java.lang.Object()
    private val timeouts = mutableListOf<Timeout>()
    private val actualTime = mutableMapOf<Timeout, Long>()

    init {
        name = "Single Threaded Timeout Task"
        start()
    }

    override fun run() = interruptableCycle()

    private fun interruptableCycle() {
        synchronized(lock) {
            try {
                if (timeouts.isEmpty()) {
                    lock.wait(6000)
                } else {
                    val timeout = timeouts[0]
                    if (System.currentTimeMillis() >= actualTime[timeout]!!) {
                        timeout.timeout()
                        timeouts.remove(timeout)
                        actualTime.remove(timeout)
                        return@synchronized
                    }

                    lock.wait(actualTime[timeout]!! - System.currentTimeMillis())
                    timeout.timeout()
                    timeouts.remove(timeout)
                    actualTime.remove(timeout)
                }
            } catch (ie: InterruptedException) {
                interruptableCycle()
            }
        }

        interruptableCycle()
    }

    fun removeTask(timeout: Timeout) {
        timeouts.remove(timeout)
        actualTime.remove(timeout)
        order()
        interrupt()
    }

    fun addTask(timeout: Timeout) {
        actualTime[timeout] = System.currentTimeMillis() + timeout.millis
        timeouts.add(timeout)
        order()
        interrupt()
    }

    private fun order() = timeouts.orderSelf { o1, o2 -> ((o1.millis - o2.millis) / 1000).toInt() }
}

private val timeoutTask = SingleThreadedTimeout()

fun timeOutHandler(time: Long, unit: TimeUnit, handler: () -> Unit) = timeoutTask.addTask(object: Timeout(time, unit) {
    override fun timeout() = handler()
})

abstract class Timeout(time: Long, unit: TimeUnit) {
    private var time = TimeUnit.MILLISECONDS.convert(time, unit)

    val millis: Long
        get() = time

    init {
        timeoutTask.addTask(this)
    }
    fun keepAlive() {
        timeoutTask.removeTask(this)
        timeoutTask.addTask(this)
    }
    fun stopTimeout() = timeoutTask.removeTask(this)
    abstract fun timeout()
}