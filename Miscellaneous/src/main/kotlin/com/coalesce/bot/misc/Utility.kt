package com.coalesce.bot.misc

import com.coalesce.bot.utilities.readText
import java.net.URLConnection

val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36"

fun URLConnection.readText(): String {
    setRequestProperty("User-Agent", userAgent)
    return getInputStream().readText()
}
