package com.coalesce.bot

//import com.coalesce.bot.chatbot.ChatbotBrain
import com.coalesce.bot.commands.Listener
import com.coalesce.bot.punishmentals.Punishment
import com.coalesce.bot.punishmentals.PunishmentManager
import com.coalesce.bot.punishmentals.PunishmentSerializer
import com.coalesce.bot.reputation.ReputationManager
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
import net.dv8tion.jda.core.entities.Message
import java.io.File
import java.io.PrintStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom
import java.util.regex.Pattern

/**
 * VERSION
 *
 * First digit = number of release
 * Second  "   = number of the patch
 * */
val VERSION = "1.3"
val GAMES = arrayOf("mienkreft", "with myself", "with lolis", "with my components", "with dabBot")

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
    lateinit var repManager: ReputationManager
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
                setGame(Game.of("the loading game"))
                setStatus(OnlineStatus.DO_NOT_DISTURB)
        }.buildBlocking()

        tryLog("Failed to load Reputation Manager") { repManager = ReputationManager() }
        tryLog("Failed to load Punishment Manager") { punishments = PunishmentManager(this) }

        injector = Guice.createInjector(Injects(this, punishments))
        listener = Listener(jda)
        listener.register()
        jda.addEventListener(listener)

        // Finished loading.
        @Suppress("INTERFACE_STATIC_METHOD_CALL_FROM_JAVA6_TARGET") // cause it's still fucking driving me nuts
        jda.presence.game = Game.of(GAMES[ThreadLocalRandom.current().nextInt(GAMES.size)])
        jda.presence.status = OnlineStatus.ONLINE

        githubSecret = secret

        System.setOut(PrintStream(ChatOutputStream(jda.getTextChannelById("315934708879982592"))))
        System.setErr(PrintStream(ChatOutputStream(jda.getTextChannelById("315934723354656768"))))
        println("Outputting messages to this channel. Running CoalesceBot version $VERSION.")
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
        bind(ReputationManager::class.java).toInstance(main.repManager)
    }
}
const val COALESCE_GUILD = 268187052753944576L
const val commandPrefix = "!"
const val commandPrefixLen = commandPrefix.length //Every nanosecond matters.
val dataDirectory = File(".${File.separatorChar}data")
val quotedFile = File(dataDirectory, "quoted.txt")
val globalPermissionsFile = File(dataDirectory, "global.dat")
val respectsLeaderboardsFile = File(dataDirectory, "leaderboard.dat")
val respectsLeaderboardsFileOld = File(dataDirectory, "leaderboard.json")
val reputationFile = File(dataDirectory, "reputation.dat")
val reputationFileOld = File(dataDirectory, "reputation.json")
val gson: Gson = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().serializeNulls().disableHtmlEscaping().registerTypeAdapter(Punishment::class.java, PunishmentSerializer(Main.instance)).create()
typealias Colour = java.awt.Color
val temperatureKelvin = Pattern.compile("K*", Pattern.CASE_INSENSITIVE)!!
val temperatureCelsius = Pattern.compile("C*", Pattern.CASE_INSENSITIVE)!!
val temperatureFahrenheit = Pattern.compile("F*", Pattern.CASE_INSENSITIVE)!!
val canDelete: (Guild) -> Boolean = { it.selfMember.hasPermission(Permission.MESSAGE_MANAGE) }
<<<<<<< Updated upstream
//val chatbot = ChatbotBrain()

/*fun getChatbotMessage(message: Message, jda: JDA): String? {
    val stripped = message.strippedContent.replace(jda.selfUser.asMention, "")
    chatbot.decay()
    chatbot.digestSentence(stripped)
    return chatbot.buildSentence()
}*/
=======
val chatbot = ChatbotBrain()
>>>>>>> Stashed changes
