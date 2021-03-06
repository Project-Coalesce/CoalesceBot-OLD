package com.coalesce.bot.utilities

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.Color
import java.io.InputStream
import java.io.OutputStream
import java.math.BigDecimal
import java.net.URLConnection
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Predicate
import java.util.regex.Pattern


var EmbedBuilder.embColor: Color?
    set(color) { setColor(color) }
    get() = null

var EmbedBuilder.embTitle: String?
    set(title) { setTitle(title, null) }
    get() = null

var EmbedBuilder.embDescription: String?
    set(description) { setDescription(description) }
    get() = null

val Pair<Long, TimeUnit>.millis: Long
    get() = second.toMillis(first)

infix fun <E> Iterable<E>.and(other: E): List<E> =
    listOf {
        addAll(this@and)
        add(other)
    }

infix fun <E> Iterable<E>.and(other: Iterable<E>): List<E> =
    listOf {
        addAll(this@and)
        addAll(other)
    }

fun <E> Predicate<E>.toLambdaFunc(): (E) -> Boolean = { this@toLambdaFunc.test(it) }

fun <E> listOf(handler: MutableList<E>.() -> Unit) = mutableListOf<E>().apply(handler)

fun <E> tryOrNull(func: () -> E): E? {
    try {
        return func()
    } catch (ex: Exception) {
        return null
    }
}

fun EmbedBuilder.description(builder: StringBuilder.() -> Unit) {
    embDescription = StringBuilder().apply(builder).toString().truncate(0, 1000)
}

fun <T> Iterable<T>.count(check: (T) -> Boolean): Int {
    var amount = 0
    forEach { if (check(it)) amount ++ }
    return amount
}

fun tryLog(message: String, func: () -> Unit) =
    try {
        func()
    } catch (ex: Exception) {
        System.err.println(message)
        ex.printStackTrace()
    }

val urlRegex = Pattern.compile("((http:\\/\\/|https:\\/\\/)?(www.)?(([a-zA-Z0-9-]){2,}\\.){1,4}([a-zA-Z]){2,6}(\\/([a-zA-Z-_\\/\\.0-9#:?=&;,]*)?)?)")

fun String.containsUrl(): Boolean {
    val matcher = urlRegex.matcher(this)
    return matcher.find()
}

fun String.smallTimeUnit(): TimeUnit? {
    TimeUnit.values().forEach {
        if (it.toString().startsWith(this.toUpperCase())) return@smallTimeUnit it
    }

    return null
}

fun quietly(func: () -> Unit) = try{ func() } catch (ex: Exception) { /* Ignore */ }

fun <E> List<E>.subList(fromIndex: Int): List<E> = subList(fromIndex, size)

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

fun String.matchList(regx: Regex): List<MatchResult> {
    val matchList = regx.matchEntire(this)
    return listOf {
        while (true) {
            add((matchList ?: break).next() ?: break)
        }
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

fun URLConnection.readBytes(): ByteArray {
    setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")
    return getInputStream().readBytes()
}

fun <E> Collection<E>.allWhere(handler: (E) -> Boolean) = listOf<E> {
    this@allWhere.forEach {
        if (handler(it)) add(it)
    }
}
fun OutputStream.writeText(text: String, charset: Charset = kotlin.text.Charsets.UTF_8): Unit = write(text.toByteArray(charset))

fun <T> List<T>.order(comp: Comparator<T>): MutableList<T> {
    val list = ArrayList(this)
    Collections.sort(list, comp)
    return list
}

fun Int.nth(): String {
    val nth = this % 10
    return "$nth${when (nth) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }}"
}

fun <T> List<T>.order(func: (T, T) -> Int): MutableList<T> {
    val list = ArrayList(this)
    Collections.sort(list, func)
    return list
}

fun InputStream.readText(charset: Charset = Charsets.UTF_8): String = readBytes().toString(charset)

fun <T> List<T>.orderSelf(func: (T, T) -> Int) = Collections.sort(this, func)

interface Embeddables {
    fun embed(): EmbedBuilder {
        return EmbedBuilder()
    }

    fun makeField(title: String?, text: String, inline: Boolean = false): MessageEmbed.Field {
        return MessageEmbed.Field(title, text, inline)
    }

    fun EmbedBuilder.field(title: String?, text: String, inline: Boolean = false): EmbedBuilder {
        return this.addField(makeField(title, text, inline))
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
                        runTimeout(timeout)
                        timeouts.remove(timeout)
                        actualTime.remove(timeout)
                        return@synchronized
                    }

                    lock.wait(actualTime[timeout]!! - System.currentTimeMillis())
                    runTimeout(timeout)
                    timeouts.remove(timeout)
                    actualTime.remove(timeout)
                }
            } catch (ie: InterruptedException) {
                interruptableCycle()
            }
        }

        interruptableCycle()
    }

    private fun runTimeout(timeout: Timeout) {
        Thread {
            tryLog("Failed to run timeout task") { timeout.timeout() }
        }.apply {
            name = "Timeout task"
            start()
        }
    }

    fun contains(timeout: Timeout) = timeouts.contains(timeout)

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

fun Double.round(decimals: Int = 0, roundingMode: Int = BigDecimal.ROUND_HALF_UP) = BigDecimal(this).setScale(decimals, roundingMode).toString()

fun String.insertDashUUID(): String {
    var sb = StringBuffer(this)
    sb.insert(8, "-")
    sb = StringBuffer(sb.toString())
    sb.insert(13, "-")
    sb = StringBuffer(sb.toString())
    sb.insert(18, "-")
    sb = StringBuffer(sb.toString())
    sb.insert(23, "-")

    return sb.toString()
}

private val timeoutTask = SingleThreadedTimeout()

fun <E> MutableMap<E, *>.removeAll(list: List<E>) = list.forEach { remove(it) }

fun timeOutHandler(time: Long, unit: TimeUnit, handler: () -> Unit) = object: Timeout(time, unit) {
    override fun timeout() = handler()
}

abstract class Timeout(time: Long, unit: TimeUnit) {
    private var time = TimeUnit.MILLISECONDS.convert(time, unit)

    val millis: Long
        get() = time
    val waiting: Boolean
        get() = timeoutTask.contains(this)

    init {
        timeoutTask.addTask(this)
    }

    fun keepAlive() {
        timeoutTask.removeTask(this)
        timeoutTask.addTask(this)
    }

    fun startTimeout() = timeoutTask.addTask(this)
    fun stopTimeout() = timeoutTask.removeTask(this)
    abstract fun timeout()
}

object ColorBlender {
    fun blendColors(fractions: FloatArray, colors: Array<Color>, progress: Float): Color {
        val indicies = getFractionIndicies(fractions, progress)
        val range = floatArrayOf(fractions[indicies[0]], fractions[indicies[1]])
        val colorRange = arrayOf(colors[indicies[0]], colors[indicies[1]])
        val max = range[1] - range[0]
        val value = progress - range[0]
        val weight = value / max

        return blend(colorRange[0], colorRange[1], (1.0f - weight).toDouble())
    }

    fun getFractionIndicies(fractions: FloatArray, progress: Float): IntArray {
        val range = IntArray(2)

        var startPoint: Int
        startPoint = 0
        while (startPoint < fractions.size && fractions[startPoint] <= progress) startPoint ++
        if (startPoint >= fractions.size) startPoint = fractions.size - 1

        range[0] = startPoint - 1
        range[1] = startPoint

        return range
    }

    fun blend(color1: Color, color2: Color, ratio: Double): Color {
        val r = ratio.toFloat()
        val ir = 1.0f - r
        val red = (color1.red.toFloat() * r + color2.red.toFloat() * ir).toInt()
        val green = (color1.green.toFloat() * r + color2.green.toFloat() * ir).toInt()
        val blue = (color1.blue.toFloat() * r + color2.blue.toFloat() * ir).toInt()
        val alpha = (color1.alpha.toFloat() * r + color2.alpha.toFloat() * ir).toInt()

        return Color(red, green, blue, alpha)
    }
}