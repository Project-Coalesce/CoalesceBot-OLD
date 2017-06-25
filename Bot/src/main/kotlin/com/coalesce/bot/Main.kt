package com.coalesce.bot

//import com.coalesce.bot.chatbot.ChatbotBrain
import com.coalesce.bot.command.AdaptationArgsChecker
import com.coalesce.bot.command.Listener
import com.coalesce.bot.command.PluginManager
import com.coalesce.bot.punishmentals.Punishment
import com.coalesce.bot.punishmentals.PunishmentManager
import com.coalesce.bot.punishmentals.PunishmentSerializer
import com.coalesce.bot.utilities.tryLog
import com.google.common.base.Preconditions
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Guild
import java.io.File
import java.io.PrintStream
import java.util.concurrent.ThreadLocalRandom
import java.util.regex.Pattern

/**
 *  VERSION
 *
 *  First number - Major version
 *  Second number - Minor version
 *  Third number - Patch
 * */
val VERSION = "1.6.0"
val GAMES = arrayOf("mienkreft", "with myself", "with lolis", "with my components", "with dabBot")

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
                System.setOut(PrintStream(ChatOutputStream(jda.getTextChannelById("315934708879982592"))))
                System.setErr(PrintStream(ChatOutputStream(jda.getTextChannelById("315934723354656768"))))
            }
        }

        tryLog("Failed to load Punishment Manager") { punishments = PunishmentManager(this) }
        tryLog("Failed to load plugins") { pluginManager = PluginManager() }
        tryLog("Failed to load CoCoins Manager") { coCoinsManager = CoCoinsManager() }
        tryLog("Failed to load Command Type Adapters") { commandTypeAdapter = AdaptationArgsChecker(jda) }

        tryLog("Failed to load Command Handler") {
            /*
            listener = Listener(jda)
            listener.register()
            jda.addEventListener(listener)
            */

            injector = Guice.createInjector(Injects(this))
            commandHandler = Listener(jda, commandTypeAdapter, injector, pluginManager)
            jda.addEventListener(commandHandler)
        }
        //tryLog("Failed to load GC") { gc = GC(listener, repManager) }

        // Finished loading.
        @Suppress("INTERFACE_STATIC_METHOD_CALL_FROM_JAVA6_TARGET") // cause it's still fucking driving me nuts
        jda.presence.game = Game.of(GAMES[ThreadLocalRandom.current().nextInt(GAMES.size)])
        jda.presence.status = OnlineStatus.ONLINE

        githubSecret = secret

        println("Outputting messages to this channel. Running CoalesceBot version $VERSION.")
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
    }

    fun <T> addGuiceInjection(clazz: Class<T>, obj: Any) = bind(clazz).toInstance(obj as T)
}
const val COALESCE_GUILD = 268187052753944576L
const val commandPrefix = "!"
const val commandPrefixLen = commandPrefix.length //Every nanosecond matters.
val dataDirectory = File(".${File.separatorChar}data")
val pluginsFolder = File("plugins")
val globalPermissionsFile = File(dataDirectory, "global.dat")
val coCoinsFile = File(dataDirectory, "cocoins.dat")
val blacklistFile = File(dataDirectory, "blacklist.json")
val rulesMessageFile = File(dataDirectory, "rulesMessage.dat")
val gson: Gson = GsonBuilder().apply {
    enableComplexMapKeySerialization()
    setPrettyPrinting()
    serializeNulls()
    disableHtmlEscaping()
    registerTypeAdapter(Punishment::class.java, PunishmentSerializer(Main.instance))
}.create()
typealias Colour = java.awt.Color
val temperatureKelvin = Pattern.compile("K*", Pattern.CASE_INSENSITIVE)!!
val temperatureCelsius = Pattern.compile("C*", Pattern.CASE_INSENSITIVE)!!
val temperatureFahrenheit = Pattern.compile("F*", Pattern.CASE_INSENSITIVE)!!
val canDelete: (Guild) -> Boolean = { it.selfMember.hasPermission(Permission.MESSAGE_MANAGE) }
//val chatbot = ChatbotBrain()

/*fun getChatbotMessage(message: Message, jda: JDA): String? {
    val stripped = message.strippedContent.replace(jda.selfUser.asMention, "")
    chatbot.decay()
    chatbot.digestSentence(stripped)
    return chatbot.buildSentence()
}*/
