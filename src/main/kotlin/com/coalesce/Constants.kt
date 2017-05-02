package com.coalesce

import com.coalesce.punishmentals.Punishment
import com.coalesce.punishmentals.PunishmentSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.text.DecimalFormat
import java.util.regex.Pattern

object Constants {
    const val COMMAND_PREFIX = "!"
    val DATA_DIRECTORY = File("data")
    val GSON: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().registerTypeAdapter(Punishment::class.java, PunishmentSerializer()).create()
    val TEMPERATURE_KELVIN: Pattern = Pattern.compile("K*", Pattern.CASE_INSENSITIVE)
    val TEMPERATURE_CELSIUS: Pattern = Pattern.compile("C*", Pattern.CASE_INSENSITIVE)
    val TEMPERATURE_FAHRENHEIT: Pattern = Pattern.compile("F*", Pattern.CASE_INSENSITIVE)
    val DECIMAL_FORMAT: DecimalFormat = DecimalFormat("##.##")
    const val USER_AGENT = "CoalesceBot/1.0"
}