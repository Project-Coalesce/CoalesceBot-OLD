package com.coalesce.bot

import com.coalesce.bot.commands.Listener
import com.coalesce.bot.punishmentals.Punishment
import com.coalesce.bot.punishmentals.PunishmentManager
import com.coalesce.bot.punishmentals.PunishmentSerializer
import com.google.common.base.Preconditions
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Game
import java.io.File
import java.util.regex.Pattern

fun main(args: Array<String>) {
    Preconditions.checkArgument(args.isNotEmpty(), "You need to specify a token.")
    Main.instance.boot(args[0].replace("\"", ""))
}

class Main private constructor() {
    lateinit var jda: JDA
    lateinit var punishments: PunishmentManager

    internal fun boot(token: String) {
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs()
        }

        jda = JDABuilder(AccountType.BOT).apply {
            setToken(token)
            setCorePoolSize(6)
            setAudioEnabled(true) // Depri is implementing a YouTube player.
            @Suppress("INTERFACE_STATIC_METHOD_CALL_FROM_JAVA6_TARGET") // cause it's fucking driving me nuts even tho we target 1.8
            setGame(Game.of("with my settings"))
        }.buildBlocking()

        punishments = PunishmentManager(this) // Load it.
        jda.addEventListener(Listener())

        // Finished loading.
        @Suppress("INTERFACE_STATIC_METHOD_CALL_FROM_JAVA6_TARGET") // cause it's still fucking driving me nuts
        jda.presence.game = Game.of("with myself")
    }

    companion object {
        val instance: Main = Main()
            @Deprecated(message = "One should rather use dependency injection, though in some cases it isn't possible.") get
    }
}

const val commandPrefix = "!"
val dataDirectory = File(".${File.separatorChar}data")
val gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().registerTypeAdapter(Punishment::class.java, PunishmentSerializer(Main.instance)).create()
typealias Colour = java.awt.Color
val temperatureKelvin = Pattern.compile("K*", Pattern.CASE_INSENSITIVE)!!
val temperatureCelsius = Pattern.compile("C*", Pattern.CASE_INSENSITIVE)!!
val temperatureFahrenheit = Pattern.compile("F*", Pattern.CASE_INSENSITIVE)!!