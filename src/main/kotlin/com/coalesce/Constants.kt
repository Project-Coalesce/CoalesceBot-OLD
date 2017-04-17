package com.coalesce

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.text.DecimalFormat
import java.util.regex.Pattern

object Constants {
    val COMMAND_PREFIX = "!"
    val DATA_DIRECTORY = File("data")
    val GSON: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create()
    val TEMPERATURE_KELVIN: Pattern = Pattern.compile("K*", Pattern.CASE_INSENSITIVE)
    val TEMPERATURE_CELSIUS: Pattern = Pattern.compile("C*", Pattern.CASE_INSENSITIVE)
    val TEMPERATURE_FAHRENHEIT: Pattern = Pattern.compile("F*", Pattern.CASE_INSENSITIVE)
    val DECIMAL_FORMAT: DecimalFormat = DecimalFormat("##.00")
}