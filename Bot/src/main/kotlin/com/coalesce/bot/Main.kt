package com.coalesce.bot

import com.coalesce.bot.command.AdaptationArgsChecker
import com.coalesce.bot.command.Listener
import com.coalesce.bot.command.PluginManager
import com.coalesce.bot.experience.ExperienceCachedDataManager
import com.coalesce.bot.punishmentals.Punishment
import com.coalesce.bot.punishmentals.PunishmentManager
import com.coalesce.bot.punishmentals.PunishmentSerializer
import com.coalesce.bot.utilities.readText
import com.coalesce.bot.utilities.tryLog
import com.google.common.base.Preconditions
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import java.io.File
import java.io.PrintStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom

/**
 *  VERSION
 *
 *  First number - Major version
 *  Second number - Minor version
 *  Third number - Patch
 * */
val VERSION = Main::class.java.getResourceAsStream("/.properties").readText() //TODO: Don't forget to update version

val GAMES = arrayOf("mienkreft", "with myself", "with lolis", "with my components", "with dabBot",
        "with spoopy skeletons", "with PI", "against humanity", "blame @deprilula28#3609 if anything borks",
        "on a third world server")

fun main(args: Array<String>) {
    Preconditions.checkArgument(args.isNotEmpty(), "You need to specify a token.")
    Main.instance.boot(args[0].replace("\"", ""), args[1], args.size > 2 && args[2] == "consoleLogging")
}

class Main private constructor() {
    lateinit var jda: JDA
    lateinit var punishments: PunishmentManager
    lateinit var injector: Injector
    lateinit var githubSecret: String
    lateinit var coCoinsManager: CoCoinsManager
    lateinit var commandTypeAdapter: AdaptationArgsChecker
    lateinit var commandHandler: Listener
    lateinit var pluginManager: PluginManager
    lateinit var experienceCachedDataManager: ExperienceCachedDataManager
    val executor = Executors.newFixedThreadPool(6)!!

    internal fun boot(token: String, secret: String, logOnConsole: Boolean) {
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs()
        }
        jda = JDABuilder(AccountType.BOT).apply {
            setToken(token)
            setCorePoolSize(6)
            setAudioEnabled(true) // Depri has implemented a Youtube player that proxi fucked up.
            @Suppress("INTERFACE_STATIC_METHOD_CALL_FROM_JAVA6_TARGET")
            setGame(Game.of("the loading game"))
            setStatus(OnlineStatus.DO_NOT_DISTURB)
        }.buildBlocking()

        if (!logOnConsole) {
            tryLog("Failed to load print streams") {
                System.setOut(PrintStream(ChatOutputStream(jda.getTextChannelById(SYS_OUT_CHANNEL))))
                System.setErr(PrintStream(ChatOutputStream(jda.getTextChannelById(SYS_IN_CHANNEL))))
            }
        }

        loadCommands()

        // Finished loading.
        @Suppress("INTERFACE_STATIC_METHOD_CALL_FROM_JAVA6_TARGET") // cause it's still fucking driving me nuts
        jda.presence.game = Game.of(GAMES[ThreadLocalRandom.current().nextInt(GAMES.size)])
        jda.presence.status = OnlineStatus.DO_NOT_DISTURB

        githubSecret = secret

        println("Outputting messages to this channel. Running CoalesceBot version $VERSION.")
    }

    fun loadCommands() {
        tryLog("Failed to load Punishment Manager") { punishments = PunishmentManager(this) }
        tryLog("Failed to load plugins") { pluginManager = PluginManager() }
        tryLog("Failed to load CoCoins Manager") { coCoinsManager = CoCoinsManager() }
        tryLog("Failed to load Command Type Adapters") { commandTypeAdapter = AdaptationArgsChecker(jda) }
        tryLog("Failed to load Experience Manager") { experienceCachedDataManager = ExperienceCachedDataManager() }

        tryLog("Failed to load Command Handler") {
            injector = Guice.createInjector(Injects(this))
            commandHandler = Listener(jda, commandTypeAdapter, injector, pluginManager)
            jda.addEventListener(commandHandler)
        }
    }

    companion object {
        val instance: Main = Main()
            @Deprecated(message = "One should rather use dependency injection.") get
    }
}

class Injects(val main: Main) : AbstractModule() {
    override fun configure() {
        main.pluginManager.addedGuiceInjections.forEach {
            addGuiceInjection(it.key, it.value)
        }
        bind(Main::class.java).toInstance(main)
        bind(JDA::class.java).toInstance(main.jda)
        bind(PunishmentManager::class.java).toInstance(main.punishments)
        bind(CoCoinsManager::class.java).toInstance(main.coCoinsManager)
        bind(ExecutorService::class.java).toInstance(main.executor)
        bind(ExperienceCachedDataManager::class.java).toInstance(main.experienceCachedDataManager)
    }

    fun <T> addGuiceInjection(clazz: Class<T>, obj: Any) = bind(clazz).toInstance(obj as T)
}

const val TESTING_GUILD = 371446366049665034L
const val COALESCE_GUILD = 268187052753944576L
const val SYS_IN_CHANNEL = 371446583860002828L
const val SYS_OUT_CHANNEL = 371446558211964930L
const val COMMAND_PREFIX = "!"
const val COMMAND_PREFIX_LENGTH = COMMAND_PREFIX.length //Every nanosecond matters.
val tempDirectory = File(".${File.separatorChar}temp")
val dataDirectory = File(".${File.separatorChar}data")
val pluginsFolder = File("plugins")
val usingPluginsFolder = File("using-plugins")
val coCoinsFile = File(dataDirectory, "cocoins.dat")
val experienceFile = File(dataDirectory, "messagesSent.dat")
val gson: Gson = GsonBuilder().apply {
    enableComplexMapKeySerialization()
    setPrettyPrinting()
    serializeNulls()
    disableHtmlEscaping()
    registerTypeAdapter(Punishment::class.java, PunishmentSerializer(Main.instance))
}.create()