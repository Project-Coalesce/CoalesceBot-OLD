package com.coalesce.bot

import com.coalesce.bot.commands.Listener
import com.coalesce.bot.punishmentals.Punishment
import com.coalesce.bot.punishmentals.PunishmentManager
import com.coalesce.bot.punishmentals.PunishmentSerializer
import com.google.common.base.Preconditions
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Guild
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

fun main(args: Array<String>) {
    Preconditions.checkArgument(args.isNotEmpty(), "You need to specify a token.")
    Main.instance.boot(args[0].replace("\"", ""), args[1])
}

class Main private constructor() {
    lateinit var jda: JDA
    lateinit var punishments: PunishmentManager
    lateinit var injector: Injector
    lateinit var listener: Listener
    lateinit var githubSecret: String
    val executor = Executors.newFixedThreadPool(6)!!

    internal fun boot(token: String, secret: String) {
        if (!dataDirectory.exists()) {
                dataDirectory.mkdirs()
            }

            jda = JDABuilder(AccountType.BOT).apply {
                setToken(token)
                setCorePoolSize(6)
                setAudioEnabled(true) // Depri has implemented a Youtube player that proxi fucked up.
                @Suppress("INTERFACE_STATIC_METHOD_CALL_FROM_JAVA6_TARGET")
            setGame(Game.of("with my settings"))
        }.buildBlocking()

        punishments = PunishmentManager(this) // Load it.
        injector = Guice.createInjector(Injects(this, punishments))
        listener = Listener()
        jda.addEventListener(listener)

        // Finished loading.
        @Suppress("INTERFACE_STATIC_METHOD_CALL_FROM_JAVA6_TARGET") // cause it's still fucking driving me nuts
        jda.presence.game = Game.of("with myself")

        githubSecret = secret
    }

    companion object {
        val instance: Main = Main()
            @Deprecated(message = "One should rather use dependency injection, though in some cases it isn't possible.") get
    }
}

class Injects(val main: Main, val pmanager: PunishmentManager) : AbstractModule() {
    override fun configure() {
        bind(Main::class.java).toInstance(main)
        bind(JDA::class.java).toInstance(main.jda)
        bind(PunishmentManager::class.java).toInstance(pmanager)
        bind(ExecutorService::class.java).toInstance(main.executor)
    }
}

const val commandPrefix = "!"
const val commandPrefixLen = commandPrefix.length //Every nanosecond matters.
val dataDirectory = File(".${File.separatorChar}data")
val respectsLeaderboardsFile = File(dataDirectory, "leaderboard.json")
val reputationFile = File(dataDirectory, "reputation.json")
val gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().registerTypeAdapter(Punishment::class.java, PunishmentSerializer(Main.instance)).create()
typealias Colour = java.awt.Color
val temperatureKelvin = Pattern.compile("K*", Pattern.CASE_INSENSITIVE)!!
val temperatureCelsius = Pattern.compile("C*", Pattern.CASE_INSENSITIVE)!!
val temperatureFahrenheit = Pattern.compile("F*", Pattern.CASE_INSENSITIVE)!!
val canDelete: (Guild) -> Boolean = { it.selfMember.hasPermission(Permission.MESSAGE_MANAGE) }